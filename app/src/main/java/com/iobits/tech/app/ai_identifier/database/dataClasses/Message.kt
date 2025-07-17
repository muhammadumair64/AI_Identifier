package com.iobits.tech.app.ai_identifier.database.dataClasses

import com.iobits.tech.app.ai_identifier.utils.MsgType


data class Message(val message: String, val msgType: MsgType = MsgType.SENDER)

