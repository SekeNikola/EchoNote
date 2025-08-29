package com.example.app.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.example.app.util.ApiKeyProvider

object RetrofitInstance {
    private const val BASE_URL = "https://api.openai.com"
    private var apiKey: String? = null
    private var apiService: OpenAIService? = null

    fun init(context: Context) {
        apiKey = ApiKeyProvider.getApiKey(context)
        apiService = null // force re-create
    }

    private fun getClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val key = apiKey ?: ""
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $key")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val api: OpenAIService
        get() {
            if (apiService == null) {
                apiService = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(OpenAIService::class.java)
            }
            return apiService!!
        }
}
