package com.example.nexusfinance

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nexusfinance.api.OpenRouterClient
import com.example.nexusfinance.model.Message
import com.example.nexusfinance.ui.ChatAdapter
import kotlinx.coroutines.*

class AiChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnBack: ImageView
    private lateinit var btnClearChat: ImageView
    private lateinit var pbLoading: ProgressBar
    private lateinit var chipAnalyze: android.widget.TextView
    private lateinit var chipSave: android.widget.TextView
    private lateinit var chipBudget: android.widget.TextView

    private lateinit var chatAdapter: ChatAdapter
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var financeContext: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_chat)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)
        btnClearChat = findViewById(R.id.btnClearChat)
        pbLoading = findViewById(R.id.pbLoading)
        chipAnalyze = findViewById(R.id.chipAnalyze)
        chipSave = findViewById(R.id.chipSave)
        chipBudget = findViewById(R.id.chipBudget)

        financeContext = intent.getStringExtra("EXTRA_FINANCE_CONTEXT") ?: "No data available."

        chatAdapter = ChatAdapter(mutableListOf())
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = chatAdapter

        btnBack.setOnClickListener { finish() }

        btnClearChat.setOnClickListener {
            chatAdapter.clearMessages()
            sendInitialAiMessage()
        }

        btnSend.setOnClickListener {
            val query = etMessage.text.toString().trim()
            if (query.isNotEmpty()) {
                etMessage.text.clear()
                sendMessage(query)
            }
        }
        
        chipAnalyze.setOnClickListener {
            sendMessage("Analyze my spending")
        }
        
        chipSave.setOnClickListener {
            sendMessage("How to save on Food?")
        }
        
        chipBudget.setOnClickListener {
            sendMessage("Am I on budget?")
        }

        sendInitialAiMessage()
    }

    private fun sendInitialAiMessage() {
        val initialQuery = "Give me a quick 3-bullet summary of my finances and tell me you are ready to answer questions."
        
        pbLoading.visibility = View.VISIBLE
        btnSend.isEnabled = false

        scope.launch {
            val response = OpenRouterClient.chatWithAI(listOf(Message("user", initialQuery)), financeContext)
            chatAdapter.addMessage(Message("assistant", response))
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
            
            pbLoading.visibility = View.GONE
            btnSend.isEnabled = true
        }
    }

    private fun sendMessage(query: String) {
        val userMsg = Message("user", query)
        chatAdapter.addMessage(userMsg)
        rvChat.scrollToPosition(chatAdapter.itemCount - 1)

        pbLoading.visibility = View.VISIBLE
        btnSend.isEnabled = false

        // Extract history to pass to API
        // Skip the initial message or include it? Let's include everything
        val history = mutableListOf<Message>()
        for (i in 0 until chatAdapter.itemCount) {
            // we can access adapter messages but since we don't expose them, let's keep a local list or add a getter
            // Actually, we can add a getMessages() to adapter
        }

        scope.launch {
            val messagesList = chatAdapter.getMessages()
            val response = OpenRouterClient.chatWithAI(messagesList, financeContext)
            chatAdapter.addMessage(Message("assistant", response))
            rvChat.scrollToPosition(chatAdapter.itemCount - 1)
            
            pbLoading.visibility = View.GONE
            btnSend.isEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
