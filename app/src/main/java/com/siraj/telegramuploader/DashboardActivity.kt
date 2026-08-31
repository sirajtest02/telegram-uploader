package com.siraj.telegramuploader

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Find CardViews from XML
        val cardPhotoVault = findViewById<CardView>(R.id.cardPhotoVault)
        val cardVideoVault = findViewById<CardView>(R.id.cardVideoVault)
        val cardSecretNotepad = findViewById<CardView>(R.id.cardSecretNotepad)

        // 1. Photo Vault Click Listener
        cardPhotoVault.setOnClickListener {
            val intent = Intent(this, PhotoVaultActivity::class.java)
            startActivity(intent)
        }

        // 2. Video Vault Click Listener
        cardVideoVault.setOnClickListener {
            val intent = Intent(this, VideoVaultActivity::class.java)
            startActivity(intent)
        }

        // 3. Secret Notepad (Secret Chat) Click Listener
        cardSecretNotepad.setOnClickListener {
            val intent = Intent(this, SecretChatActivity::class.java)
            startActivity(intent)
        }
    }
}
