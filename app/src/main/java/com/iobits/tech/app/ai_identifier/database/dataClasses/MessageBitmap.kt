package com.iobits.tech.app.ai_identifier.database.dataClasses

import android.graphics.Bitmap
import com.iobits.tech.app.ai_identifier.utils.MsgType

data class MessageBitmap(val bitmap: Bitmap, val msgType: MsgType = MsgType.SENDER)

