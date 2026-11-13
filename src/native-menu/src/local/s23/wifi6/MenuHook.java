package local.s23.wifi6;
import android.content.Context;
import android.os.Build;
import java.lang.reflect.Field;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Settings-process-only UI gate. Does not patch drivers or global CSC properties. */
public final class MenuHook implements IXposedHookLoadPackage {
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p) throws Throwable {
        if (!"com.android.settings".equals(p.packageName)) return;
        if (!"SM-S9180".equals(Build.MODEL) || !"S9180ZCU6EYI6_YanYu_Moded".equals(Build.DISPLAY)) {
            XposedBridge.log("S23Wifi6: firmware mismatch; no hooks installed"); return;
        }
        Class<?> sem = Class.forName("com.samsung.android.wifi.SemWifiManager", false, p.classLoader);
        Field contextField = sem.getDeclaredField("mContext");
        contextField.setAccessible(true);
        XposedBridge.hookMethod(sem.getMethod("supportWifiAp6GBasedOnCountry"), new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Context context = (Context)contextField.get(param.thisObject);
                    Object wifi = context.getSystemService("wifi");
                    Object country = wifi.getClass().getMethod("getCountryCode").invoke(wifi);
                    if ("JP".equals(country)) param.setResult(true);
                } catch (Throwable e) {
                    XposedBridge.log("S23Wifi6: could not verify JP; original menu policy retained: " + e.getClass().getSimpleName());
                }
            }
        });
        XposedBridge.log("S23Wifi6: Settings-only JP menu hook installed");
        try {
            WifiDetailsHook.install(p.classLoader);
        } catch (Throwable e) {
            XposedBridge.log("S23Wifi6: Wi-Fi details hook unavailable: " + e);
        }
    }
}
