package com.example.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class CompressedAudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null

    fun startRecording(file: File): Boolean {
        stopRecording()
        outputFile = file.absolutePath
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(16000)
                setOutputFile(outputFile)
                prepare()
                start()
            }
        } catch (e: Exception) {
            mediaRecorder = null
            return false
        }
        return true
    }

    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null
    }

    fun getOutputFile(): String? = outputFile
}
