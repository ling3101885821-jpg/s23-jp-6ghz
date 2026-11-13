import android.content.Context;
import android.os.Looper;
import java.lang.reflect.*;
import java.util.concurrent.*;

/** Temporary, explicit test tool. Does not change saved hotspot configuration. */
public final class HotspotProbe {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) throw new IllegalArgumentException("start <ssid> <password> | stop | status");
        Looper.prepareMainLooper();
        Class<?> at = Class.forName("android.app.ActivityThread");
        Object thread = at.getMethod("systemMain").invoke(null);
        Context context = (Context)at.getMethod("getSystemContext").invoke(thread);
        Object manager = context.getSystemService("tethering");
        Class<?> tm = Class.forName("android.net.TetheringManager");
        if (args[0].equals("status")) {
            Object wifi = context.getSystemService("wifi");
            System.out.println("AP_STATE=" + wifi.getClass().getMethod("getWifiApState").invoke(wifi));
            System.exit(0);
        }
        if (args[0].equals("stop")) {
            tm.getMethod("stopTethering", int.class).invoke(manager, 0);
            System.out.println("Stop requested");
            System.exit(0);
        }

        boolean saved = args[0].equals("start-saved");
        if (!saved && (!args[0].equals("start") || args.length != 3))
            throw new IllegalArgumentException("start requires SSID and WPA3 password");
        Class<?> configClass = Class.forName("android.net.wifi.SoftApConfiguration");
        Object config;
        if (saved) {
            Object semWifi = context.getSystemService("sem_wifi");
            config = semWifi.getClass().getMethod("getSoftApConfiguration").invoke(semWifi);
        } else {
        Class<?> configBuilderClass = Class.forName("android.net.wifi.SoftApConfiguration$Builder");
        Object cb = configBuilderClass.getConstructor().newInstance();
        configBuilderClass.getMethod("setSsid", String.class).invoke(cb, args[1]);
        configBuilderClass.getMethod("setPassphrase", String.class, int.class).invoke(cb, args[2], 3);
        configBuilderClass.getMethod("setBand", int.class).invoke(cb, 4);
        configBuilderClass.getMethod("setAutoShutdownEnabled", boolean.class).invoke(cb, false);
        config = configBuilderClass.getMethod("build").invoke(cb);
        }
        Class<?> requestClass = Class.forName("android.net.TetheringManager$TetheringRequest");
        Class<?> builderClass = Class.forName("android.net.TetheringManager$TetheringRequest$Builder");
        Object rb = builderClass.getConstructor(int.class).newInstance(0);
        builderClass.getMethod("setSoftApConfiguration", configClass).invoke(rb, config);
        Object request = builderClass.getMethod("build").invoke(rb);
        CountDownLatch completed = new CountDownLatch(1);
        final boolean[] started = {false};
        Class<?> callbackClass = Class.forName("android.net.TetheringManager$StartTetheringCallback");
        Object callback = Proxy.newProxyInstance(callbackClass.getClassLoader(), new Class<?>[]{callbackClass}, (p,m,a) -> {
            if (m.getName().equals("onTetheringStarted")) {
                started[0] = true; System.out.println("TETHERING_STARTED"); completed.countDown();
            } else if (m.getName().equals("onTetheringFailed")) {
                System.out.println("TETHERING_FAILED code=" + a[0]); completed.countDown();
            } else if (m.getName().equals("toString")) return "HotspotProbeCallback";
            else if (m.getName().equals("hashCode")) return System.identityHashCode(p);
            else if (m.getName().equals("equals")) return p == a[0];
            return null;
        });
        Executor executor = Runnable::run;
        tm.getMethod("startTethering", requestClass, Executor.class, callbackClass).invoke(manager, request, executor, callback);
        if (!completed.await(25, TimeUnit.SECONDS)) System.out.println("CALLBACK_TIMEOUT; inspect live tethering state");
        System.exit(started[0] ? 0 : 1);
    }
}
