package com.example.app.network

data class TTSRequest(
    val model: String = "tts-1",
    val input: String,
    val voice: String = "alloy",
    val response_format: String = "mp3",
    val speed: Double = 1.0
)