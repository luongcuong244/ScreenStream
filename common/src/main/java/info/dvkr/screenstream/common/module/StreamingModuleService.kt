package info.dvkr.screenstream.common.module

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.ServiceCompat
import com.elvishew.xlog.XLog
import info.dvkr.screenstream.common.getLog
import info.dvkr.screenstream.common.isPermissionGranted
import info.dvkr.screenstream.common.notification.NotificationHelper
import org.koin.android.ext.android.inject
import java.util.UUID

public abstract class StreamingModuleService : Service() {

    protected abstract val notificationIdForeground: Int
    protected abstract val notificationIdError: Int

    protected val streamingModuleManager: StreamingModuleManager by inject(mode = LazyThreadSafetyMode.NONE)
    protected val notificationHelper: NotificationHelper by inject(mode = LazyThreadSafetyMode.NONE)

    protected val processedIntents: MutableSet<String> = mutableSetOf()

    protected companion object {
        public const val INTENT_ID: String = "info.dvkr.screenstream.intent.ID"

        public fun Intent.addIntentId(): Intent = putExtra(INTENT_ID, UUID.randomUUID().toString())
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        XLog.d(getLog("onCreate"))
    }

    override fun onDestroy() {
        stopForeground()
        hideErrorNotification()
        super.onDestroy()
    }

    protected fun isDuplicateIntent(intent: Intent): Boolean {
        val id = intent.getStringExtra(INTENT_ID)
        return when {
            id == null -> {
                XLog.w(getLog("isDuplicateIntent", "No intent ID provided"))
                false
            }
            processedIntents.contains(id) -> {
                XLog.w(getLog("isDuplicateIntent", "Duplicate intent ID: $id"))
                true
            }
            else -> {
                processedIntents.add(id)
                false
            }
        }
    }

    @SuppressLint("InlinedApi")
    protected fun startForeground(stopIntent: Intent) {
        val notification = notificationHelper.createForegroundNotification(this, stopIntent)
        val serviceType = when {
            isPermissionGranted(Manifest.permission.RECORD_AUDIO) -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
            else -> ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        }

        ServiceCompat.startForeground(this, notificationIdForeground, notification, serviceType)

        // open accessibility service settings
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    public fun stopForeground() {
        XLog.d(getLog("stopForeground"))

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    protected fun showErrorNotification(message: String, recoverIntent: Intent) {
        hideErrorNotification()

        if (notificationHelper.notificationPermissionGranted(this).not()) {
            XLog.e(getLog("showErrorNotification", "No permission granted. Ignoring."))
            return
        }

        if (notificationHelper.errorNotificationsEnabled().not()) {
            XLog.e(getLog("showErrorNotification", "Notifications disabled. Ignoring."))
            return
        }

        val notification = notificationHelper.getErrorNotification(this, message, recoverIntent)
        notificationHelper.showNotification(notificationIdError, notification)
    }

    public fun hideErrorNotification() {
        XLog.d(getLog("hideErrorNotification"))

        notificationHelper.cancelNotification(notificationIdError)
    }
}