package com.siraj.telegramuploader

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MediaAdapter(
    private val mediaList: List<String>,
    private val isVideo: Boolean = false,
    private val botToken: String = ""
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    private val client = OkHttpClient()

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivMedia: ImageView = itemView.findViewById(R.id.ivMedia)

        fun bind(fileId: String) {
            // Fetch file path from Telegram
            fetchFilePathAndLoadImage(fileId, ivMedia)

            // Click listener to open full screen
            ivMedia.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putExtra("file_id", fileId)
                context.startActivity(intent)
            }

            // Long click listener for delete
            ivMedia.setOnLongClickListener {
                Toast.makeText(itemView.context, "ডিলিট অপশন শীঘ্রই আসবে", Toast.LENGTH_SHORT).show()
                true
            }
        }

        private fun fetchFilePathAndLoadImage(fileId: String, imageView: ImageView) {
            val url = "https://api.telegram.org/bot$botToken/getFile?file_id=$fileId"
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    // Show placeholder on failure
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val body = response.body?.string()
                    if (response.isSuccessful && !body.isNullOrEmpty()) {
                        try {
                            val json = JSONObject(body)
                            if (json.getBoolean("ok")) {
                                val result = json.getJSONObject("result")
                                val filePath = result.getString("file_path")
                                val imageUrl =
                                    "https://api.telegram.org/file/bot$botToken/$filePath"

                                // Load image using Glide
                                Glide.with(imageView.context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_placeholder)
                                    .error(R.drawable.ic_placeholder)
                                    .into(imageView)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val layoutId = if (isVideo) R.layout.item_video else R.layout.item_photo
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(mediaList[position])
    }

    override fun getItemCount(): Int = mediaList.size
}
