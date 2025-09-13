package com.example.app

import androidx.compose.runtime.*

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.app.data.AppDatabase
import com.example.app.data.NoteRepository
import com.example.app.navigation.LogionNavGraph
import com.example.app.ui.theme.LogionTheme
import com.example.app.viewmodel.NoteViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.util.ApiKeyProvider
import com.example.app.network.RetrofitInstance
import com.example.app.ui.ApiKeyDialog

class LogionApp : Application() {
    
    // Database instance for the app
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        // App-level init if needed
    }
}

