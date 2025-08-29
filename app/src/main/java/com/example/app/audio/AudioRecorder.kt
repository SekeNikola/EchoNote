package com.example.app.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {
    interface AmplitudeListener {
        fun onAmplitude(amplitude: Int)
    }
    private var amplitudeListener: AmplitudeListener? = null
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    @Volatile private var isRecording = false
    private var outputFile: String? = null

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    fun setOnAmplitudeListener(listener: AmplitudeListener?) {
        amplitudeListener = listener
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun startRecording(file: File): Boolean {
        stopRecording()
        outputFile = file.absolutePath

        val packageManager = context.packageManager
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            Log.e("AudioRecorder", "ABORTING: Device does not have a microphone feature!")
            return false
        }

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        isRecording = true
        audioRecord?.startRecording()

        recordingThread = Thread {
            writeAudioDataToFile(file, bufferSize)
        }.also { it.start() }

        Log.d("AudioRecorder", "AudioRecord started for: $outputFile")
        return true
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping AudioRecord for: $outputFile", e)
        }
        audioRecord = null
        recordingThread?.join(500)
        recordingThread = null
        Log.d("AudioRecorder", "AudioRecord stopped for: $outputFile")
    }

    private fun writeAudioDataToFile(file: File, bufferSize: Int) {
    val buffer = ShortArray(bufferSize)
    val raf = java.io.RandomAccessFile(file, "rw")
    // Write WAV header placeholder (will update after recording)
    val header = ByteArray(44)
    raf.write(header)
        var totalAudioLen = 0
        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                // Calculate amplitude (peak absolute value in buffer)
                val amplitude = buffer.take(read).maxOfOrNull { kotlin.math.abs(it.toInt()) } ?: 0
                amplitudeListener?.onAmplitude(amplitude)
                val byteBuffer = ByteArray(read * 2)
                for (i in 0 until read) {
                    byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                    byteBuffer[i * 2 + 1] = ((buffer[i].toInt() shr 8) and 0xFF).toByte()
                }
                raf.write(byteBuffer)
                totalAudioLen += byteBuffer.size
            }
        }
        // Update WAV header in-place
        updateWavHeaderInPlace(raf, totalAudioLen)
        raf.close()
        Log.d("AudioRecorder", "WAV file written: ${file.absolutePath}, size: ${file.length()} bytes")

        // Log the first 44 bytes (WAV header) for debugging
        try {
            val headerBytes = ByteArray(44)
            val fis = java.io.FileInputStream(file)
            fis.read(headerBytes)
            fis.close()
            val headerHex = headerBytes.joinToString(" ") { String.format("%02X", it) }
            Log.d("AudioRecorder", "WAV header: $headerHex")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to read WAV header for debug", e)
        }
    }

    private fun updateWavHeaderInPlace(raf: java.io.RandomAccessFile, totalAudioLen: Int) {
        val totalDataLen = totalAudioLen + 36
        val channels = 1
        val byteRate = 16 * sampleRate * channels / 8
        val header = ByteArray(44)
        // RIFF/WAVE header
    header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        writeInt(header, 4, totalDataLen)
    header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        writeInt(header, 16, 16) // Subchunk1Size
        writeShort(header, 20, 1.toShort()) // AudioFormat (PCM)
        writeShort(header, 22, channels.toShort())
        writeInt(header, 24, sampleRate)
        writeInt(header, 28, byteRate)
        writeShort(header, 32, (channels * 16 / 8).toShort()) // BlockAlign
        writeShort(header, 34, 16.toShort()) // BitsPerSample
    header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        writeInt(header, 40, totalAudioLen)
        raf.seek(0)
        raf.write(header, 0, 44)
    }
    // ...existing code...
    }

    private fun writeInt(header: ByteArray, offset: Int, value: Int) {
        header[offset] = (value and 0xff).toByte()
        header[offset + 1] = ((value shr 8) and 0xff).toByte()
        header[offset + 2] = ((value shr 16) and 0xff).toByte()
        header[offset + 3] = ((value shr 24) and 0xff).toByte()
    }

    private fun writeShort(header: ByteArray, offset: Int, value: Short) {
        header[offset] = (value.toInt() and 0xff).toByte()
        header[offset + 1] = ((value.toInt() shr 8) and 0xff).toByte()
    }

    // Removed unused getOutputFile()

