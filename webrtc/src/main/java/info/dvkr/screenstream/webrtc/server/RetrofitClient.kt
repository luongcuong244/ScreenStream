package info.dvkr.screenstream.webrtc.server

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public class RetrofitClient {

    public companion object {

        private var retrofit: Retrofit? = null
        private var currentBaseUrl: String? = null

        public fun getClient(): Retrofit {
            if (retrofit == null) {
                createClient("https://google.com")
            }

            return retrofit!!
        }

        private fun createClient(baseUrl: String) {
            val gson = GsonBuilder()
                .setLenient()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }
}