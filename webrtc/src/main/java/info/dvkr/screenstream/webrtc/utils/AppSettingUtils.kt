package info.dvkr.screenstream.webrtc.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast

public object AppSettingUtils {
    public fun getBaseUrl(context: Context) {
        val uri = Uri.parse("content://com.example.kmamdm.appsettingprovider/anything")
        val cursor = context.contentResolver.query(uri, null, null, null, null)

        if (cursor != null) {
            try {
                var foundUrl = false
                while (cursor.moveToNext()) {
                    val attribute = cursor.getString(0)
                    val value = cursor.getString(1)
                    val comment = cursor.getString(2)
                    Log.d("MainActivity", "Attribute: $attribute - Value: $value - Comment: $comment")

                    if (attribute == "remote_server_url") {
                        foundUrl = true
                        PreferenceUtils.getInstance(context).setServerUrl(value)
                        break
                    }
                }
                if (!foundUrl) {
                    Toast.makeText(context, "Không tìm thấy URL hợp lệ trong dữ liệu.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Lỗi khi đọc dữ liệu từ ContentProvider", e)
                Toast.makeText(context, "Đã xảy ra lỗi khi đọc dữ liệu.", Toast.LENGTH_SHORT).show()
            } finally {
                cursor.close()
            }
        } else {
            Toast.makeText(context, "Không lấy được dữ liệu. Cursor trả về null.", Toast.LENGTH_SHORT).show()
        }
    }
}