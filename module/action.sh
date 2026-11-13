#!/system/bin/sh
MODDIR=${0%/*}
umask 077
export CLASSPATH="$MODDIR/hotspot.dex"
probe() { app_process /system/bin HotspotProbe "$@"; }
restore_wifi() {
    if [ -f "$MODDIR/wifi-was-enabled" ]; then
        cmd wifi set-wifi-enabled enabled
        rm -f "$MODDIR/wifi-was-enabled"
    fi
}
stop_ours() {
    probe stop
    cmd wifi stop-softap >/dev/null 2>&1
    rm -f "$MODDIR/active"
    restore_wifi
    echo '6 GHz 热点已关闭。'
}
[ "$(getprop ro.product.model)" = "SM-S9180" ] || { echo '机型不匹配，未修改。'; exit 1; }
[ "$(getprop ro.build.display.id)" = "S9180ZCU6EYI6_YanYu_Moded" ] || { echo '固件版本已改变，需要重新验证。'; exit 1; }
if [ "$1" = "stop" ]; then
    [ -f "$MODDIR/active" ] && stop_ours
    exit 0
fi
if [ -f "$MODDIR/active" ] && probe status | grep -q 'AP_STATE=13'; then
    stop_ours
    exit 0
fi
if probe status | grep -q 'AP_STATE=13'; then
    echo '已有热点正在运行，请先关闭现有热点再点操作。'
    exit 1
fi
cmd wifi force-country-code enabled JP || exit 1
# Preserve the Wi-Fi uplink. Native Wi-Fi sharing selects the upstream.
settings put secure wifi_ap_wifi_sharing 1
if ! probe start-saved; then
    probe stop >/dev/null 2>&1
    restore_wifi
    echo '启动失败，已撤销本次热点启动。'
    exit 1
fi
touch "$MODDIR/active"
sleep 3
APINFO=$(dumpsys wifi | awk '/^mCurrentSoftApInfoMap/ && !seen++ {print}')
if ! echo "$APINFO" | grep -Eq 'frequency= (59[5-9][0-9]|6[0-9][0-9][0-9])[, ]'; then
    echo '热点已启动，但实际未运行在 6 GHz。'
    echo '请检查原生保存频段、JP地区及MCC配置是否生效；此处报告的是实际运行频率。'
else
    echo '已验证热点实际运行在 6 GHz。'
fi
echo '名称和密码沿用“设置 → 移动热点”中保存的配置。'
echo "$APINFO"
dumpsys tethering | awk '/^    Current upstream interface\(s\):/ {print}'
echo 'Wi-Fi 保持原状态；再次点“操作”关闭本按钮启动的热点。'
