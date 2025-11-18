package com.yourcompany.mainapp3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.epicgames.unreal.GameActivity

/**
 * 自定义的 GameActivity，运行在独立的 :ue_process 进程中
 * 
 * 双进程架构的优势：
 * - UE 调用 exit(0) 只会终止 :ue_process 进程
 * - 主进程和 MainActivity 完全不受影响
 * - 可以安全地在 UE 和主界面之间切换
 */
class CustomGameActivity : GameActivity() {

    private var overlayView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 延迟添加覆盖层，确保 UE 界面已创建
        window.decorView.post {
            addOverlayUI()
        }
    }

    private fun addOverlayUI() {
        try {
            // 从布局文件加载覆盖层
            val inflater = LayoutInflater.from(this)
            overlayView = inflater.inflate(R.layout.overlay_game_ui, null)

            // 设置返回按钮点击事件
            overlayView?.findViewById<Button>(R.id.btnBackToMain)?.setOnClickListener {
                onBackToMainClicked()
            }

            // 设置测试按钮 1 点击事件
            overlayView?.findViewById<Button>(R.id.btnTest1)?.setOnClickListener {
                Toast.makeText(this, "测试按钮 1 被点击了！", Toast.LENGTH_SHORT).show()
            }

            // 设置测试按钮 2 点击事件
            overlayView?.findViewById<Button>(R.id.btnTest2)?.setOnClickListener {
                Toast.makeText(this, "测试按钮 2 被点击了！这是蓝色按钮", Toast.LENGTH_SHORT).show()
            }

            // 设置测试按钮 3 点击事件
            overlayView?.findViewById<Button>(R.id.btnTest3)?.setOnClickListener {
                Toast.makeText(
                    this, 
                    "双进程架构测试成功！\nUE 进程：${android.os.Process.myPid()}", 
                    Toast.LENGTH_LONG
                ).show()
            }

            // 将覆盖层添加到内容视图
            addContentView(
                overlayView,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onBackToMainClicked() {
        try {
            // 在独立进程中，可以安全地启动主进程的 MainActivity
            // UE 进程即使崩溃也不影响主进程
            val intent = Intent(this, MainActivity::class.java).apply {
                // 使用 NEW_TASK 跨进程启动
                // CLEAR_TOP 确保清理 MainActivity 之上的所有 Activity
                // SINGLE_TOP 避免创建多个 MainActivity 实例
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            
            // 现在可以安全地 finish()
            // 因为即使 UE 调用 exit()，只会终止 :ue_process 进程
            // 主进程和 MainActivity 不受影响
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        overlayView = null
        super.onDestroy()
    }
    
    /**
     * 拦截返回键，调用返回主界面逻辑
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        onBackToMainClicked()
    }
}
