package com.example.nexusfinance.model

data class Message(
    val role: String, // "user", "assistant", or "system"
    val content: String
)
