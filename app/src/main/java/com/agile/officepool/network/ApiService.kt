package com.agile.officepool.network


import com.agile.officepool.model.FcmTokenRequest
import com.agile.officepool.model.LoginRequest
import com.agile.officepool.model.LoginResponse
import com.agile.officepool.model.ProfileRequest
import com.agile.officepool.model.ProfileResponse
import com.agile.officepool.model.RegisterRequest
import com.agile.officepool.model.RegisterResponse
import com.agile.officepool.model.ReqResponseDTO
import com.agile.officepool.model.User
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.model.RideResponse
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response // Add this import
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("api/users/register")
    suspend fun register(@Body newRegisterRequest: RegisterRequest): Response<RegisterResponse>

    @POST("api/users/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/users/logout")
    suspend fun logout(): Response<JSONObject>

    //for rideinfo
    @POST("ride/addRide")
    suspend fun addRide(@Body rideInfo: RideInfo): Response<RideInfo>

    @GET("ride/getAllRides")
    suspend fun getAllRides(): Response<List<RideInfo>>

    @GET("ride/getRideByStatus")
    suspend fun getRideByStatus(@Query("status") status: String): Response<List<RideInfo>>

    @GET("ride/getRideByRideId")
    suspend fun getRideByRideId(@Query("rideId") rideId: String): Response<RideInfo>

    @PUT("ride/updateRide")
    suspend fun updateRide(@Body rideInfo: RideInfo): Response<RideInfo>

    @DELETE("ride/deleteRide/{rideId}")
    suspend fun deleteRide(@Path("rideId") rideId: Int): Response<String>

    @POST("ride/startRideAndNotifyPassenger")
    suspend fun startRideAndNotifyPassenger(@Body request:RideRequest): Response<Unit>


    // update profile details of user
    @POST("api/users/updateProfile")
    suspend fun updateProfile(@Body profileRequest: ProfileRequest): Response<ProfileResponse>

    @POST("api/users/update-fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): Response<Unit>

    @GET("api/users/fcmToken/{userId}")
    suspend fun getFcmTokenByUserId(@Path("userId") userId: Long): Response<Map<String, String>>


    // for ride request
    @POST("rides/addRideReq")
    suspend fun addRideReq(@Body rideRequest : RideRequest) : Response<RideResponse>

    @POST("rides/sendNotificationToRider")
    suspend fun sendNotificationToRider(@Body rideRequest : RideRequest) : Response<RideResponse>

    @POST("rides/sendNotificationToPassenger")
    suspend fun sendNotificationToPassenger(@Body reqRes : ReqResponseDTO) : Response<RideResponse>

    @GET("rides/getAllReqByRiderId")
    suspend fun getAllReqByRiderId(@Query("riderId") riderId: Long): Response<List<RideRequest>>

    @GET("rides/getAllReqByPassengerId")
    suspend fun getAllReqByPassengerId(@Query("passengerId") passengerId: Long): Response<List<RideRequest>>

    @PUT("rides/updateRideRequestStatus")
    suspend fun updateRideRequestStatus(@Body rideRequestDto: RideRequestStatusUpdateDTO): Response<ResponseBody>

}

object RetrofitClient {
    private const val BASE_URL = "https://officepoolspringboot.onrender.com/"

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableListOf<Cookie>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            // Replace cookies with the same name and path
            cookieStore.removeAll { old ->
                cookies.any { new ->
                    old.name() == new.name() && old.domain() == new.domain() && old.path() == new.path()
                }
            }
            cookieStore.addAll(cookies)
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val validCookies = cookieStore.filter { it.matches(url) && it.expiresAt() > System.currentTimeMillis() }
            return validCookies
        }
    }

    // code to send the test notification to the device










    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(false) // ✅ Prevents automatic redirection
        .followSslRedirects(false)
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


