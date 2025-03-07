package com.example.chatwithme

import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.core.net.toFile
import java.io.File
import java.io.FileInputStream
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toFile
import java.io.InputStream
import android.content.Context


class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        apiKey = Constants.apiKey
    )

    @RequiresApi(35)
    fun sendMessage(context: Context, text: String, imageUri: String?) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }.toList()
                )

                messageList.add(MessageModel(text, imageUri, "user"))
                messageList.add(MessageModel("Typing...", null, "model"))

                val response = if (imageUri != null) {
                    val bitmap = convertUriToBitmap(context, Uri.parse(imageUri)) // Pass context
                    if (bitmap != null) {
                        chat.sendMessage(
                            content {
                                text(text)
                                image(bitmap)
                            }
                        )
                    } else {
                        chat.sendMessage(text) // Fallback if image conversion fails
                    }
                } else {
                    chat.sendMessage(text)
                }

                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), null, "model"))
            } catch (e: Exception) {
                messageList.removeLast()
                messageList.add(MessageModel("Error: ${e.message}", null, "model"))
            }
        }
    }




    fun convertUriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }






}