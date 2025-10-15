package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class CatsDataFragment : Fragment() {

    private val client: OkHttpClient by lazy { OkHttpClient() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_cats_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)
        val contentScroll = view.findViewById<ScrollView>(R.id.contentScroll)
        val factBody = view.findViewById<TextView>(R.id.factBody)
        val imageView = view.findViewById<ImageView>(R.id.catImageView)
        val placeholder = view.findViewById<TextView>(R.id.catImagePlaceholder)

        fun loadCatsAndFacts() {
            loadingIndicator.visibility = View.VISIBLE
            contentScroll.visibility = View.GONE

            viewLifecycleOwner.lifecycleScope.launch {
                var fact: String? = null
                var image: Bitmap? = null

                withContext(Dispatchers.IO) {
                    fact = fetchFact()
                    image = fetchCatImage()
                }

                if (!isAdded) return@launch

                val hasData = fact != null

                if (hasData) {
                    factBody.text = fact
                    if (image != null) {
                        imageView.setImageBitmap(image)
                        imageView.visibility = View.VISIBLE
                        placeholder.visibility = View.GONE
                    } else {
                        imageView.setImageResource(R.drawable.pixel_emblem_twins)
                        imageView.visibility = View.VISIBLE
                        placeholder.visibility = View.VISIBLE
                    }
                } else {
                    factBody.text = getString(R.string.cats_error_message)
                    imageView.setImageResource(R.drawable.pixel_emblem_twins)
                    imageView.visibility = View.VISIBLE
                    placeholder.visibility = View.VISIBLE
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.cats_error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                loadingIndicator.visibility = View.GONE
                contentScroll.visibility = View.VISIBLE
            }
        }
        
        loadCatsAndFacts()
    }

    private fun fetchFact(): String? {
        val request = Request.Builder()
            .url("http://monsterballgo.com/api/datoperturbador")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null
                val jsonString = body.string()
                val json = JSONObject(jsonString)
                json.optString("text", getString(R.string.cats_error_message))
            }
        } catch (ex: IOException) {
            null
        }
    }

    private fun fetchCatImage(): Bitmap? {
        val request = Request.Builder()
            .url("https://cataas.com/cat")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null
                val bytes = body.bytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (ex: IOException) {
            null
        }
    }
}
