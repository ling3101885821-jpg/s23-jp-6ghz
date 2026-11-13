# Building / 构建说明

The published APK and `module/hotspot.dex` are the original device-tested artifacts. Rebuilds can differ because of debug metadata, tool versions and signing keys. No signing private key is published. APKs signed with another key cannot update the published APK in place.

发布 APK 与 `module/hotspot.dex` 沿用已测试文件。源码重编译可能因调试信息、工具版本及签名不同而产生不同哈希。不发布签名私钥；使用其他密钥构建的 APK 不能直接覆盖安装发布版本。

## Prerequisites / 依赖

- JDK with `javac`, Java 17 for Android tools.
- Android SDK `android.jar` containing the APIs referenced by the sources, and D8/R8.
- Apktool 2.12.1 and Android apksigner (or an equivalent APK signer).
- Xposed API stubs in `src/native-menu/stubs/` are compile-only; exclude them from DEX.

## Hotspot helper / 热点辅助程序

Compile `src/hotspot/HotspotProbe.java` against `android.jar` with Java source/target 8, then convert the generated class with D8 (`--min-api 33 --lib android.jar`). Name the resulting `classes.dex` file `hotspot.dex` inside the module. The helper uses reflection to call Samsung configuration and Android tethering APIs. Supported modes: `status`, `stop`, `start-saved`, and explicit temporary `start <ssid> <password>`. The Action script uses `start-saved` and never places the saved password on the command line.

使用 Java 8 source/target 和 android.jar 编译该类，再以 D8（min-api 33）生成 DEX。操作脚本调用 start-saved，沿用系统保存配置，不把保存的热点密码放入命令行。

## Settings APK / 设置 APK

1. Compile the Xposed stubs into a separate output directory.
2. Compile `src/native-menu/src/` against `android.jar` and those stubs.
3. Run D8 on only `local/s23/wifi6/*.class`, including generated inner classes. Do not pass the stubs.
4. Copy `src/native-menu/apk/` into a fresh build directory. Add the generated `classes.dex` there.
5. Build with Apktool and sign with your own key. Use an ASCII-only build-directory path if aapt2 fails on Unicode paths.

先单独编译 Xposed 桩，再编译模块类；D8 输入只含模块类。将 DEX 加入 apk 模板后使用 Apktool 打包并自行签名。桩类不应出现在最终 APK 中。

## Module ZIP / 模块 ZIP

ZIP the contents of `module/` at the archive root (not a parent directory), preserve LF line endings and use the KernelSU manager to install. Do not add generated status files, device backups, `active`/`disable` flags or credentials. `customize.sh` applies installation guards and file permissions.

将 module 目录内部文件直接放在 ZIP 根目录，保留 LF 换行。在 KernelSU 管理器安装；不要加入设备备份、运行状态或私密信息。菜单 APK 需要另行安装。
