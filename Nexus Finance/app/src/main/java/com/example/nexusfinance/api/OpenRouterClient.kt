package com.example.nexusfinance.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import com.example.nexusfinance.model.Message

object OpenRouterClient {
    private const val TAG = "OpenRouterClient"
    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"
    const val API_KEY = ""
    private const val MODEL = "poolside/laguna-xs-2.1:free"

    suspend fun chatWithAI(messages: List<Message>, financeContext: String): String {
        return withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                Log.d(TAG, "Requesting AI chat with context: $financeContext")
                val url = URL(API_URL)
                conn = url.openConnection() as HttpURLConnection
                conn.apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $API_KEY")
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("HTTP-Referer", "https://nexusfinance.app")
                    setRequestProperty("X-Title", "Nexus Finance")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 15000
                }

                val systemMsg = JSONObject().apply {
                    put("role", "system")
                    put("content", "You are Nexus AI, a professional and extremely helpful financial advisor. Analyze the user's data and answer their questions thoughtfully. Keep responses concise and use markdown formatting where appropriate. Here is the user's current financial context:\n$financeContext")
                }
                
                val messagesArray = JSONArray().put(systemMsg)
                for (msg in messages) {
                    messagesArray.put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }

                val requestBody = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", messagesArray)
                }

                OutputStreamWriter(conn.outputStream).use { it.write(requestBody.toString()) }

                val responseCode = conn.responseCode
                Log.d(TAG, "OpenRouter response code: $responseCode")

                if (responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "OpenRouter raw response: $response")
                    val jsonResponse = JSONObject(response)
                    val choices = jsonResponse.optJSONArray("choices")
                    if (choices != null && choices.length() > 0) {
                        choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                    } else {
                        "Nexus AI could not generate insights at this time."
                    }
                } else {
                    val errorStream = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "OpenRouter error ($responseCode): $errorStream")
                    "Nexus AI encountered an error ($responseCode). Please try again."
                }
            } catch (e: java.net.UnknownHostException) {
                Log.e(TAG, "Network disconnected", e)
                "Network Error: Your device or emulator is not connected to the internet."
            } catch (e: Exception) {
                Log.e(TAG, "Nexus AI connection error", e)
                "Nexus AI is currently unavailable. Error: ${e.localizedMessage}"
            } finally {
                conn?.disconnect()
            }
        }
    }
}
