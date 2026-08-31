package com.vivocloud.reporting_app.data

import com.google.gson.annotations.SerializedName

data class ImageEntity(
    @SerializedName("id") val id: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("downloadUrl") val downloadUrl: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("confidence") val confidence: Double? = null,
    @SerializedName("aiDescription") val aiDescription: String? = null,
    @SerializedName("recommendedDepartment") val recommendedDepartment: String? = null
)
