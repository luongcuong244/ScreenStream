package info.dvkr.screenstream.webrtc

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.view.accessibility.AccessibilityEvent
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager

public class RemoteAccessibilityService : AccessibilityService() {

    private val clientClickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clickX = intent?.getIntExtra("clickX", 0) ?: 0
            val clickY = intent?.getIntExtra("clickY", 0) ?: 0
            drawCircle(clickX, clickY)
        }
    }

    // Create a custom View to draw a red circle
    private fun createCircleView(x: Int, y: Int): View {
        val circleView = object : View(applicationContext) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                val paint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                }
                val radius = 50f  // Radius of the circle
                canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint)
            }
        }
        circleView.layoutParams = FrameLayout.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        return circleView
    }

    private fun drawCircle(x: Int, y: Int) {
        val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams().apply {
            // Set the layout parameters for the overlay window
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.TOP or Gravity.LEFT
        }

        val circleView = createCircleView(x, y)
        windowManager.addView(circleView, params)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val intentFilter = IntentFilter().apply {
            addAction("info.dvkr.screenstream.webrtc.ClientClick")
        }
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(clientClickReceiver, intentFilter)

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent("info.dvkr.screenstream.webrtc.RemoteAccessibilityServiceConnected").apply {
            putExtra("clickX", 100)
            putExtra("clickY", 100)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No need to handle accessibility events for now
    }

    override fun onInterrupt() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(clientClickReceiver)
    }
}