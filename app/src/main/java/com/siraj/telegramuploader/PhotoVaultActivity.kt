package com.siraj.telegramuploader

import android.app.Activity
import android.content.Context
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

class PhotoVaultActivity : AppCompatActivity() {

    private val BOT_TOKEN = "8895548602:AAFnCrskSydX-O5RmaH80T7HYnPirIxU55U"
    private val CHAT_ID = "-1004495678613"

    private lateinit var tvTitle: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnAddPhoto: ImageButton
    private lateinit var rvGallery: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutActions: LinearLayout
    private lateinit var btnDownload: Button
    private lateinit var btnDelete: Button

    private val photoList = mutableListOf<String>()
    private lateinit var adapter: MediaAdapter
    private val client = OkHttpClient()

    private val selectImagesLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val clipData = result.data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    uploadPhotoToTelegram(uri)
                }
            } else {
                result.data?.data?.let { uri ->
                    uploadPhotoToTelegram(uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_vault)

        tvTitle = findViewById(R.id.tvTitle)
        btnBack = findViewById(R.id.btnBack)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        rvGallery = findViewById(R.id.rvGallery)
        progressBar = findViewById(R.id.progressBar)
        layoutActions = findViewById(R.id.layoutActions)
        btnDownload = findViewById(R.id.btnDownload)
        btnDelete = findViewById(R.id.btnDelete)

        adapter = MediaAdapter(photoList, isVideo = false, botToken = BOT_TOKEN)
        rvGallery.layoutManager = GridLayoutManager(this, 3)
        rvGallery.adapter = adapter

        btnBack.setOnClickListener { finish() }

        btnAddPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            selectImagesLauncher.launch(Intent.createChooser(intent, "Select Photos"))
        }

        btnDownload.setOnClickListener {
            Toast.makeText(this, "ডাউনলোড ফিচার প্রসেস হচ্ছে...", Toast.LENGTH_SHORT).show()
        }

        btnDelete.setOnClickListener {
            Toast.makeText(this, "ডিলিট ফিচার প্রসেস হচ্ছে...", Toast.LENGTH_SHORT).show()
        }

        // লোকাল স্টোরেজ থেকে পূর্বে সেভ হওয়া ফটোগুলো লোড করা
        loadSavedPhotos()
    }

    private fun uploadPhotoToTelegram(uri: Uri) {
        progressBar.visibility = View.VISIBLE
        val file = getFileFromUri(uri)
        if (file == null) {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "ফাইল প্রসেস করতে ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", CHAT_ID)
            .addFormDataPart("photo", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("https://api.telegram.org/bot$BOT_TOKEN/sendPhoto")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@PhotoVaultActivity, "আপলোড ব্যর্থ হয়েছে!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                        try {
                            val json = JSONObject(responseData)
                            if (json.getBoolean("ok")) {
                                val resultJson = json.getJSONObject("result")
                                val photos = resultJson.getJSONArray("photo")
                                // Telegram থেকে ফেরত আসা সবচেয়ে বড় সাইজের ছবির file_id নেওয়া
                                val fileId = photos.getJSONObject(photos.length() - 1).getString("file_id")

                                saveAndShowPhoto(fileId)
                                Toast.makeText(this@PhotoVaultActivity, "ছবি আপলোড সফল!", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    // আপলোড হওয়া ছবির file_id লোকাল ডাটাবেসে সেভ করা এবং UI তে আপডেট করা
    private fun saveAndShowPhoto(fileId: String) {
        val sharedPref = getSharedPreferences("VaultData", Context.MODE_PRIVATE)
        val savedSet = sharedPref.getStringSet("photo_ids", mutableSetOf()) ?: mutableSetOf()

        val newSet = HashSet(savedSet)
        newSet.add(fileId)

        sharedPref.edit().putStringSet("photo_ids", newSet).apply()

        photoList.clear()
        photoList.addAll(newSet)
        adapter.notifyDataSetChanged()
    }

    // অ্যাপ ওপেন হলে আগের সেভ করা ছবিগুলো লোড করা
    private fun loadSavedPhotos() {
        val sharedPref = getSharedPreferences("VaultData", Context.MODE_PRIVATE)
        val savedSet = sharedPref.getStringSet("photo_ids", emptySet()) ?: emptySet()

        photoList.clear()
        photoList.addAll(savedSet)
        adapter.notifyDataSetChanged()
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
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
