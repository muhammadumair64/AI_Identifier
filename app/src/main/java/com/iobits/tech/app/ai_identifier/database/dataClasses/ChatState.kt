package com.iobits.tech.app.ai_identifier.database.dataClasses


data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
