# 问题排查与解决：返回桌面而非 MainActivity

## 🐛 问题描述

点击"返回主界面"按钮后，应用返回到桌面，而不是显示 MainActivity。

## 🔍 问题分析过程

### 1. 初步怀疑

用户怀疑可能是虚幻引擎打包出来的程序导致的问题 ✅ **正确！**

### 2. 排查步骤

1. **查看 AAR 的 AndroidManifest.xml**
   ```bash
   cd E:\AndroidAPP\MainApp\MainApp3\app\libs
   Copy-Item "ue-library.aar" "ue-library-temp.zip" -Force
   Expand-Archive -Path "ue-library-temp.zip" -DestinationPath "ue-temp" -Force
   Get-Content "ue-temp\AndroidManifest.xml"
   ```

2. **发现关键配置**
   ```xml
   <activity
       android:name="com.epicgames.unreal.GameActivity"
       android:launchMode="singleTask"  ← 问题所在！
       ...
   </activity>
   ```

### 3. 根本原因

**`android:launchMode="singleTask"` 的行为**：

- **创建独立任务栈**：`singleTask` Activity 会在自己独立的任务栈中运行
- **任务栈隔离**：
  ```
  任务栈 1: MainActivity (标准任务栈)
  任务栈 2: CustomGameActivity (singleTask 任务栈)
  ```
- **返回行为**：
  - 从 `CustomGameActivity` 启动 `MainActivity` → MainActivity 在任务栈 1 显示
  - 用户看到的是 MainActivity（正确）
  - 但当用户按返回键时：
    - 系统检查任务栈 1 → MainActivity 是栈底，没有可返回的 Activity
    - 系统返回到桌面（错误）
    - CustomGameActivity 仍在任务栈 2 中（但用户看不到）

## ✅ 解决方案

### 核心：覆盖 `launchMode` 为 `standard`

在 `MainApp3/app/src/main/AndroidManifest.xml` 中：

```xml
<!-- 1. CustomGameActivity 显式设置为 standard -->
<activity
    android:name=".CustomGameActivity"
    android:exported="false"
    android:launchMode="standard"  ← 关键！
    android:theme="@style/Theme.MainApp3.NoActionBar"
    android:screenOrientation="sensorLandscape"
    android:configChanges="mcc|mnc|uiMode|density|screenSize|smallestScreenSize|screenLayout|orientation|keyboardHidden|keyboard|navigation|touchscreen|locale|fontScale|layoutDirection">
    <meta-data android:name="android.app.lib_name" android:value="Unreal" />
</activity>

<!-- 2. 覆盖 UE 的 GameActivity 的 launchMode -->
<activity
    android:name="com.epicgames.unreal.GameActivity"
    android:exported="false"
    android:launchMode="standard"  ← 覆盖 AAR 中的 singleTask
    tools:node="merge"
    tools:replace="android:exported,android:launchMode">  ← 注意添加 launchMode 到 replace
    <intent-filter tools:node="remove">
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### 标准任务栈行为

使用 `launchMode="standard"` 后：

```
任务栈（统一）:
├── MainActivity (栈底)
├── CustomGameActivity
└── (用户返回时) → MainActivity (正确！)
```

## 📊 Android Launch Mode 对比

| Launch Mode | 行为 | 适用场景 | UE 使用情况 |
|------------|------|---------|------------|
| **standard** | 每次启动都创建新实例，在调用者的任务栈中 | 普通 Activity | ✅ 我们需要这个 |
| **singleTop** | 如果实例在栈顶则复用，否则新建 | 通知页面 | - |
| **singleTask** | 独立任务栈，单例模式 | 主页面 | ❌ UE 默认使用，导致问题 |
| **singleInstance** | 独立任务栈，且栈中只有这一个 Activity | 特殊场景 | - |

## 🎯 关键要点

### ✅ 正确做法

1. **明确设置 `CustomGameActivity` 的 `launchMode="standard"`**
2. **覆盖 `GameActivity` 的 `launchMode` 为 `standard`**
3. **使用正确的 Intent 标志**：
   ```kotlin
   val intent = Intent(this, MainActivity::class.java).apply {
       flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
   }
   startActivity(intent)
   ```
4. **不调用 `finish()`**（避免触发 UE 的 `System.exit(0)`）

### ❌ 错误做法

1. ❌ 保留 UE 的 `singleTask` 配置
2. ❌ 使用 `Intent.FLAG_ACTIVITY_NEW_TASK`（会创建新任务栈）
3. ❌ 使用 `moveTaskToBack()`（会把整个任务移到后台）
4. ❌ 调用 `finish()`（会触发 `System.exit(0)` 导致崩溃）

## 📝 其他虚幻引擎集成项目注意事项

如果你要在其他项目中集成虚幻引擎 AAR，**必须**检查并覆盖以下配置：

### 1. Launch Mode
```xml
<activity
    android:name="com.epicgames.unreal.GameActivity"
    android:launchMode="standard"
    tools:replace="android:launchMode">
</activity>
```

### 2. Exported
```xml
<activity
    android:name="com.epicgames.unreal.SplashActivity"
    android:exported="false"
    tools:replace="android:exported">
</activity>
```

### 3. Launcher Intent Filter
```xml
<intent-filter tools:node="remove">
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
```

## 🧪 测试验证

### 测试流程

1. ✅ 启动应用 → 显示 MainActivity
2. ✅ 点击 FAB 按钮 → 启动 CustomGameActivity（UE 游戏）
3. ✅ 点击"返回主界面"按钮 → 返回到 MainActivity（不是桌面）
4. ✅ 应用不崩溃
5. ✅ 可以重复步骤 2-3

### 验证命令

```bash
# 查看当前任务栈
adb shell dumpsys activity activities | findstr "com.yourcompany.mainapp3"

# 应该看到所有 Activity 在同一个 Task 中
```

## 📚 参考资料

- [Android Developer - Tasks and Back Stack](https://developer.android.com/guide/components/activities/tasks-and-back-stack)
- [Android Developer - Activity Launch Modes](https://developer.android.com/guide/topics/manifest/activity-element#lmode)
- [Unreal Engine Android Development](https://docs.unrealengine.com/5.0/en-US/android-game-development-in-unreal-engine/)

## 🎉 总结

**问题本质**：虚幻引擎 AAR 默认使用 `singleTask` 启动模式，导致独立任务栈，返回时回到桌面。

**解决关键**：在主应用的 `AndroidManifest.xml` 中覆盖 UE 的 `launchMode` 为 `standard`，确保所有 Activity 在同一个任务栈中。

---

**更新时间**: 2025-11-17  
**适用版本**: MainApp3 v1.0.0  
**虚幻引擎**: UE 5.6.1

