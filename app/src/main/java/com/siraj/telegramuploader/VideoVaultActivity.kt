package com.siraj.telegramuploader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VideoVaultActivity : AppCompatActivity() {

    private val BOT_TOKEN = "8895548602:AAFnCrskSydX-O5RmaH80T7HYnPirIxU55U"
    private val CHAT_ID = "-1004495678613"

    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnAddVideo: ImageButton
    private lateinit var rvGallery: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutActions: LinearLayout
    private lateinit var btnDownload: Button
    private lateinit var btnDelete: Button

    private val videoList = mutableListOf<String>()
    private lateinit var adapter: MediaAdapter
    private val client = OkHttpClient()

    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            result.data?.data?.let { uri ->
                uploadVideoToTelegram(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_vault)

        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        btnAddVideo = findViewById(R.id.btnAddVideo)
        rvGallery = findViewById(R.id.rvGallery)
        progressBar = findViewById(R.id.progressBar)
        layoutActions = findViewById(R.id.layoutActions)
        btnDownload = findViewById(R.id.btnDownload)
        btnDelete = findViewById(R.id.btnDelete)

        // এখানে botToken = BOT_TOKEN পাস করা হলো
        adapter = MediaAdapter(videoList, isVideo = true, botToken = BOT_TOKEN)
        rvGallery.layoutManager = GridLayoutManager(this, 2)
        rvGallery.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnAddVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "video/*"
            }
            selectVideoLauncher.launch(Intent.createChooser(intent, "Select Video"))
        }

        btnDownload.setOnClickListener {
            Toast.makeText(this, "ডাউনলোড ফিচার প্রসেস হচ্ছে...", Toast.LENGTH_SHORT).show()
        }

        btnDelete.setOnClickListener {
            Toast.makeText(this, "ডিলিট ফিচার প্রসেস হচ্ছে...", Toast.LENGTH_SHORT).show()
        }

        fetchVideosFromTelegram()
    }

    private fun uploadVideoToTelegram(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        val file = getFileFromUri(uri)
        if (file == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "ফাইল তৈরি করতে সমস্যা হয়েছে!", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaType = "video/*".toMediaTypeOrNull()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", CHAT_ID)
            .addFormDataPart("video", file.name, file.asRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$BOT_TOKEN/sendVideo")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@VideoVaultActivity, "ভিডিও আপলোড ব্যর্থ!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@VideoVaultActivity, "ভিডিও আপলোড সফল!", Toast.LENGTH_SHORT).show()
                        fetchVideosFromTelegram()
                    }
                }
            }
        })
    }

    private fun fetchVideosFromTelegram() {
        progressBar.visibility = View.VISIBLE
        val url = "https://api.telegram.org/bot$BOT_TOKEN/getUpdates"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { progressBar.visibility = View.GONE }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && !body.isNullOrEmpty()) {
                    try {
                        val json = JSONObject(body)
                        val results = json.getJSONArray("result")
                        val newVideos = mutableListOf<String>()

                        for (i in 0 until results.length()) {
                            val item = results.getJSONObject(i)
                            if (item.has("message")) {
                                val msg = item.getJSONObject("message")
                                if (msg.has("video")) {
                                    val video = msg.getJSONObject("video")
                                    val fileId = video.getString("file_id")
                                    newVideos.add(fileId)
                                }
                            }
                        }

                        runOnUiThread {
                            videoList.clear()
                            videoList.addAll(newVideos)
                            adapter.notifyDataSetChanged()
                            progressBar.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread { progressBar.visibility = View.GONE }
                    }
                }
            }
        })
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_vid_", ".mp4", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
