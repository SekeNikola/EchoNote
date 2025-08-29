package com.example.app.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAIService {
    @Multipart
    @POST("/v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Part audio: MultipartBody.Part,
        @Part("model") model: String = "whisper-1"
    ): Response<TranscriptionResponse>

    @Headers("Content-Type: application/json")
    @POST("/v1/chat/completions")
    suspend fun summarizeText(
        @Body request: GPTRequest
    ): Response<GPTResponse>
}
