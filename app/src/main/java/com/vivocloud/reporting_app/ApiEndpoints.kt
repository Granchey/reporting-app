package com.vivocloud.reporting_app

import android.util.Log

object ApiEndpoints {
    val baseUrl: String
        get() {
            var raw = BuildConfig.REPORTING_API_URL.trim().trimEnd('/')
            if (raw.isBlank()) return ""
            val idx = raw.indexOf("/api/v1")
            if (idx != -1) {
                raw = raw.substring(0, idx)
            }
            return raw.trimEnd('/')
        }

    val loginUrl: String
        get() = "$baseUrl/api/v1/auth/login"

    val registerUrl: String
        get() = "$baseUrl/api/v1/auth/register"

    val uploadUrl: String
        get() = "$baseUrl/api/v1/images/upload"

    val userImagesUrl: String
        get() = "$baseUrl/api/v1/images/me"

    val allImagesUrl: String
        get() = "$baseUrl/api/v1/images"

    fun imageDownloadUrl(id: String): String = "$baseUrl/api/v1/images/$id/download"
}
