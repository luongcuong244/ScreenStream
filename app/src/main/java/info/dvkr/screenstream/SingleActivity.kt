package info.dvkr.screenstream

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowMetrics
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import info.dvkr.screenstream.common.getLog
import info.dvkr.screenstream.common.module.StreamingModuleManager
import info.dvkr.screenstream.common.settings.AppSettings
import info.dvkr.screenstream.ui.ScreenStreamContent
import info.dvkr.screenstream.ui.theme.ScreenStreamTheme
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.android.ext.android.inject
import org.koin.compose.KoinContext
import kotlin.coroutines.cancellation.CancellationException


public class SingleActivity : AppUpdateActivity() {

    internal companion object {
        internal fun getIntent(context: Context): Intent = Intent(context, SingleActivity::class.java)
    }

    private val streamingModulesManager: StreamingModuleManager by inject(mode = LazyThreadSafetyMode.NONE)
    private val appSettings: AppSettings by inject(mode = LazyThreadSafetyMode.NONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        XLog.d(this@SingleActivity.getLog("onCreate", "Bug workaround: ${window.decorView}"))
        super.onCreate(savedInstanceState)

        setContent {
            KoinContext {
                ScreenStreamTheme {
                    ScreenStreamContent(updateFlow = updateFlow)
                }
            }
        }

        appSettings.data.map { it.nightMode }
            .distinctUntilChanged()
            .onEach { if (AppCompatDelegate.getDefaultNightMode() != it) AppCompatDelegate.setDefaultNightMode(it) }
            .launchIn(lifecycleScope)

        streamingModulesManager.selectedModuleIdFlow
            .onStart { XLog.d(this@SingleActivity.getLog("selectedModuleIdFlow.onStart")) }
            .onEach { moduleId ->
                if (streamingModulesManager.isActive(moduleId)) return@onEach
                XLog.i(this@SingleActivity.getLog("selectedModuleIdFlow.onEach:", "$moduleId"))
                streamingModulesManager.startModule(moduleId, this)
            }
            .catch {
                if (it is IllegalStateException) XLog.i(this@SingleActivity.getLog("selectedModuleIdFlow.catch: ${it.message}"), it)
                else throw it
            }
            .onCompletion { cause ->
                if (cause == null || cause is CancellationException) XLog.d(this@SingleActivity.getLog("selectedModuleIdFlow.onCompletion"))
                else XLog.e(this@SingleActivity.getLog("selectedModuleIdFlow.onCompletion: ${cause.message}"), cause)
            }
            .flowWithLifecycle(lifecycle, minActiveState = Lifecycle.State.RESUMED)
            .launchIn(lifecycleScope)

        // check overlay permission
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }

        // get screen size
        val metrics: WindowMetrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getSystemService(
                WindowManager::class.java
            ).currentWindowMetrics
        } else {
            TODO("VERSION.SDK_INT < R")
        }
        val width: Int = metrics.bounds.width()
        val height: Int = metrics.bounds.height()
        Log.d("SingleActivity", "Full screen size: $width x $height")

        // get display screen size
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels
        Log.d("SingleActivity", "Display screen size: $displayWidth x $displayHeight")

        // get status bar height
        val statusBarResourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (statusBarResourceId > 0) {
            resources.getDimensionPixelSize(statusBarResourceId)
        } else {
            0
        }
        Log.d("SingleActivity", "Status bar height: $statusBarHeight")

        // get navigation bar height
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        val navigationBarHeight = if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
        Log.d("SingleActivity", "Navigation bar height: $navigationBarHeight")
    }
}