#!/system/bin/sh
MODDIR=${0%/*}
sh "$MODDIR/action.sh" stop
cmd wifi force-country-code disabled
