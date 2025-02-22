package com.agile.officepool.network

import com.agile.officepool.model.User
import com.agile.officepool.model.RideInfo
import okhttp3.OkHttpClient
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response // Add this import
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ApiService {

    //for login and registration
    @POST("/api/users/verify-linkedin")
    suspend fun verifyLinkedIn(@Query("accessToken") accessToken: String): Response<User>


    //for rideinfo
    @POST("ride/addRide")
    suspend fun addRide(@Body rideInfo: RideInfo): Response<RideInfo>

    @GET("ride/getAllRides")
    suspend fun getAllRides(): Response<List<RideInfo>>

    @GET("ride/getRideByStatus")
    suspend fun getRideByStatus(@Query("status") status: String): Response<List<RideInfo>>

    @PUT("ride/updateRide")
    suspend fun updateRide(@Body rideInfo: RideInfo): Response<RideInfo>

    @DELETE("ride/deleteRide/{rideId}")
    suspend fun deleteRide(@Path("rideId") rideId: Int): Response<String>
}

object RetrofitClient {
    private const val BASE_URL = "https://e121-2401-4900-8fc8-bfaa-64f0-5d02-ad3-8dc1.ngrok-free.app"

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

