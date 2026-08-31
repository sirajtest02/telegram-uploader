package com.siraj.telegramuploader

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SecretChatActivity : AppCompatActivity() {

    private lateinit var rvSecretChat: RecyclerView
    private lateinit var etNoteInput: EditText
    private lateinit var btnSendNote: ImageButton
    private lateinit var btnBack: ImageButton

    private val messageList = mutableListOf<NoteMessage>()
    private lateinit var adapter: SecretChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_chat)

        // Initialize Views
        rvSecretChat = findViewById(R.id.rvSecretChat)
        etNoteInput = findViewById(R.id.etNoteInput)
        btnSendNote = findViewById(R.id.btnSendNote)
        btnBack = findViewById(R.id.btnBack)

        // Setup RecyclerView
        adapter = SecretChatAdapter(messageList)
        rvSecretChat.layoutManager = LinearLayoutManager(this)
        rvSecretChat.adapter = adapter

        // Load saved notes
        loadSavedNotes()

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Send button
        btnSendNote.setOnClickListener {
            sendNote()
        }
    }

    private fun sendNote() {
        val noteText = etNoteInput.text.toString().trim()

        if (noteText.isEmpty()) {
            Toast.makeText(this, "মেসেজ খালি রাখা যাবে না!", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current timestamp
        val timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Create new message
        val newMessage = NoteMessage(noteText, timestamp)

        // Add to list
        messageList.add(newMessage)
        adapter.notifyItemInserted(messageList.size - 1)

        // Scroll to bottom
        rvSecretChat.scrollToPosition(messageList.size - 1)

        // Save to SharedPreferences
        saveNote(newMessage)

        // Clear input
        etNoteInput.text.clear()

        Toast.makeText(this, "মেসেজ সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
    }

    private fun saveNote(noteMessage: NoteMessage) {
        val sharedPref = getSharedPreferences("SecretNotes", MODE_PRIVATE)
        val savedNotes = sharedPref.getStringSet("notes_list", mutableSetOf()) ?: mutableSetOf()

        val newSet = HashSet(savedNotes)
        // Save as JSON format: message|timestamp
        newSet.add("${noteMessage.message}|${noteMessage.timestamp}")

        sharedPref.edit().putStringSet("notes_list", newSet).apply()
    }

    private fun loadSavedNotes() {
        val sharedPref = getSharedPreferences("SecretNotes", MODE_PRIVATE)
        val savedNotes = sharedPref.getStringSet("notes_list", emptySet()) ?: emptySet()

        messageList.clear()
        for (note in savedNotes) {
            val parts = note.split("|")
            if (parts.size == 2) {
                messageList.add(NoteMessage(parts[0], parts[1]))
            }
        }

        adapter.notifyDataSetChanged()

        // Scroll to bottom if messages exist
        if (messageList.isNotEmpty()) {
            rvSecretChat.scrollToPosition(messageList.size - 1)
        }
    }
}
