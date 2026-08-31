package com.vivocloud.reporting_app.api

import com.vivocloud.reporting_app.data.ImageEntity
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @Multipart
    @POST("api/v1/images/upload")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ImageEntity>

    @GET("api/v1/images/me")
    suspend fun getUserImages(
        @Header("Authorization") token: String
    ): Response<List<ImageEntity>>

    @PUT("api/v1/images/{id}")
    suspend fun updateImageDetails(
        @Header("Authorization") token: String,
        @Path("id") imageId: String,
        @Body imageEntity: ImageEntity
    ): Response<ImageEntity>
}
