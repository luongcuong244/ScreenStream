package info.dvkr.screenstream.webrtc.utils

import android.content.Context

public class PreferenceUtils(context: Context) {
    public companion object {
        private const val PREF_NAME = "screen_stream_prefs"
        private const val KEY_FIRST_RUN = "first_run"
        private const val KEY_LAST_VERSION = "last_version"

        private var instance: PreferenceUtils? = null

        public fun getInstance(context: Context): PreferenceUtils {
            if (instance == null) {
                instance = PreferenceUtils(context)
            }
            return instance as PreferenceUtils
        }

        public fun getInstanceOrNull(): PreferenceUtils? {
            return instance
        }
    }

    private val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor = preferences.edit()

    public fun getServerUrl(): String? {
        return preferences.getString("server_url", null)
    }

    public fun setServerUrl(url: String?) {
        editor.putString("server_url", url)
        editor.apply()
    }

    public fun getDeviceId(): String? {
        return preferences.getString("device_id", null)
    }

    public fun setDeviceId(deviceId: String?) {
        editor.putString("device_id", deviceId)
        editor.apply()
    }
}