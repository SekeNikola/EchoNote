package com.example.app.network

data class ModelsResponse(
    val data: List<Model>
)

data class Model(
    val id: String,
    val `object`: String,
    val created: Long,
    val owned_by: String
)
