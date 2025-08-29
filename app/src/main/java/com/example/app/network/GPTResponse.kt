package com.example.app.network

data class GPTResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
