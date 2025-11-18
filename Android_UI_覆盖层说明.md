# Android UI 覆盖层实现说明

本文档说明如何在虚幻引擎游戏界面上添加 Android 原生 UI 控件。

## 实现概述

通过创建自定义的 `CustomGameActivity` 继承自 `GameActivity`，在虚幻引擎渲染的游戏画面上叠加 Android 原生 UI。

## 实现的功能

- ✅ 在游戏界面顶部添加半透明控制栏
- ✅ 显示游戏状态信息
- ✅ **"返回主界面"按钮** - 点击后退出游戏回到 MainActivity
- ✅ 不影响虚幻引擎的渲染和交互
- ✅ **双进程架构支持** - 安全返回主界面，不会崩溃

## 文件结构

```
app/src/main/
├── java/com/yourcompany/mainapp3/
│   ├── CustomGameActivity.kt          # 自定义的 GameActivity
│   └── MainActivity.kt                # 已更新启动逻辑
├── res/layout/
│   └── overlay_game_ui.xml            # 覆盖层 UI 布局
└── AndroidManifest.xml                # 已注册 CustomGameActivity
```

## 关键代码

### 1. CustomGameActivity.kt

```kotlin
class CustomGameActivity : GameActivity() {
    
    private var overlayView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 在 UE 界面创建后添加覆盖层
        window.decorView.post {
            addOverlayUI()
        }
    }

    private fun addOverlayUI() {
        // 加载覆盖层布局
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_game_ui, null)
        
        // 设置返回按钮
        overlayView?.findViewById<Button>(R.id.btnBackToMain)?.setOnClickListener {
            finish()  // 关闭当前 Activity，返回主界面
        }
        
        // 添加到内容视图
        addContentView(overlayView, ViewGroup.LayoutParams(...))
    }
}
```

### 2. overlay_game_ui.xml

```xml
<FrameLayout ...
    android:background="@android:color/transparent">
    
    <!-- 顶部控制栏 -->
    <LinearLayout
        android:layout_gravity="top"
        android:background="#80000000">  <!-- 半透明黑色 -->
        
        <TextView
            android:id="@+id/tvGameInfo"
            android:text="虚幻引擎游戏运行中" />
        
        <Button
            android:id="@+id/btnBackToMain"
            android:text="返回主界面" />
    </LinearLayout>
</FrameLayout>
```

### 3. AndroidManifest.xml

```xml
<!-- 注册自定义 Activity -->
<activity
    android:name=".CustomGameActivity"
    android:exported="false"
    android:launchMode="standard"
    android:theme="@style/Theme.MainApp3.NoActionBar"
    android:screenOrientation="sensorLandscape"
    android:configChanges="...">
    <meta-data android:name="android.app.lib_name" android:value="Unreal" />
</activity>

<!-- 重要：覆盖 UE 的 GameActivity 的 launchMode -->
<activity
    android:name="com.epicgames.unreal.GameActivity"
    android:exported="false"
    android:launchMode="standard"
    tools:node="merge"
    tools:replace="android:exported,android:launchMode">
    <intent-filter tools:node="remove">
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### 4. MainActivity.kt

```kotlin
private fun launchUnrealEngine() {
    // 启动自定义的 CustomGameActivity
    val intent = Intent(this, CustomGameActivity::class.java)
    startActivity(intent)
}
```

## 使用方法

1. **启动应用** - 打开 MainApp3
2. **点击 FAB 按钮** - 启动虚幻引擎
3. **查看覆盖层** - 游戏界面顶部会显示半透明控制栏
4. **点击"返回主界面"** - 退出游戏，回到 MainActivity

## UI 特点

### 布局设计

- **FrameLayout** - 作为根容器，透明背景
- **LinearLayout** - 顶部控制栏，半透明黑色背景 (#80000000)
- **TextView** - 显示游戏状态信息
- **Button** - 返回主界面按钮，粉色背景 (#FF4081)

### 位置安排

- 顶部：控制栏（信息 + 返回按钮）
- 中间：空白（虚幻引擎游戏画面）
- 底部：可扩展添加其他控件

## 交互流程

```
MainActivity
    ↓ 点击 FAB
CustomGameActivity (启动)
    ↓ onCreate
加载 UE 游戏引擎
    ↓
添加 Android UI 覆盖层
    ↓ 用户操作
点击"返回主界面"按钮
    ↓ finish()
返回 MainActivity
```

## 扩展方式

### 1. 添加更多控件

在 `overlay_game_ui.xml` 中添加：

```xml
<!-- 底部操作按钮 -->
<LinearLayout
    android:layout_gravity="bottom|end"
    android:layout_margin="16dp">
    
    <Button
        android:id="@+id/btnPause"
        android:text="暂停" />
    
    <Button
        android:id="@+id/btnSettings"
        android:text="设置" />
</LinearLayout>
```

在 `CustomGameActivity.kt` 中处理：

```kotlin
overlayView?.findViewById<Button>(R.id.btnPause)?.setOnClickListener {
    // 暂停游戏逻辑
}

overlayView?.findViewById<Button>(R.id.btnSettings)?.setOnClickListener {
    // 显示设置对话框
}
```

### 2. 显示/隐藏 UI

```kotlin
class CustomGameActivity : GameActivity() {
    
    fun showOverlay() {
        overlayView?.visibility = View.VISIBLE
    }
    
    fun hideOverlay() {
        overlayView?.visibility = View.GONE
    }
    
    fun toggleOverlay() {
        overlayView?.visibility = if (overlayView?.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}
```

### 3. 与 UE 通信

如果需要 Android UI 和虚幻引擎互相通信：

```kotlin
class CustomGameActivity : GameActivity() {
    
    companion object {
        // 从 UE C++ 调用的静态方法
        @JvmStatic
        fun updateScoreFromUE(score: Int) {
            // 更新 UI 显示的分数
        }
    }
    
    // 调用 UE 的 native 方法
    private external fun nativeOnButtonClick(buttonId: Int)
    
    private fun onCustomButtonClick() {
        // 通知 UE 按钮被点击
        nativeOnButtonClick(1)
    }
}
```

### 4. 添加对话框

```kotlin
private fun showGameMenu() {
    AlertDialog.Builder(this)
        .setTitle("游戏菜单")
        .setItems(arrayOf("继续游戏", "重新开始", "设置", "退出")) { _, which ->
            when (which) {
                0 -> dismissMenu()
                1 -> restartGame()
                2 -> showSettings()
                3 -> finish()
            }
        }
        .show()
}
```

## ⚠️ 注意事项

1. **触摸事件**
   - 覆盖层会拦截触摸事件
   - 透明区域的触摸会传递给 UE
   - 按钮区域的触摸由 Android 处理

2. **性能考虑**
   - 保持 UI 简洁，避免复杂动画
   - 使用透明或半透明背景减少绘制开销
   - 及时释放资源，避免内存泄漏

3. **屏幕方向**
   - 游戏默认横屏 (`sensorLandscape`)
   - UI 布局需要适配横屏显示
   - 考虑不同分辨率的适配

4. **生命周期**
   - 正确处理 Activity 生命周期
   - 在 `onDestroy()` 中清理资源
   - 避免持有 Context 引用导致内存泄漏

5. **⚠️ 重要：双进程架构解决 UE 退出问题**
   - 虚幻引擎的 `GameActivity` 在退出时会调用 `exit(0)`
   - 单进程架构下会导致整个应用崩溃
   - **解决方案：双进程架构**
     - ✅ CustomGameActivity 运行在独立进程（`:ue_process`）
     - ✅ UE 调用 `exit(0)` 只终止 UE 进程，主进程不受影响
     - ✅ 使用 `FLAG_ACTIVITY_NEW_TASK` 跨进程启动 MainActivity
     - ✅ 可以安全调用 `finish()`，UE 进程退出不影响主应用
   - 详见：**[双进程架构实施说明.md](./双进程架构实施说明.md)**

## 常见问题

### Q: UI 不显示？

A: 检查以下几点：
- 确认 `addOverlayUI()` 被调用
- 检查布局文件是否正确
- 使用 `window.decorView.post {}` 确保在主线程执行

### Q: 点击按钮无响应？

A: 确认：
- 按钮的 ID 是否正确
- `setOnClickListener` 是否正确设置
- 检查 logcat 日志是否有错误

### Q: UI 遮挡了游戏画面？

A: 调整布局：
- 使用 `layout_gravity` 控制位置
- 减小控件的高度和宽度
- 使用半透明背景 (`#80000000`)

### Q: 点击返回按钮后应用崩溃？

A: **已通过双进程架构完美解决！**

**问题原因**：
- UE 的 Native 代码在退出时调用 `exit(0)`
- 单进程架构下会终止整个应用进程

**解决方案**：
- ✅ 使用双进程架构（`android:process=":ue_process"`）
- ✅ UE 运行在独立进程，`exit(0)` 只终止 UE 进程
- ✅ 主进程和 MainActivity 完全不受影响
- ✅ 可以安全调用 `finish()`，流畅返回主界面

详见：**[双进程架构实施说明.md](./双进程架构实施说明.md)**

### Q: 如何验证双进程架构是否生效？

A: 运行以下命令查看进程：

```bash
adb shell ps | findstr mainapp3
```

预期输出（运行游戏时）：
```
u0_a186  xxxxx  ...  com.yourcompany.mainapp3              # 主进程
u0_a186  xxxxx  ...  com.yourcompany.mainapp3:ue_process  # UE进程
```

### Q: 如何在 UE 中调用 Android 方法？

A: 使用 JNI 调用：
```cpp
// UE C++ 代码
#if PLATFORM_ANDROID
extern "C" {
    void CallAndroidMethod() {
        // 获取 JNI 环境
        JNIEnv* Env = FAndroidApplication::GetJavaEnv();
        // 调用 Java 方法
        ...
    }
}
#endif
```

## 测试建议

- ✅ 测试按钮点击功能
- ✅ 测试返回主界面流程（双进程架构下应流畅返回）
- ✅ 测试不同屏幕分辨率
- ✅ 测试长时间运行是否有内存泄漏
- ✅ 测试与 UE 游戏交互是否正常
- ✅ 测试多次进入/退出 UE 界面的稳定性

---

**更新时间**: 2025-11-18  
**适用版本**: MainApp3 v2.0.0  
**架构**: 双进程架构（Multi-Process Architecture）

