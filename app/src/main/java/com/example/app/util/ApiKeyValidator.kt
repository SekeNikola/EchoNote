package com.example.app.util

import android.content.Context
import com.example.app.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ApiKeyValidator {
    suspend fun validateOpenAIKey(context: Context, apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Temporarily save the key for validation
                val originalKey = ApiKeyProvider.getApiKey(context)
                ApiKeyProvider.saveApiKey(context, apiKey)
                RetrofitInstance.init(context)
                
                // Try to call the models endpoint which requires valid auth
                val response = RetrofitInstance.api.validateApiKey()
                
                // If validation failed, restore original state
                if (!response.isSuccessful) {
                    if (originalKey != null) {
                        ApiKeyProvider.saveApiKey(context, originalKey)
                    } else {
                        // Clear the invalid key if there was no original key
                        context.getSharedPreferences("logion_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .remove("openai_api_key")
                            .apply()
                    }
                    RetrofitInstance.init(context)
                }
                
                response.isSuccessful
            } catch (e: Exception) {
                // If there was an error, restore original state
                val originalKey = ApiKeyProvider.getApiKey(context)
                if (originalKey != apiKey) {
                    if (originalKey != null) {
                        ApiKeyProvider.saveApiKey(context, originalKey)
                    } else {
                        context.getSharedPreferences("logion_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .remove("openai_api_key")
                            .apply()
                    }
                    RetrofitInstance.init(context)
                }
                false
            }
        }
    }
}
