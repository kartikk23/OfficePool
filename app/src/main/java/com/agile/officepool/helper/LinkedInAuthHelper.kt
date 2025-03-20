package com.agile.officepool.helper

import android.util.Log
import com.agile.officepool.BuildConfig
import com.github.scribejava.core.builder.ServiceBuilder

import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.core.model.Verb
import com.github.scribejava.apis.LinkedInApi20
import com.github.scribejava.core.oauth.OAuth20Service
import com.google.gson.internal.GsonBuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class LinkedInAuthHelper {
    private val clientId = "77vp84pv0ee3qi"
    private val clientSecret = "WPL_AP1.dA9t6eXWnaFcJHH1.82TKiQ=="
    private val redirectUri = "https://75e6-2402-e280-3e9d-70f-2db6-97fc-ecab-ff15.ngrok-free.app/oauth/linkedin-callback"
    private val scopes = "openid profile email"



    private val service: OAuth20Service = ServiceBuilder(clientId)
        .apiSecret(clientSecret)
        .callback(redirectUri)
        .defaultScope(scopes)
        .build(LinkedInApi20.instance())



    // Function to generate the LinkedIn authorization URL
    fun getAuthorizationUrl(): String {
        Log.d("LinkedInLogin", "Authorization URL: ${service.authorizationUrl}")
        return service.authorizationUrl
    }

    suspend fun getAccessToken(code: String): String {
        return withContext(Dispatchers.IO) {
            service.getAccessToken(code).accessToken
        }
    }

    suspend fun getUserInfo(accessToken: String): LinkedInUserInfo {
        return withContext(Dispatchers.IO) {
            val request = OAuthRequest(Verb.GET, "https://api.linkedin.com/v2/userinfo")
            request.addHeader("Authorization", "Bearer $accessToken")
            val response = service.execute(request)
            val jsonResponse = JSONObject(response.body)
            Log.d("LinkedInLogin", "JSON RESPONSE: $jsonResponse")
            LinkedInUserInfo(
                name = jsonResponse.getString("name"),
                email = jsonResponse.getString("email"),
            )
        }
    }
}

data class LinkedInUserInfo(
    val name: String,
    val email: String

)
