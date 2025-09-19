package com.example.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.app.server.NgrokManager
import com.example.app.server.KtorServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.NetworkInterface

class NetworkChangeManager(
    private val context: Context,
    private val onNetworkChanged: ((NetworkInfo) -> Unit)? = null
) {
    
    companion object {
        private const val TAG = "NetworkChangeManager"
    }
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var currentIpAddress: String? = null
    private var isMonitoring = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(TAG, "Network available: $network")
            handleNetworkChange()
        }
        
        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(TAG, "Network lost: $network")
            handleNetworkChange()
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.d(TAG, "Network capabilities changed for: $network")
            
            // Check if it's a different type of network (WiFi vs Mobile)
            val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            
            Log.d(TAG, "Network type - WiFi: $isWifi, Cellular: $isCellular")
            handleNetworkChange()
        }
    }
    
    fun startMonitoring() {
        if (isMonitoring) return
        
        Log.d(TAG, "Starting network change monitoring")
        isMonitoring = true
        
        // Get initial IP address
        currentIpAddress = getCurrentIpAddress()
        Log.d(TAG, "Initial IP address: $currentIpAddress")
        
        // Register network callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    fun stopMonitoring() {
        if (!isMonitoring) return
        
        Log.d(TAG, "Stopping network change monitoring")
        isMonitoring = false
        
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering network callback", e)
        }
    }
    
    private fun handleNetworkChange() {
        coroutineScope.launch {
            try {
                // Small delay to allow network to stabilize
                kotlinx.coroutines.delay(2000)
                
                val newIpAddress = getCurrentIpAddress()
                Log.d(TAG, "Network change detected. Old IP: $currentIpAddress, New IP: $newIpAddress")
                
                if (newIpAddress != null && newIpAddress != currentIpAddress) {
                    Log.i(TAG, "IP address changed from $currentIpAddress to $newIpAddress")
                    currentIpAddress = newIpAddress
                    
                    // Update server with new IP address
                    updateServerWithNewIp(newIpAddress)
                    
                    // Notify callback about network change
                    onNetworkChanged?.invoke(getNetworkInfo())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling network change", e)
            }
        }
    }
    
    private suspend fun updateServerWithNewIp(newIpAddress: String) {
        try {
            Log.d(TAG, "Updating server with new IP: $newIpAddress")
            
            // Stop current ngrok tunnel if running
            NgrokManager.stopTunnel()
            
            // Wait a moment for cleanup
            kotlinx.coroutines.delay(1000)
            
            // Restart ngrok with new IP
            NgrokManager.startTunnel()
            
            // Log the new server URLs
            val serverUrls = NgrokManager.getServerUrls()
            Log.i(TAG, "Server updated with new URLs: $serverUrls")
            
            // Optionally, you could restart the entire Ktor server if needed
            // This might be necessary if the server is bound to a specific interface
            // restartKtorServer()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating server with new IP", e)
        }
    }
    
    private suspend fun restartKtorServer() {
        try {
            Log.d(TAG, "Restarting Ktor server due to IP change")
            
            // Stop current server
            KtorServer.stop()
            
            // Wait for cleanup
            kotlinx.coroutines.delay(2000)
            
            // Restart server
            KtorServer.start()
            
            Log.i(TAG, "Ktor server restarted successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error restarting Ktor server", e)
        }
    }
    
    private fun getCurrentIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                
                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp) {
                    continue
                }
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    
                    // We want IPv4 addresses that are not loopback
                    if (!address.isLoopbackAddress && 
                        !address.isLinkLocalAddress &&
                        address.hostAddress?.contains(':') == false) {
                        
                        Log.d(TAG, "Found IP address: ${address.hostAddress} on interface: ${networkInterface.name}")
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current IP address", e)
        }
        return null
    }
    
    fun getCurrentNetworkType(): String {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Mobile Data"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Unknown"
        }
    }
    
    fun getNetworkInfo(): NetworkInfo {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        return NetworkInfo(
            type = getCurrentNetworkType(),
            ipAddress = getCurrentIpAddress(),
            isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        )
    }
}

data class NetworkInfo(
    val type: String,
    val ipAddress: String?,
    val isConnected: Boolean
)