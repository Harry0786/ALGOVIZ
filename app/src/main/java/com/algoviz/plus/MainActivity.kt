package com.algoviz.plus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.algoviz.plus.core.designsystem.theme.AlgoVizTheme
import com.algoviz.plus.navigation.RootNavHost
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var supabaseClient: SupabaseClient

    private var hasPasswordResetLink by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasPasswordResetLink = isPasswordResetLink(intent)
        supabaseClient.handleDeeplinks(intent)
        enableEdgeToEdge()
        
        setContent {
            AlgoVizTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavHost(
                        isPasswordResetLink = hasPasswordResetLink
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        hasPasswordResetLink = isPasswordResetLink(intent)
        supabaseClient.handleDeeplinks(intent)
    }

    private fun isPasswordResetLink(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        return data.scheme == "algovizplus" && data.host == "password-reset"
    }
}
