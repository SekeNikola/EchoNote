package com.example.app.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAIService {
    @Multipart
    @POST("/v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody? = null,
        @Part("prompt") prompt: RequestBody? = null
    ): Response<TranscriptionResponse>

    @Headers("Content-Type: application/json")
    @POST("/v1/chat/completions")
    suspend fun summarizeText(
        @Body request: GPTRequest
    ): Response<GPTResponse>

    @GET("/v1/models")
    suspend fun validateApiKey(): Response<ModelsResponse>

    @Headers("Content-Type: application/json")
    @POST("/v1/audio/speech")
    suspend fun generateSpeech(
        @Body request: TTSRequest
    ): Response<okhttp3.ResponseBody>
}
