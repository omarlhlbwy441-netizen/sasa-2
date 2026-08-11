package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SasaHomeScreen
import com.example.ui.SasaViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SasaViewModel by viewModels {
        val app = application as SasaApplication
        SasaViewModel.Factory(app.chatRepository, app.memoryRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SasaHomeScreen(viewModel = viewModel)
            }
        }
    }
}

