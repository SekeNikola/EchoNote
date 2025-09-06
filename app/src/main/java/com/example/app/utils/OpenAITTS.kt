package com.example.app.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.IOException
import java.io.File
import java.io.FileOutputStream

class OpenAITTS(private val context: Context, private val apiKey: String) {

    private val client = OkHttpClient()
    private var currentMediaPlayer: MediaPlayer? = null

    fun speak(text: String, voice: String = "alloy", onReady: (() -> Unit)? = null, onComplete: (() -> Unit)? = null, onError: ((String) -> Unit)? = null) {
        // Stop any currently playing audio
        stopSpeaking()
        
        val url = "https://api.openai.com/v1/audio/speech"

        val json = """
            {
                "model": "tts-1",
                "voice": "$voice",
                "input": "$text",
                "speed": 1.0
            }
        """.trimIndent()

        val body = RequestBody.create("application/json".toMediaType(), json)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OpenAITTS", "Request failed: ${e.message}")
                onError?.invoke(e.message ?: "Request failed")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e("OpenAITTS", "Error response: ${response.code}")
                    onError?.invoke("Error: ${response.code}")
                    return
                }

                try {
                    // Save audio file locally
                    val audioFile = File(context.cacheDir, "tts_output_${System.currentTimeMillis()}.mp3")
                    val sink = FileOutputStream(audioFile)
                    response.body?.byteStream()?.copyTo(sink)
                    sink.close()

                    // Play with MediaPlayer on main thread
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        currentMediaPlayer = MediaPlayer().apply {
                            try {
                                setDataSource(audioFile.absolutePath)
                                prepareAsync()
                                setOnPreparedListener {
                                    start()
                                    Log.d("OpenAITTS", "Started playing TTS audio")
                                    onReady?.invoke()
                                }
                                setOnCompletionListener {
                                    release()
                                    currentMediaPlayer = null
                                    // Clean up the temporary file
                                    audioFile.delete()
                                    Log.d("OpenAITTS", "TTS playback completed")
                                    onComplete?.invoke() // Notify completion
                                }
                                setOnErrorListener { _, what, extra ->
                                    Log.e("OpenAITTS", "MediaPlayer error: $what, $extra")
                                    release()
                                    currentMediaPlayer = null
                                    audioFile.delete()
                                    onError?.invoke("Playback error")
                                    true
                                }
                            } catch (e: Exception) {
                                Log.e("OpenAITTS", "Error setting up MediaPlayer", e)
                                release()
                                currentMediaPlayer = null
                                audioFile.delete()
                                onError?.invoke("Setup error: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("OpenAITTS", "Error processing audio response", e)
                    onError?.invoke("Processing error: ${e.message}")
                }
            }
        })
    }

    fun stopSpeaking() {
        currentMediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                Log.d("OpenAITTS", "Stopped current TTS playback")
            } catch (e: Exception) {
                Log.e("OpenAITTS", "Error stopping TTS", e)
            }
        }
        currentMediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return currentMediaPlayer?.isPlaying ?: false
    }
}
