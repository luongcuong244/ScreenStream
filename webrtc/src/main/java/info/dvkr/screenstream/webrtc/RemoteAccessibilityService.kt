package info.dvkr.screenstream.webrtc

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager


public class RemoteAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var params: WindowManager.LayoutParams? = null

    private val clientClickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "info.dvkr.screenstream.webrtc.ClientClick" -> handleClientClick(intent)
                "info.dvkr.screenstream.webrtc.ClientSwipe" -> handleClientSwipe(intent)
            }
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
                val radius = 15f  // Radius of the circle
                canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint)
            }
        }
        circleView.layoutParams = FrameLayout.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        return circleView
    }

    private fun drawCircle(x: Int, y: Int) {
        val circleView = createCircleView(x, y)
        windowManager?.addView(circleView, params)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("RemoteAccessibilityService", "onServiceConnected")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, // This is important to draw over status bar and bottom nav
                PixelFormat.TRANSPARENT
            )
        } else {
            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSPARENT
            )
        }
        params?.gravity = Gravity.TOP or Gravity.LEFT

        val intentFilter = IntentFilter().apply {
            addAction("info.dvkr.screenstream.webrtc.ClientClick")
            addAction("info.dvkr.screenstream.webrtc.ClientSwipe")
        }
        LocalBroadcastManager.getInstance(applicationContext)
            .registerReceiver(clientClickReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("RemoteAccessibilityService", "onStartCommand")
        params?.gravity = Gravity.TOP or Gravity.LEFT
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.d("RemoteAccessibilityService", "onAccessibilityEvent: ${event.toString()}")
        // No need to handle accessibility events for now
    }

    override fun onInterrupt() {
        Log.d("RemoteAccessibilityService", "onInterrupt")
        LocalBroadcastManager.getInstance(applicationContext)
            .unregisterReceiver(clientClickReceiver)
    }

    private  fun handleClientClick(intent: Intent?) {
        val clickX = intent?.getDoubleExtra("clickX", 0.0) ?: 0.0
        val clickY = intent?.getDoubleExtra("clickY", 0.0) ?: 0.0
        Log.d("RemoteAccessibilityService", "Received click at ($clickX, $clickY)")
        // drawCircle(clickX, clickY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            simulateClick(clickX.toFloat(), clickY.toFloat())
        }
    }

    private fun handleClientSwipe(intent: Intent?) {
        val touchStartX = intent?.getDoubleExtra("touchStartX", 0.0) ?: 0.0
        val touchStartY = intent?.getDoubleExtra("touchStartY", 0.0) ?: 0.0
        val touchEndX = intent?.getDoubleExtra("touchEndX", 0.0) ?: 0.0
        val touchEndY = intent?.getDoubleExtra("touchEndY", 0.0) ?: 0.0
        val duration = intent?.getLongExtra("duration", 0L) ?: 0L
        Log.d("RemoteAccessibilityService", "Received swipe from ($touchStartX, $touchStartY) to ($touchEndX, $touchEndY) with duration $duration")
        // drawCircle(touchStartX, touchStartY)
        // drawCircle(touchEndX, touchEndY)
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        //     simulateSwipe(touchStartX.toFloat(), touchStartY.toFloat(), touchEndX.toFloat(), touchEndY.toFloat(), duration)
        // }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun simulateClick(x: Float, y: Float) {
        // Create a Path object for the gesture
        val path = Path()
        path.moveTo(x, y) // Move to the (x, y) position to simulate a tap

        // Build the gesture description (tap gesture)
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(StrokeDescription(path, 0, 100)) // Tap duration is 100ms

        // Create the gesture description object
        val gesture = gestureBuilder.build()

        // Dispatch the gesture (this will simulate the click)
        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                // Gesture completed successfully
                // You can log or handle any actions here
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                // Gesture was cancelled (e.g., if something went wrong)
            }
        }, null)
    }
}