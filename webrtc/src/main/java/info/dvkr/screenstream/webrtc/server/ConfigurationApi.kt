package info.dvkr.screenstream.webrtc.server

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

public interface ConfigurationApi {
    @GET("/common/nonce")
    public fun getNonce(): Call<String>
}