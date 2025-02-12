package com.agile.officepool.network

import com.agile.officepool.User
import okhttp3.OkHttpClient
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response // Add this import
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

interface ApiService {
    @POST("/api/users/verify-linkedin")
    suspend fun verifyLinkedIn(@Query("accessToken") accessToken: String): Response<User>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.254:8088"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

