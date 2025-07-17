package com.iobits.tech.app.ai_identifier.interfaces

import com.iobits.tech.app.ai_identifier.database.dataClasses.MessageBitmap

sealed interface ChatEvents {
    data class OnMessageSendBitmap(val message: MessageBitmap) : ChatEvents
    data object CancelResponse : ChatEvents
}