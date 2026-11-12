package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.PolyCodeApp
import com.example.ui.theme.PolyCodeTheme
import com.example.viewmodel.PolyCodeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PolyCodeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PolyCodeTheme {
                PolyCodeApp(viewModel = viewModel)
            }
        }
    }
}
