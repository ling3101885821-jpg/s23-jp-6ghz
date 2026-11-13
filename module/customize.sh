#!/system/bin/sh
[ "$KSU" = "true" ] || abort "KernelSU required / 需要 KernelSU"
[ "$(getprop ro.product.model)" = "SM-S9180" ] || abort "Unsupported model / 机型不匹配"
[ "$(getprop ro.build.display.id)" = "S9180ZCU6EYI6_YanYu_Moded" ] || abort "Unsupported firmware / 固件不匹配"
ui_print "S23 JP 6 GHz - experimental / 实验版"
ui_print "No data wipe. Configure 6 GHz + WPA3 in Settings after reboot."
ui_print "不清空数据；重启后在原生设置中选择 6 GHz 和 WPA3。"
set_perm_recursive "$MODPATH" 0 0 0755 0644
for script in action.sh service.sh post-fs-data.sh uninstall.sh; do
    set_perm "$MODPATH/$script" 0 0 0700
done
set_perm "$MODPATH/hotspot.dex" 0 0 0600
set_perm "$MODPATH/WCNSS_qcom_cfg.ini" 0 0 0644 u:object_r:vendor_configs_file:s0
