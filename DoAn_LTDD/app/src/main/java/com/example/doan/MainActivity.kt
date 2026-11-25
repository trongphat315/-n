package com.example.doan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.doan.navigation.AppNavGraph
import com.example.doan.ui.theme.DoAnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoAnTheme {
                AppNavGraph()
            }
        }
    }
}