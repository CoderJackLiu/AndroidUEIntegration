# 虚幻引擎 AAR 集成指南

本文档说明如何将虚幻引擎（Unreal Engine）打包的 AAR 库集成到 Android 项目中。

## ⚠️ 前提条件

在开始集成之前，你需要先从虚幻引擎项目生成 AAR 文件。

### AAR 文件来源

AAR 文件是从虚幻引擎的 Android 打包项目生成的：

**虚幻引擎项目路径示例**:
```
E:\AndroidAPP\AndroidShipping1\Android\arm64\
```

### 如何生成 AAR？

#### 方法 1: 使用自动化脚本（推荐）

在虚幻引擎 Android 项目目录下运行：

```bash
cd E:\AndroidAPP\AndroidShipping1\Android
.\build-aar.bat
```

生成的 AAR 位置：
```
arm64/gradle/app/build/outputs/aar/app-release.aar
```

#### 方法 2: 手动构建

1. **修改 build.gradle**（备份原文件）

文件位置：`arm64/gradle/app/build.gradle`

```gradle
// 将这行：
apply plugin: 'com.android.application'

// 改为：
apply plugin: 'com.android.library'

// 并注释掉这行：
// applicationId PACKAGE_NAME
```

2. **构建 AAR**

```bash
cd arm64/gradle
gradlew :app:assembleRelease
```

3. **恢复原配置**（如果需要继续构建 APK）

```bash
.\restore-apk-mode.bat
```

### AAR 文件大小

生成的 AAR 文件约 **250-300MB**，因为包含了：
- 虚幻引擎 native 库（.so 文件）
- Java 代码和资源
- 所有必需的依赖

---

## 📋 集成步骤总览

1. 复制 AAR 文件和资源文件
2. 修改 `build.gradle.kts` 配置
3. 修改 `AndroidManifest.xml`
4. 添加必需的 Permission 类
5. 修改 MainActivity 添加启动功能
6. 构建并测试

---

## 步骤 1: 复制 AAR 文件

### 1.1 创建 libs 目录并复制 AAR

```bash
# 创建 libs 目录
mkdir -p app/libs

# 复制 AAR 文件
cp [虚幻引擎项目路径]/arm64/gradle/app/build/outputs/aar/app-release.aar app/libs/ue-library.aar
```

### 1.2 复制 assets 资源文件

```bash
# 创建 assets 目录
mkdir -p app/src/main/assets

# 复制虚幻引擎资源文件（包括 UECommandLine.txt 和 main.obb.png）
cp -r [虚幻引擎项目路径]/arm64/assets/* app/src/main/assets/
```

---

## 步骤 2: 修改 build.gradle.kts

### 2.1 更新 android 配置块

在 `app/build.gradle.kts` 文件中修改：

```kotlin
android {
    namespace = "com.yourcompany.mainapp3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yourcompany.mainapp3"
        minSdk = 29  // UE AAR 实际要求最低 29
        targetSdk = 34  // 保持兼容性
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 只支持 arm64-v8a 架构
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        
        // 启用 MultiDex
        multiDexEnabled = true
    }
    
    // ... 其他配置 ...
    
    // 添加 packaging 配置，处理 .so 文件冲突
    packaging {
        resources {
            pickFirsts += listOf(
                "lib/arm64-v8a/libUnreal.so",
                "lib/arm64-v8a/libpsoservice.so",
                "lib/arm64-v8a/libswappy.so"
            )
        }
    }
}
```

### 2.2 添加依赖项

在 `dependencies` 块中添加：

```kotlin
dependencies {
    // ==================== 虚幻引擎 AAR ====================
    implementation(files("libs/ue-library.aar"))
    
    // UE AAR 必需的依赖
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.6.1")
    
    // MultiDex 支持
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Google Play Services (UE AAR 需要)
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    
    // ==================== 原有依赖 ====================
    // ... 你的其他依赖 ...
}
```

---

## 步骤 3: 修改 AndroidManifest.xml

在 `app/src/main/AndroidManifest.xml` 中：

### 3.1 添加必需权限

在 `<manifest>` 标签内，`<application>` 标签之前添加：

```xml
<!-- UE AAR 必需的权限 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### 3.2 修改 application 标签

添加 `tools:replace` 属性：

```xml
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:theme="@style/Theme.MainApp3"
    tools:replace="android:icon,android:theme">
```

### 3.3 **重要：移除 SplashActivity 的启动器入口**

在 `</application>` 标签之前添加（确保只有一个应用图标）：

```xml
<!-- 注意：UE 的 Activities 和 Services 已经在 AAR 的 Manifest 中定义 -->

<!-- 移除 SplashActivity 的启动器入口，只保留内部调用 -->
<activity
    android:name="com.epicgames.unreal.SplashActivity"
    android:exported="false"
    tools:node="merge"
    tools:replace="android:exported">
    <intent-filter tools:node="remove">
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

---

## 步骤 4: 添加 Permission 辅助类

虚幻引擎需要 VR 权限辅助类。创建以下文件：

### 4.1 创建包目录

```bash
mkdir -p app/src/main/java/com/google/vr/sdk/samples/permission
```

### 4.2 创建 PermissionHelper.java

文件路径：`app/src/main/java/com/google/vr/sdk/samples/permission/PermissionHelper.java`

```java
package com.google.vr.sdk.samples.permission;

import android.app.Activity;
import android.util.Log;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import java.lang.reflect.Method;

public class PermissionHelper {
    private static final String LOG_TAG = "PermissionHelper";
    
    public static Activity getForegroundActivity() {
        Activity activity = null;
        try {
            Class<?> clazz = Class.forName("com.google.vr.sdk.samples.transition.GVRTransition2DActivity");
            Method m = clazz.getMethod("getActivity", new Class[] {});
            activity = (Activity)m.invoke(null);
        } catch (Exception e) {
            Log.e(LOG_TAG, "GVRTransition2DActivity.getActivity() failed. Trying to get GameActivity.");
        }
        if (activity != null) return activity;
        
        try {
            Class<?> clazz = Class.forName("com.epicgames.unreal.GameActivity");
            Method m = clazz.getMethod("Get", new Class[] {});
            return (Activity)m.invoke(null);
        } catch (Exception e) {
            Log.e(LOG_TAG, "GameActivity.Get() failed");
        }
        return null;
    }

    public static boolean checkPermission(String permission) {
        Activity activity = getForegroundActivity();
        if (activity==null) return false;
        if (ContextCompat.checkSelfPermission(activity, permission) ==
            PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "checkPermission: " + permission + " has granted");
            return true;
        } else {
            Log.d(LOG_TAG, "checkPermission: " + permission + " has not granted");
            return false;
        }
    }

    public static void acquirePermissions(final String permissions[]) {
        Activity activity = getForegroundActivity();
        PermissionHelper.acquirePermissions(permissions, activity);
    }

    public static void acquirePermissions(final String permissions[], Activity InActivity) {
        if (InActivity==null) return;
        final Activity activity = InActivity;
        activity.runOnUiThread(new Runnable(){
            public void run() {
                PermissionFragment fragment = PermissionFragment.getInstance(activity);
                if (fragment!=null) {
                    fragment.acquirePermissions(permissions);
                }
            }
        });
    }

    public static native void onAcquirePermissions(String permissions[], int grantResults[]);
}
```

### 4.3 创建 PermissionFragment.java

文件路径：`app/src/main/java/com/google/vr/sdk/samples/permission/PermissionFragment.java`

```java
package com.google.vr.sdk.samples.permission;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class PermissionFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1101;
    private static final String TAG = "PermissionFragment";
    private static final String PERMISSION_TAG = "TAG_PermissionFragment";

    public static PermissionFragment getInstance(Activity activity) {
        FragmentManager fm = activity.getFragmentManager();
        PermissionFragment fragment = (PermissionFragment)fm.findFragmentByTag(PERMISSION_TAG);
        if (fragment == null) {
            try {
                Log.d(TAG, "Creating PermissionFragment");
                fragment = new PermissionFragment();
                FragmentTransaction trans = fm.beginTransaction();
                trans.add(fragment, PERMISSION_TAG);
                trans.commit();
                fm.executePendingTransactions();
            } catch (Throwable th) {
                Log.e(TAG, "Cannot launch PermissionFragment:" + th.getMessage(), th);
                return null;
            }
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void acquirePermissions(String permissions[]) {
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode==PERMISSION_REQUEST_CODE && permissions.length>0) {
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                Log.d(TAG, "permission granted for " + permissions[0]);
            else
                Log.d(TAG, "permission not granted for " + permissions[0]);
            PermissionHelper.onAcquirePermissions(permissions, grantResults);
        }
    }
}
```

---

## 步骤 5: 修改 MainActivity

在你的 MainActivity 中添加启动虚幻引擎的功能。

### 5.1 添加 import

```kotlin
import android.content.Intent
import com.google.android.material.snackbar.Snackbar
```

### 5.2 修改按钮点击事件

例如，将浮动按钮（FAB）的点击事件改为启动虚幻引擎：

```kotlin
binding.appBarMain.fab.setOnClickListener { view ->
    // 启动虚幻引擎游戏
    launchUnrealEngine()
}
```

### 5.3 添加启动方法

在 MainActivity 类中添加：

```kotlin
private fun launchUnrealEngine() {
    try {
        // 启动虚幻引擎的 SplashActivity
        val intent = Intent()
        intent.setClassName(this, "com.epicgames.unreal.SplashActivity")
        startActivity(intent)
    } catch (e: Exception) {
        Snackbar.make(
            binding.root,
            "启动虚幻引擎失败: ${e.message}",
            Snackbar.LENGTH_LONG
        ).show()
        e.printStackTrace()
    }
}
```

---

## 步骤 6: 构建并测试

### 6.1 构建项目

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建并安装到设备
./gradlew installDebug
```

### 6.2 测试流程

1. 在设备上启动应用
2. 确认启动器中**只有一个应用图标**（MainApp3）
3. 点击启动应用图标，进入 MainActivity
4. 点击浮动按钮（FAB）
5. 应用会跳转到虚幻引擎的游戏界面
6. 按返回键可以返回 MainActivity

---

## ⚠️ 常见问题

### 问题 1: 出现两个应用图标

**原因：** AAR 的 AndroidManifest 中 SplashActivity 有 LAUNCHER intent-filter

**解决：** 确保步骤 3.3 正确添加了移除 SplashActivity 启动器入口的配置

### 问题 2: 编译错误 - NoClassDefFoundError: PermissionHelper

**原因：** 缺少 PermissionHelper 和 PermissionFragment 类

**解决：** 按照步骤 4 添加这两个类文件

### 问题 3: 编译错误 - compileSdk 版本冲突

**原因：** AndroidX 依赖要求 compileSdk 36，但 UE 可能需要较低版本

**解决：** 使用 `compileSdk = 36`，`targetSdk = 34`，`minSdk = 29` 的组合

### 问题 4: 运行时崩溃 - 找不到 .so 文件

**原因：** 多个依赖包含相同的 native 库

**解决：** 确保步骤 2.1 中的 `packaging` 配置正确

### 问题 5: Google Play Services 相关错误

**原因：** 缺少必需的 Google Play Services 依赖

**解决：** 确保步骤 2.2 中添加了所有 Google Play Services 依赖

---

## 📝 技术细节说明

### 应用架构

集成后的应用结构：

```
MainApp3 (一个 APK 应用)
├── MainActivity (主界面)
│   └── 点击按钮 → 启动 SplashActivity
├── com.epicgames.unreal.SplashActivity (来自 AAR)
│   └── 自动跳转到 GameActivity
└── com.epicgames.unreal.GameActivity (虚幻引擎游戏界面)
```

### SDK 版本要求

- **compileSdk**: 36（AndroidX 要求）
- **targetSdk**: 34（保持兼容性）
- **minSdk**: 29（UE AAR 实际要求）

### 文件大小

- AAR 文件大小：约 250-300MB（包含 .so 库）
- 最终 APK 大小：约 250-300MB

---

## 🎯 验证清单

集成完成后，请确认以下事项：

- [ ] 构建成功，无编译错误
- [ ] 设备上只显示一个应用图标
- [ ] 点击图标启动 MainActivity
- [ ] 点击按钮可以跳转到虚幻引擎界面
- [ ] 虚幻引擎界面正常显示
- [ ] 按返回键可以返回 MainActivity
- [ ] logcat 中可以看到 UE 相关日志

---

## 📞 支持

如果遇到问题，请检查：

1. 所有文件路径是否正确
2. AAR 文件是否完整（约 250MB）
3. assets 文件是否正确复制
4. AndroidManifest.xml 中的配置是否完整
5. Permission 类是否正确添加

生成的 APK 位置：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

---

**最后更新时间：** 2024-11-17

