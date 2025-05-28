package info.dvkr.screenstream.webrtc.server

import retrofit2.Callback

public object ConfigurationRepository {

    private fun getApi(): ConfigurationApi {
        return RetrofitClient.getClient().create(ConfigurationApi::class.java)
    }

    public fun getNonce(callback: Callback<String>) {
        getApi().getNonce().enqueue(callback)
    }
}