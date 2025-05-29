package info.dvkr.screenstream.webrtc.internal

import android.content.Context
import info.dvkr.screenstream.webrtc.BuildConfig
import info.dvkr.screenstream.webrtc.utils.PreferenceUtils
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

internal class WebRtcEnvironment(context: Context) {
    internal val packageName: String = context.applicationContext.packageName
    internal val cloudProjectNumber: Long = BuildConfig.CLOUD_PROJECT_NUMBER.toLong()
    internal val signalingServerUrl: String =
        PreferenceUtils.getInstance(context).getServerUrl() ?: BuildConfig.SIGNALING_SERVER
    internal val noncePath: String = "/common/nonce"
    internal val socketPath: String = "/remote"
    internal val signalingServerHost: String = signalingServerUrl.toHttpUrl().host
    internal val signalingServerNonceUrl: HttpUrl = (signalingServerUrl + noncePath).toHttpUrl()
}