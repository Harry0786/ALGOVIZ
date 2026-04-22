package com.algoviz.plus

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.algoviz.plus.core.designsystem.theme.AlgoVizTheme
import com.algoviz.plus.domain.usecase.UpdateUserPresenceUseCase
import com.algoviz.plus.features.auth.domain.usecase.GetCurrentUserUseCase
import com.algoviz.plus.navigation.RootNavHost
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.handleDeeplinks
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var supabaseClient: SupabaseClient

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Inject
    lateinit var updateUserPresenceUseCase: UpdateUserPresenceUseCase

    private var hasPasswordResetLink by mutableStateOf(false)
    private var presenceHeartbeatJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hasPasswordResetLink = isPasswordResetLink(intent)
        supabaseClient.handleDeeplinks(intent)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.BLACK)
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        setContent {
            AlgoVizTheme(darkTheme = true) {
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

    override fun onStart() {
        super.onStart()
        startPresenceHeartbeat()
    }

    override fun onStop() {
        stopPresenceHeartbeat()
        super.onStop()
    }

    private fun startPresenceHeartbeat() {
        if (presenceHeartbeatJob?.isActive == true) return

        presenceHeartbeatJob = lifecycleScope.launch {
            while (true) {
                val userId = getCurrentUserUseCase.sync()?.id ?: break
                updateUserPresenceUseCase(userId, true)
                delay(25_000L)
            }
        }
    }

    private fun stopPresenceHeartbeat() {
        presenceHeartbeatJob?.cancel()
        presenceHeartbeatJob = null

        lifecycleScope.launch {
            val userId = getCurrentUserUseCase.sync()?.id ?: return@launch
            updateUserPresenceUseCase(userId, false)
        }
    }

    private fun isPasswordResetLink(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        return data.scheme == "algovizplus" && data.host == "password-reset"
    }
}
