#!/system/bin/sh
MODDIR=${0%/*}
TARGET=/vendor/etc/wifi/kiwi_v2/WCNSS_qcom_cfg.ini
umask 077
[ "$(getprop ro.product.model)" = "SM-S9180" ] || exit 1
[ "$(getprop ro.build.display.id)" = "S9180ZCU6EYI6_YanYu_Moded" ] || exit 1
[ -e "$MODDIR/disable" ] && exit 0
if [ -e "$MODDIR/disable-mcc" ]; then
    echo 'MCC override disabled by marker' > "$MODDIR/mcc-status.txt"
    exit 0
fi
if [ "$(sha256sum "$TARGET" | cut -d ' ' -f 1)" != "db608e456f65f06fc56cd9f083d9c6a33f6a2790890e945f069ce15c180df903" ]; then
    echo 'Original vendor config changed; MCC override skipped' > "$MODDIR/mcc-status.txt"
    exit 1
fi
if mount --bind "$MODDIR/WCNSS_qcom_cfg.ini" "$TARGET"; then
    echo 'MCC policy override mounted: gWlanMccToSccSwitchMode=0' > "$MODDIR/mcc-status.txt"
else
    echo 'MCC policy override mount failed' > "$MODDIR/mcc-status.txt"
    exit 1
fi
