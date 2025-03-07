package com.example.chatwithme

data class MessageModel(
    val message: String = "",
    val imageUrl: String? = null,
    val role: String,
)
