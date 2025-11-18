# MainApp3

一个集成了虚幻引擎（Unreal Engine）的 Android 应用示例项目 - **采用双进程架构**。

## 项目简介

MainApp3 演示了如何将虚幻引擎 5.6.1 AAR 库集成到标准 Android 应用中，并通过**双进程架构**实现稳定的应用内 UE 集成。

**核心特性**：
- 完整集成 UE 5.6.1 AAR 库
- 单应用架构（只有一个图标）
- 点击按钮即可启动游戏
- 支持在主应用和游戏场景间流畅切换
- **双进程隔离**：UE 崩溃不影响主应用
- Android 原生 UI 覆盖层支持

## 架构亮点

### 双进程架构

项目采用创新的**双进程架构**，完美解决了 Native 游戏引擎集成的痛点：

```
主进程 (com.yourcompany.mainapp3)
  └── MainActivity（主界面）
  
UE 进程 (com.yourcompany.mainapp3:ue_process)
  └── CustomGameActivity（游戏界面 + UI 覆盖层）
```

**优势**：
- ✅ UE 退出时不会导致应用崩溃
- ✅ 从游戏返回主界面流畅自然
- ✅ 内存管理更优，UE 退出立即释放资源
- ✅ 主应用与 UE 完全隔离，稳定性大幅提升

详见：**[双进程架构实施说明.md](./双进程架构实施说明.md)**

## 系统要求

- Android 10 (API 29) 及以上
- ARM64-v8a 架构设备
- 至少 500MB 可用空间

## 快速开始

### 构建并安装

```bash
# 构建并安装到设备
./gradlew installDebug
```

### 使用方法

1. **启动应用** → 看到主界面
2. **点击右下角的浮动按钮（FAB）** → 进入虚幻引擎游戏界面
3. **点击顶部"返回主界面"按钮** → 返回主界面（不崩溃！）
4. 可重复步骤 2-3，每次都能正常切换

### 验证双进程架构

```bash
# 查看进程
adb shell ps | findstr mainapp3

# 预期输出（运行游戏时）：
# u0_a186  xxxxx  ...  com.yourcompany.mainapp3              # 主进程
# u0_a186  xxxxx  ...  com.yourcompany.mainapp3:ue_process  # UE进程
```

## 项目结构

```
MainApp3/
├── app/
│   ├── libs/
│   │   └── ue-library.aar                          # 虚幻引擎 AAR (250MB)
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── UECommandLine.txt                   # UE 启动参数
│   │   │   └── main.obb.png                        # UE 资源包 (250MB)
│   │   ├── java/com/yourcompany/mainapp3/
│   │   │   ├── MainActivity.kt                     # 主界面（主进程）
│   │   │   └── CustomGameActivity.kt               # 游戏界面 + UI 覆盖层（UE 进程）
│   │   ├── java/com/google/vr/sdk/samples/permission/
│   │   │   ├── PermissionHelper.java               # 权限辅助类
│   │   │   └── PermissionFragment.java             # 权限 Fragment
│   │   ├── res/layout/
│   │   │   └── overlay_game_ui.xml                 # Android UI 覆盖层布局
│   │   └── AndroidManifest.xml                     # 双进程配置
│   └── build.gradle.kts
├── 虚幻引擎AAR集成完整指南-双进程架构.md               # 完整技术文档（推荐）
├── 双进程架构实施说明.md                             # 双进程架构详解
├── 虚幻引擎AAR集成指南.md                             # 基础集成指南
├── Android_UI_覆盖层说明.md                          # UI 覆盖层实现
└── README.md
```

## 集成到其他项目

### AAR 来源

AAR 文件从虚幻引擎 Android 项目生成：

```
源项目: E:\AndroidAPP\AndroidShipping1\Android\
生成位置: arm64/gradle/app/build/outputs/aar/app-release.aar
```

### 完整集成步骤

**强烈推荐阅读**：**[虚幻引擎AAR集成完整指南-双进程架构.md](./虚幻引擎AAR集成完整指南-双进程架构.md)**

这是一份 1100+ 行的完整技术文档，包含：
- ✅ AAR 生成详细步骤
- ✅ Android 项目集成配置
- ✅ UI 覆盖层实现方法
- ✅ **双进程架构完整实施（核心解决方案）**
- ✅ 技术原理深入讲解（带架构图和流程图）
- ✅ 性能分析和优化建议
- ✅ 常见问题 Q&A

### 核心配置要点

#### 1. AndroidManifest.xml - 双进程配置

```xml
<!-- 主界面：运行在主进程 -->
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- 游戏界面：运行在独立进程（关键！）-->
<activity
    android:name=".CustomGameActivity"
    android:process=":ue_process"
    android:exported="false"
    android:launchMode="standard">
    <meta-data android:name="android.app.lib_name" android:value="Unreal" />
</activity>
```

#### 2. CustomGameActivity.kt - 跨进程返回

```kotlin
private fun onBackToMainClicked() {
    val intent = Intent(this, MainActivity::class.java).apply {
        // 跨进程启动的关键 Flags
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    startActivity(intent)
    finish()  // UE 进程退出不影响主进程
}
```

## 技术信息

**SDK 版本**:
- compileSdk: 36
- targetSdk: 34
- minSdk: 29

**架构**:
- 双进程架构（主进程 + UE 进程）
- ARM64-v8a only
- MultiDex 支持

**主要依赖**:
```kotlin
- ue-library.aar (虚幻引擎)
- androidx.lifecycle:lifecycle-extensions:2.2.0
- androidx.multidex:multidex:2.0.1
- com.google.android.gms:play-services-base:18.5.0
- com.google.android.gms:play-services-auth:21.2.0
- com.google.android.gms:play-services-games-v2:20.1.2
```

**APK 信息**:
- 包名: `com.yourcompany.mainapp3`
- 大小: 约 250-300MB
- 输出: `app/build/outputs/apk/debug/app-debug.apk`

**性能开销**（双进程 vs 单进程）:
- 额外内存: 约 40MB（虚拟机和系统开销）
- 启动延迟: 约 100-200ms（进程创建）
- 收益：**彻底解决崩溃问题，稳定性大幅提升**

## ⚠️ 注意事项

1. **AAR 文件约 250MB**，需要从虚幻引擎项目生成
2. **仅支持 ARM64 设备**，无法在模拟器上运行
3. **安装时间较长**，因为包含大型 native 库
4. **设备需支持 Vulkan** 以获得最佳性能
5. **双进程架构**：UE 和主应用运行在不同进程，内存占用会增加约 40MB

## 常见问题

### 架构相关

**Q: 为什么需要双进程架构？**  
A: 虚幻引擎的 Native 代码会调用 `exit(0)`，在单进程架构下会导致整个应用崩溃。双进程架构让 UE 运行在独立进程，`exit(0)` 只终止 UE 进程，主应用不受影响。

**Q: 双进程会不会显示两个应用图标？**  
A: 不会。只有 MainActivity 有 LAUNCHER intent-filter，用户只能看到一个应用图标。

**Q: 如何验证双进程是否生效？**  
A: 运行 `adb shell ps | findstr mainapp3`，应该看到两个进程：主进程和 `:ue_process`。

### 功能相关

**Q: 从 UE 返回主界面后崩溃怎么办？**  
A: 这正是双进程架构要解决的问题。确保 `AndroidManifest.xml` 中 `CustomGameActivity` 配置了 `android:process=":ue_process"`。

**Q: 点击"返回主界面"后退回到桌面？**  
A: 检查 `CustomGameActivity` 的 `onBackToMainClicked()` 是否正确使用了 `FLAG_ACTIVITY_NEW_TASK` 等 flags。

**Q: 出现两个应用图标？**  
A: 检查 `AndroidManifest.xml` 是否正确配置移除了 SplashActivity 的 LAUNCHER intent-filter。

### 开发调试

**Q: 如何调试双进程应用？**  
A: Android Studio → Run → Attach Debugger to Android Process → 选择要调试的进程（主进程或 ue_process）。

**Q: 如何查看 UE 进程的日志？**  
A: `adb logcat | findstr "ue_process"` 或在 Android Studio 的 Logcat 中过滤进程。

**Q: 编译错误 NoClassDefFoundError？**  
A: 确保添加了 `PermissionHelper.java` 和 `PermissionFragment.java`。

**Q: 虚幻引擎黑屏？**  
A: 检查设备是否支持 Vulkan，查看 logcat 中的 UE 日志。

## 推荐阅读顺序

1. **README.md**（本文档）- 快速了解项目
2. **[虚幻引擎AAR集成完整指南-双进程架构.md](./虚幻引擎AAR集成完整指南-双进程架构.md)** - 完整技术文档，强烈推荐！
3. **[双进程架构实施说明.md](./双进程架构实施说明.md)** - 双进程架构细节
4. **[Android_UI_覆盖层说明.md](./Android_UI_覆盖层说明.md)** - UI 覆盖层实现

## 项目价值

本项目解决了**虚幻引擎集成到 Android 应用的核心痛点**：

### 解决的问题
- ✅ **彻底消除崩溃**：UE Native 代码调用 `exit(0)` 导致的应用崩溃
- ✅ **实现流畅返回**：从 UE 界面返回主应用不再闪退到桌面
- ✅ **提升应用稳定性**：双进程隔离确保主应用不受 UE 异常影响
- ✅ **优化内存管理**：UE 退出后立即释放大量内存，主应用更流畅

### 技术突破
通过双进程架构，成功实现了 Android 应用与虚幻引擎的**稳定共存**，让开发者可以在原生 Android 应用中无缝集成高质量的 3D 游戏内容。

## 许可证

本项目仅供学习和参考使用。虚幻引擎内容遵循 Epic Games 许可协议。

---

**版本**: 2.0.0 (2025-11-18)  
**虚幻引擎**: UE 5.6.1  
**开发环境**: Android Studio Hedgehog | 2024.1.1  
**架构**: 双进程架构（Multi-Process Architecture）
