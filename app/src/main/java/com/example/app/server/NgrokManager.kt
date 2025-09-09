package com.example.app.server

import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL

object NgrokManager {
    private var ngrokProcess: Process? = null
    private var publicUrl: String? = null
    private var localUrl: String? = null
    
    suspend fun startTunnel() = withContext(Dispatchers.IO) {
        try {
            // Get local IP address
            localUrl = getLocalIpAddress()?.let { "http://$it:8080" }
            Log.d("NgrokManager", "Local server URL: $localUrl")
            
            // Try to start ngrok tunnel (optional)
            try {
                val processBuilder = ProcessBuilder("ngrok", "http", "8080", "--log=stdout")
                ngrokProcess = processBuilder.start()
                
                // Wait a bit for ngrok to start
                delay(5000)
                
                // Get public URL
                publicUrl = getTunnelUrl()
                Log.d("NgrokManager", "Ngrok tunnel started: $publicUrl")
                
            } catch (e: Exception) {
                Log.w("NgrokManager", "Ngrok not available, using local IP only: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e("NgrokManager", "Failed to start tunnel", e)
        }
    }
    
    fun stopTunnel() {
        try {
            ngrokProcess?.destroyForcibly()
            ngrokProcess = null
            
        } catch (e: Exception) {
            Log.e("NgrokManager", "Failed to stop ngrok", e)
        }
    }
    
    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.indexOf(':') == -1) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("NgrokManager", "Failed to get local IP", e)
        }
        return null
    }
    
    private suspend fun getTunnelUrl(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://localhost:4040/api/tunnels")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val response = connection.inputStream.bufferedReader().readText()
            
            // Parse the JSON response to extract public URL
            val regex = "\"public_url\":\"(https://[^\"]+)\"".toRegex()
            val match = regex.find(response)
            
            return@withContext match?.groupValues?.get(1)
            
        } catch (e: Exception) {
            Log.w("NgrokManager", "Failed to get tunnel URL: ${e.message}")
            null
        }
    }
    
    fun getPublicUrl(): String? = publicUrl
    fun getLocalUrl(): String? = localUrl
    
    fun getServerUrls(): List<String> {
        val urls = mutableListOf<String>()
        localUrl?.let { urls.add("Local: $it") }
        publicUrl?.let { urls.add("Public: $it") }
        if (urls.isEmpty()) {
            urls.add("Server starting...")
        }
        return urls
    }
}
