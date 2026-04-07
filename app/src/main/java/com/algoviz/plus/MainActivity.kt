package com.algoviz.plus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.algoviz.plus.core.designsystem.theme.AlgoVizTheme
import com.algoviz.plus.navigation.RootNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val darkPurple = Color(0xFF0B0B0D).toArgb()
        
            // Enable edge to edge with charcoal status and navigation bars
        enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(darkPurple),
                navigationBarStyle = SystemBarStyle.dark(darkPurple)
        )
        
        setContent {
            AlgoVizTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavHost()
                }
            }
        }
    }
}
