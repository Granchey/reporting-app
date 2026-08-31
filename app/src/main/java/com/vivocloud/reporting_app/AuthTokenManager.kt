package com.vivocloud.reporting_app

import android.util.Log

object AuthTokenManager {
    var token: String? = null

    fun saveTokenFromResponse(jsonBody: String) {
        try {
            if (jsonBody.isBlank()) return
            val trimmed = jsonBody.trim()
            if (trimmed.startsWith("{")) {
                val json = org.json.JSONObject(trimmed)
                val extractedToken = listOf("token", "accessToken", "jwt", "idToken", "bearerToken", "access_token")
                    .firstNotNullOfOrNull { key ->
                        if (json.has(key) && !json.isNull(key)) json.optString(key) else null
                    } ?: if (json.has("data")) {
                    val dataObj = json.optJSONObject("data")
                    listOf("token", "accessToken", "jwt", "access_token").firstNotNullOfOrNull { key ->
                        if (dataObj?.has(key) == true && !dataObj.isNull(key)) dataObj.optString(key) else null
                    }
                } else null

                if (!extractedToken.isNullOrBlank()) {
                    token = extractedToken
                    Log.d("AuthTokenManager", "Saved auth token successfully")
                }
            }
        } catch (e: Exception) {
            Log.e("AuthTokenManager", "Failed to parse token from response", e)
        }
    }
}
