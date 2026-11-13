# Samsung S23 Ultra 日本 6 GHz Wi-Fi 与热点插件

[English](README.en.md) · [下载安装包](https://github.com/ling3101885821-jpg/s23-jp-6ghz/releases/tag/v0.1.0-experimental)

为 **SM-S9180、Android 16 / One UI 8、`S9180ZCU6EYI6_YanYu_Moded`** 制作的实验性插件组合：通过 KernelSU 保留 JP 无线地区配置，通过 LSPosed 在三星原生设置中显示 6 GHz 热点选项，并显示当前 Wi-Fi 的真实频段、主信道和收发协商速率。

**仅验证上述机型与固件，不是全系列三星通用解锁包。5 GHz 上游＋6 GHz 热点可以在手机侧同时运行，但存在明显降速，尚未解决。**

## 下载

| 文件 | 用途 |
| --- | --- |
| `s23-jp-6ghz-kernelsu-0.2-mcc-test.zip` | 在 KernelSU 管理器中安装的配置模块 |
| `s23-wifi6menu-0.3-test.apk` | 安装后在 LSPosed 中启用的设置菜单插件 |
| `s23-jp-6ghz-source-v0.1.0-experimental.zip` | 发布源码、脚本与中英文文档；不是刷入包 |
| `SHA256SUMS.txt` | 下载文件校验值 |

组合发布版本为 `v0.1.0-experimental`，内部沿用已测试模块 `0.2-mcc-test` 和菜单 APK `0.3-test`。APK 和热点辅助 DEX 沿用实机测试文件；公开 ZIP 新增了安装时机型检查和权限设置，尚未在第二台设备验证安装。

## 功能

- 开机后请求 JP 无线地区码，不自动开启热点。
- 原生移动热点页面显示 6 GHz 选项；须自行选择 6 GHz、WPA3 并保存，名称和密码由用户设置。
- Wi-Fi 网络详情页显示实际频段、频率（MHz）、主信道、Tx/Rx 协商速率；页面活跃时每 2 秒刷新。
- 驱动配置仅将 `gWlanMccToSccSwitchMode` 从 `4` 改为 `0`，避免连接 5 GHz 上游后强制迁移热点信道，允许 MCC 多信道并发试验。
- 使用可撤销 bind mount，不写入 vendor 原文件；开机检查机型、固件和原文件 SHA-256，不匹配则跳过 MCC 修改。
- 不包含恢复出厂、CSC 刷写或驱动／无线固件二进制替换步骤。

## 安装与使用

1. 确认机型、固件完全匹配，并已有正常工作的 KernelSU、Zygisk 环境和 LSPosed。实测环境为 KernelSU＋Zygisk＋LSPosed IT 1.9.2；其他管理器未验证。本项目不提供 Root 或引导程序解锁流程。
2. 在 KernelSU 管理器中安装模块 ZIP，重启手机。ZIP 不能安装到 LSPosed，也不是 Recovery 刷机包。
3. 安装菜单 APK，在 LSPosed → 模块中启用「S23 日本6GHz原生菜单」，作用域**仅勾选设置 `com.android.settings`**，然后重启设置应用或手机。
4. 进入设置 → 连接 → 移动热点和网络共享 → 移动热点，选择 **6 GHz／WPA3**，保存并开启。插件不会自动改写已有热点名称、密码或保存频段。
5. 需要共享 Wi-Fi 时开启三星原生「Wi-Fi 共享」。5＋6 GHz 并发仍可能严重降速；建议先在关闭热点时完成 Wi-Fi 连接测试，再分别验证热点。
6. Wi-Fi → 已连接网络右侧齿轮，可查看新增的频段、信道及速率信息。协商速率不等于互联网测速速度。

KernelSU 的「操作」按钮可按原生保存配置启动／停止热点，保留 Wi-Fi 连接，并报告实际频率。它不会强制将保存的 2.4／5 GHz 配置改成 6 GHz。

## 验证范围与限制

| 项目 | 状态 |
| --- | --- |
| 原生 6 GHz 菜单、Wi-Fi 频段／信道／速率显示 | 实机与用户确认 |
| JP 和保存的 6 GHz／WPA3 配置跨重启保留 | 已验证 |
| 手机连接 6 GHz Wi-Fi | 用户于 2026-11-13 报告实测连接成功；该次连接带宽与数据吞吐未读取 |
| 热点实际运行频率 | 手机报告 5975 MHz、主信道 5、160 MHz |
| 5 GHz 上游＋6 GHz 热点 | 手机侧接口和共享上游验证通过；性能问题未解决 |
| 外部 6E／7 客户端连接手机热点并上网 | 尚未获得验证结果 |
| 320 MHz／Wi-Fi 7 解锁 | **不支持，本项目不提供此功能** |
| 跨固件升级／恢复出厂后保留 | 不保证；固件变化时保护检查会停止应用 |

6 GHz 是频段，160 MHz 是信道带宽。S23 Ultra 官方规格为 Wi-Fi 6E／HE160，不能因开启 6 GHz 就视为获得 Wi-Fi 7／320 MHz。只在实际使用地区允许相应无线配置时使用；不要将固定 JP 配置用于不允许该配置的地区。

## 撤销

关闭热点，在原生设置中选择当地支持的常规频段和安全方式；在 LSPosed 中禁用或卸载菜单 APK，在 KernelSU 中卸载配置模块，然后重启。卸载脚本取消无线地区覆盖。禁用模块而不卸载时，已运行的地区覆盖可能要等重启后才消失。

只撤销 MCC 并发设置、保留 JP 与菜单：在 root 终端执行 `touch /data/adb/modules/s23_jp_6ghz/disable-mcc` 后重启。重新启用时删除这个标记并重启。模块操作按钮开启 Wi-Fi 共享后，卸载不会自动还原用户的 Wi-Fi 共享开关或热点配置，请在原生设置中调整。

## 源码与发布内容

`module/` 是配置模块，`src/native-menu/` 是设置插件源码及仅编译用的 Xposed API 桩，`src/hotspot/` 是热点辅助程序源码。编译桩不得打入 APK。详见 [BUILDING.md](BUILDING.md)。

发布包不包含手机序列号、热点密码／备份、SSID／BSSID、诊断日志、账号凭据、签名私钥、三星原生 APK/JAR 或驱动二进制。包含的 WLAN 文本配置为这版固件的配置副本，仅修改上述 MCC 项。安装过程不清空用户数据。

参考：[Samsung S23 Ultra 规格](https://www.samsung.com/africa_en/smartphones/galaxy-s/galaxy-s23-ultra-cream-256gb-sm-s918bzecafc/) · [KernelSU 模块说明](https://kernelsu.org/guide/module.html)
