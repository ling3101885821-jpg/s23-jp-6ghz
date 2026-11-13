# Samsung S23 Ultra JP 6 GHz Wi-Fi and hotspot modules

[简体中文](README.md) · [Download packages](https://github.com/ling3101885821-jpg/s23-jp-6ghz/releases/tag/v0.1.0-experimental)

An experimental package for **SM-S9180, Android 16 / One UI 8, build `S9180ZCU6EYI6_YanYu_Moded`**. The KernelSU module retains a JP Wi-Fi country override. The LSPosed module exposes the native Samsung 6 GHz hotspot option and adds live band, primary-channel and negotiated link-rate information to Wi-Fi details.

**Only this model and firmware have been tested. This is not a universal Samsung unlock. A 5 GHz uplink and 6 GHz hotspot can coexist at the phone-interface level, but significant throughput loss remains unresolved.**

## Downloads

| File | Purpose |
| --- | --- |
| `s23-jp-6ghz-kernelsu-0.2-mcc-test.zip` | Configuration module for the KernelSU manager |
| `s23-wifi6menu-0.3-test.apk` | Settings module to install and enable in LSPosed |
| `s23-jp-6ghz-source-v0.1.0-experimental.zip` | Source, scripts and bilingual documentation; not an installable module |
| `SHA256SUMS.txt` | Download checksums |

Bundle release `v0.1.0-experimental` retains module version `0.2-mcc-test` and menu APK version `0.3-test`. The APK and hotspot helper DEX are the device-tested binaries. The public module ZIP adds installer model/build checks and permissions; installation on a second device has not been tested.

## Features

- Requests the JP Wi-Fi country code after boot. Does not automatically start a hotspot.
- Exposes 6 GHz in native Mobile Hotspot settings. Select 6 GHz and WPA3 and save manually; the user controls the SSID and password.
- Adds actual Wi-Fi band, frequency in MHz, primary channel and Tx/Rx negotiated link rates to network details, refreshing every two seconds while the page is active.
- Changes only `gWlanMccToSccSwitchMode` from `4` to `0` in the WLAN configuration to prevent forced hotspot channel migration when a 5 GHz uplink connects, enabling experimental MCC operation.
- Uses a reversible bind mount without writing the original vendor file. Model, build and original-file SHA-256 checks guard the MCC change at boot.
- Does not perform a factory reset, CSC flash or driver/radio firmware binary replacement.

## Installation and use

1. Match the exact model/build and have working KernelSU, a Zygisk environment and LSPosed. The tested environment used KernelSU + Zygisk + LSPosed IT 1.9.2; other managers are unverified. Rooting and bootloader unlocking are outside this project.
2. Install the module ZIP in KernelSU and reboot. It is neither an LSPosed package nor a recovery-flashable firmware ZIP.
3. Install the menu APK. In LSPosed, enable `S23 日本6GHz原生菜单` and scope it **only to Settings (`com.android.settings`)**. Restart Settings or reboot.
4. Open Settings → Connections → Mobile Hotspot and Tethering → Mobile Hotspot. Select **6 GHz / WPA3**, save, then enable the hotspot. Installation does not overwrite an existing SSID, password or saved band.
5. Enable Samsung's native Wi-Fi sharing when sharing a Wi-Fi uplink. 5+6 GHz concurrency can cause severe slowdown; first test the Wi-Fi connection with the hotspot off, then test hotspot operation separately.
6. Open the gear icon beside the connected Wi-Fi network to see the additional information. Negotiated link rates are not internet speed-test results. The current APK's added labels are in Chinese; these guides are bilingual.

The KernelSU Action button starts/stops a hotspot using the native saved configuration, preserves the Wi-Fi uplink and reports the actual frequency. It does not force a saved 2.4/5 GHz configuration to 6 GHz.

## Verification and limitations

| Item | Status |
| --- | --- |
| Native 6 GHz menu and Wi-Fi band/channel/rate rows | Device-tested and confirmed by the user |
| JP and saved 6 GHz/WPA3 configuration survive reboot | Verified |
| Phone connecting to a 6 GHz Wi-Fi network | User reported success on 2026-11-13; width and data throughput for that connection were not read |
| Actual hotspot operating frequency | Phone reported 5975 MHz, primary channel 5, 160 MHz width |
| 5 GHz uplink + 6 GHz hotspot | Phone interfaces and tethering upstream verified; performance remains unresolved |
| External 6E/7 client joining the phone hotspot and accessing the internet | No confirmed result yet |
| 320 MHz / Wi-Fi 7 unlock | **Not supported or provided** |
| Persistence across firmware updates / factory resets | Not guaranteed; build changes cause the guards to skip application |

6 GHz is a frequency band; 160 MHz is a channel width. Samsung specifies Wi-Fi 6E/HE160 for the S23 Ultra. Enabling 6 GHz does not add Wi-Fi 7/320 MHz capability. Use the configuration only where the actual local radio rules permit it; do not use a fixed JP override in a region that disallows it.

## Removal and rollback

Turn the hotspot off and select a locally supported conventional band/security mode in native Settings. Disable or uninstall the LSPosed APK, uninstall the KernelSU module, and reboot. The uninstall script cancels the country override. If merely disabling the module, an already-active country override may remain until reboot.

To disable only the MCC change while keeping JP and the menu, run `touch /data/adb/modules/s23_jp_6ghz/disable-mcc` as root, then reboot. Remove that marker and reboot to re-enable it. The Action button enables Wi-Fi sharing; uninstalling does not restore the user's sharing toggle or hotspot configuration automatically. Adjust these in native Settings.

## Source and release contents

`module/` contains the configuration module, `src/native-menu/` the Settings hooks and compile-only Xposed API stubs, and `src/hotspot/` the helper source. Never package the compile stubs into the APK. See [BUILDING.md](BUILDING.md).

No device serial, hotspot password/backup, SSID/BSSID, diagnostic logs, account credentials, signing private keys, stock Samsung APK/JAR or driver binaries are included. The included WLAN text configuration is the matching firmware configuration with only the MCC entry above changed. Installation does not wipe user data.

References: [Samsung S23 Ultra specifications](https://www.samsung.com/africa_en/smartphones/galaxy-s/galaxy-s23-ultra-cream-256gb-sm-s918bzecafc/) · [KernelSU module guide](https://kernelsu.org/guide/module.html)
