package com.siraj.telegramuploader

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var ivFullImage: ImageView
    private lateinit var btnBack: ImageButton
    private val BOT_TOKEN = "8895548602:AAFnCrskSydX-O5RmaH80T7HYnPirIxU55U"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullscreen_image)

        ivFullImage = findViewById(R.id.ivFullImage)
        btnBack = findViewById(R.id.btnBack)

        // Get file_id from intent
        val fileId = intent.getStringExtra("file_id")

        if (!fileId.isNullOrEmpty()) {
            loadImageFromTelegram(fileId)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadImageFromTelegram(fileId: String) {
        // Telegram API URL to get file URL
        val url = "https://api.telegram.org/bot$BOT_TOKEN/getFile?file_id=$fileId"

        // You can use Retrofit or OkHttp to fetch the file URL
        // Then load it with Glide or Picasso
        // For now, we'll use a simple approach with Glide

        val imageUrl = "https://api.telegram.org/file/bot$BOT_TOKEN/photos/$fileId.jpg"
        Glide.with(this)
            .load(imageUrl)
            .into(ivFullImage)
    }
}
