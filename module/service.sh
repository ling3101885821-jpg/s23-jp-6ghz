#!/system/bin/sh
MODDIR=${0%/*}
umask 077
[ "$(getprop ro.product.model)" = "SM-S9180" ] || exit 1
[ "$(getprop ro.build.display.id)" = "S9180ZCU6EYI6_YanYu_Moded" ] || exit 1
i=0
while [ "$(getprop sys.boot_completed)" != "1" ] && [ "$i" -lt 120 ]; do
    sleep 2
    i=$((i + 1))
done
[ "$(getprop sys.boot_completed)" = "1" ] || exit 1
[ -e "$MODDIR/disable" ] && exit 0
i=0
while [ "$i" -lt 15 ]; do
    [ -e "$MODDIR/disable" ] && exit 0
    # Binder shell commands pass stdio FDs to system_server. Use a pipe rather
    # than a root-only module file FD, which SELinux can reject at transaction time.
    RESULT=$(cmd wifi force-country-code enabled JP 2>&1)
    printf '%s\n' "$RESULT" > "$MODDIR/boot-status.txt"
    COUNTRY=$(cmd wifi get-country-code 2>&1)
    echo "$COUNTRY" >> "$MODDIR/boot-status.txt"
    case "$COUNTRY" in *'= JP'*) exit 0;; esac
    i=$((i + 1))
    sleep 2
done
exit 1
