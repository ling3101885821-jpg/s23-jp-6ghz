package local.s23.wifi6;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/** Adds live connection information to this firmware's native Wi-Fi detail page. */
final class WifiDetailsHook {
    private static final WeakHashMap<Object, Panel> PANELS = new WeakHashMap<>();

    static void install(ClassLoader loader) throws Exception {
        Class<?> controller = Class.forName("com.samsung.android.settings.wifi.details.WifiDetailPreferenceController", false, loader);
        Class<?> screen = Class.forName("androidx.preference.PreferenceScreen", false, loader);
        XposedBridge.hookMethod(controller.getMethod("displayPreference", screen), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                try {
                    Panel old = PANELS.remove(param.thisObject);
                    if (old != null) old.stop();
                    Panel panel = new Panel(param.thisObject, param.args[0], loader);
                    PANELS.put(param.thisObject, panel);
                    panel.refresh();
                } catch (Throwable error) { log(error); }
            }
        });
        for (String name : new String[]{"onResume", "onUpdated", "onPause"}) {
            final String event = name;
            XposedBridge.hookMethod(controller.getMethod(name), new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    Panel panel = PANELS.get(param.thisObject);
                    if (panel == null) return;
                    if (event.equals("onPause")) panel.stop();
                    else if (event.equals("onResume")) panel.start();
                    else panel.refresh();
                }
            });
        }
        XposedBridge.log("S23Wifi6: live Wi-Fi details hooks installed v3");
    }

    private static void log(Throwable error) {
        XposedBridge.log("S23Wifi6: Wi-Fi details: " + error);
    }

    private static final class Panel implements Runnable {
        final WeakReference<Object> owner;
        final WeakReference<Object> frequencyRow;
        final WeakReference<Object> speedRow;
        final Class<?> preference;
        final Handler handler = new Handler(Looper.getMainLooper());
        boolean active;
        String lastFrequency;
        boolean errorLogged;

        Panel(Object controller, Object screen, ClassLoader loader) throws Exception {
            owner = new WeakReference<>(controller);
            preference = Class.forName("androidx.preference.Preference", false, loader);
            Class<?> group = Class.forName("androidx.preference.PreferenceGroup", false, loader);
            Object security = controller.getClass().getField("mSecurityPref").get(controller);
            Context context = (Context) preference.getMethod("getContext").invoke(security);
            Object parent = preference.getMethod("getParent").invoke(security);
            if (parent == null) parent = screen;
            int order = (Integer) preference.getMethod("getOrder").invoke(security);
            frequencyRow = new WeakReference<>(row(parent, group, context, "s23_live_frequency", "当前连接频段与信道", order));
            speedRow = new WeakReference<>(row(parent, group, context, "s23_live_link_speed", "Wi-Fi 连接速率", order));
        }

        Object row(Object parent, Class<?> group, Context context, String key, String title, int order) throws Exception {
            Object row = group.getMethod("findPreference", CharSequence.class).invoke(parent, key);
            if (row == null) {
                row = preference.getConstructor(Context.class).newInstance(context);
                preference.getMethod("setKey", String.class).invoke(row, key);
                preference.getMethod("setTitle", CharSequence.class).invoke(row, title);
                preference.getMethod("setSelectable", boolean.class).invoke(row, false);
                preference.getMethod("setOrder", int.class).invoke(row, order);
                group.getMethod("addPreference", preference).invoke(parent, row);
            }
            return row;
        }

        void start() { stop(); active = true; run(); }
        void stop() { active = false; handler.removeCallbacks(this); }
        @Override public void run() {
            refresh();
            if (active && owner.get() != null && frequencyRow.get() != null) handler.postDelayed(this, 2000);
        }

        void refresh() {
            try {
                Object controller = owner.get(), frequency = frequencyRow.get(), speed = speedRow.get();
                if (controller == null || frequency == null || speed == null) { stop(); return; }
                Object entry = controller.getClass().getField("mWifiEntry").get(controller);
                boolean connected = (Integer) entry.getClass().getMethod("getConnectedState").invoke(entry) == 2;
                WifiManager manager = (WifiManager)controller.getClass().getField("mWifiManager").get(controller);
                WifiInfo info = connected ? manager.getConnectionInfo() : null;
                WifiConfiguration config = (WifiConfiguration)entry.getClass().getMethod("getWifiConfiguration").invoke(entry);
                if (info != null && config != null && config.networkId != info.getNetworkId()) info = null;
                String bandText = "未连接此网络";
                String speedText = "连接后显示发送与接收协商速率";
                if (info != null && info.getFrequency() > 0) {
                    int mhz = info.getFrequency();
                    String band = mhz >= 2400 && mhz < 2500 ? "2.4 GHz" : mhz >= 4900 && mhz < 5925 ? "5 GHz" : mhz >= 5925 && mhz < 7125 ? "6 GHz" : "未知频段";
                    bandText = band + " · " + mhz + " MHz";
                    int channel = ScanResult.convertFrequencyMhzToChannelIfSupported(mhz);
                    bandText += channel > 0 ? "\n主信道：" + channel : "\n主信道：暂不可用";
                    speedText = "发送：" + rate(info.getTxLinkSpeedMbps()) + "\n接收：" + rate(info.getRxLinkSpeedMbps()) + "\n协商速率，非实际上网速度";
                }
                preference.getMethod("setSummary", CharSequence.class).invoke(frequency, bandText);
                preference.getMethod("setSummary", CharSequence.class).invoke(speed, speedText);
                if (!bandText.equals(lastFrequency)) {
                    lastFrequency = bandText;
                    XposedBridge.log("S23Wifi6: native Wi-Fi rows refreshed: " + bandText + " | " + speedText.replace('\n', ';'));
                }
            } catch (Throwable error) {
                if (!errorLogged) { log(error); errorLogged = true; }
            }
        }
        static String rate(int mbps) { return mbps > 0 ? mbps + " Mbps" : "暂不可用"; }
    }
}
