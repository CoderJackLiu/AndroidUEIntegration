# MainApp3

一个集成了虚幻引擎（Unreal Engine）的 Android 应用示例项目。

## 📱 项目简介

MainApp3 是一个标准的 Android 导航抽屉（Navigation Drawer）应用，集成了虚幻引擎 5.6.1 的 AAR 库。用户可以在应用内通过点击按钮启动虚幻引擎游戏场景。

## ✨ 特性

- 🎮 **虚幻引擎集成** - 完整集成 UE 5.6.1 AAR 库
- 📱 **单应用架构** - 只有一个应用图标，虚幻引擎作为内部 Activity
- 🚀 **简单启动** - 点击浮动按钮即可启动游戏
- 🔙 **无缝切换** - 支持在主应用和游戏场景间切换
- 🏗️ **标准架构** - 基于 Android Navigation Component

## 📋 系统要求

- **Android 版本**: Android 10 (API 29) 及以上
- **架构支持**: ARM64-v8a
- **存储空间**: 至少 500MB 可用空间
- **内存**: 建议 4GB 以上

## 🛠️ 技术栈

- **开发语言**: Kotlin
- **最低 SDK**: 29 (Android 10)
- **目标 SDK**: 34 (Android 14)
- **编译 SDK**: 36
- **构建工具**: Gradle 8.13
- **虚幻引擎**: UE 5.6.1

### 主要依赖

```gradle
- Unreal Engine AAR (ue-library.aar)
- AndroidX Core KTX
- AndroidX Navigation
- Material Design Components
- Google Play Services (Base, Auth, Games)
- MultiDex Support
```

## 📦 项目结构

```
MainApp3/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/yourcompany/mainapp3/
│   │       │   ├── MainActivity.kt                 # 主界面
│   │       │   └── ui/                             # UI 组件
│   │       ├── java/com/google/vr/sdk/samples/permission/
│   │       │   ├── PermissionHelper.java           # 权限辅助类
│   │       │   └── PermissionFragment.java         # 权限 Fragment
│   │       ├── assets/
│   │       │   ├── UECommandLine.txt               # UE 启动参数
│   │       │   └── main.obb.png                    # UE 资源包 (250MB+)
│   │       ├── res/                                # 资源文件
│   │       └── AndroidManifest.xml                 # 清单文件
│   ├── libs/
│   │   └── ue-library.aar                          # 虚幻引擎 AAR (250MB+)
│   └── build.gradle.kts                            # 应用级构建配置
├── 虚幻引擎AAR集成指南.md                            # 详细集成文档
└── README.md                                        # 本文件
```

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone [your-repository-url]
cd MainApp3
```

### 2. 打开项目

使用 Android Studio 打开项目：
```
File -> Open -> 选择 MainApp3 目录
```

### 3. 同步依赖

Android Studio 会自动同步 Gradle 依赖，或手动执行：
```bash
./gradlew sync
```

### 4. 构建项目

```bash
# 构建 Debug 版本
./gradlew assembleDebug

# 构建并安装到设备
./gradlew installDebug
```

### 5. 运行应用

1. 连接 Android 设备或启动模拟器
2. 点击 Android Studio 的 Run 按钮
3. 应用启动后，点击右下角的**紫色浮动按钮（FAB）**
4. 即可进入虚幻引擎游戏场景

## 🎮 使用说明

### 启动虚幻引擎

1. 打开 MainApp3 应用
2. 在主界面点击右下角的浮动按钮（FAB 📧）
3. 应用会自动跳转到虚幻引擎游戏界面
4. 按设备的**返回键**可以退出游戏，回到主界面

### 导航功能

应用包含标准的导航抽屉，提供以下页面：
- **Home** - 主页
- **Gallery** - 图库
- **Slideshow** - 幻灯片

## 📝 集成说明

如果你想在其他项目中集成虚幻引擎 AAR，请参考：

📖 **[虚幻引擎AAR集成指南.md](./虚幻引擎AAR集成指南.md)**

该文档包含：
- ✅ 完整的集成步骤（7步）
- ✅ 详细的代码示例
- ✅ 常见问题解答
- ✅ 技术细节说明

## 🔧 构建配置

### Debug 构建

```bash
./gradlew assembleDebug
```

输出位置：`app/build/outputs/apk/debug/app-debug.apk`

### Release 构建

```bash
./gradlew assembleRelease
```

输出位置：`app/build/outputs/apk/release/app-release.apk`

**注意：** Release 构建需要配置签名密钥。

## 📱 APK 信息

- **包名**: `com.yourcompany.mainapp3`
- **版本号**: 1.0
- **APK 大小**: 约 250-300MB（包含虚幻引擎库）
- **最小 Android 版本**: Android 10 (API 29)

## ⚠️ 注意事项

1. **文件大小**
   - AAR 文件约 250MB，assets 中的 OBB 文件约 250MB
   - 最终 APK 大小约 250-300MB
   - 首次下载和安装需要较长时间

2. **设备要求**
   - 必须是 ARM64 架构设备
   - 不支持 x86 或 ARMv7 设备
   - 建议使用配置较高的设备以获得最佳性能

3. **权限要求**
   - 应用需要网络、存储等权限
   - 首次启动时可能需要授予权限

4. **开发注意**
   - 修改代码后需要重新构建
   - 虚幻引擎部分（AAR）无法在模拟器上运行
   - 必须使用真实的 ARM64 设备进行测试

## 🐛 常见问题

### 1. 应用在启动器中显示两个图标

**解决方案**: 这是因为 AAR 的 Manifest 配置问题。确保 `AndroidManifest.xml` 中正确配置了移除 SplashActivity 的启动器入口。

### 2. 点击按钮无法启动虚幻引擎

**解决方案**: 
- 检查 logcat 日志查看错误信息
- 确认 AAR 文件已正确放置在 `app/libs/` 目录
- 确认 assets 文件已正确复制

### 3. 编译错误 - NoClassDefFoundError

**解决方案**: 确保添加了 `PermissionHelper.java` 和 `PermissionFragment.java` 文件。

### 4. 虚幻引擎黑屏

**解决方案**: 
- 检查设备是否支持 Vulkan
- 确认 assets 文件是否完整
- 查看 logcat 中的 UE 日志

## 📊 性能优化建议

1. **首次加载优化**
   - 虚幻引擎首次加载需要较长时间
   - 可以添加加载进度提示

2. **内存管理**
   - 虚幻引擎占用内存较大
   - 建议在退出游戏时清理资源

3. **多 DEX 支持**
   - 项目已启用 MultiDex
   - 如果遇到方法数超限，无需额外配置

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

本项目仅供学习和参考使用。

虚幻引擎相关内容遵循 Epic Games 的许可协议。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 GitHub Issue
- 发送邮件至 [your-email@example.com]

## 🎯 版本历史

### v1.0.0 (2024-11-17)
- ✨ 初始版本
- ✅ 集成虚幻引擎 5.6.1 AAR
- ✅ 实现单应用图标架构
- ✅ 添加简单的启动功能

---

**最后更新**: 2024-11-17

**开发环境**: Android Studio Hedgehog | 2024.1.1

