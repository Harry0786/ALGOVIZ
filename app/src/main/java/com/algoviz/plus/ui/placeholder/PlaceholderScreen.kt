package com.algoviz.plus.ui.placeholder

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.algoviz.plus.features.auth.presentation.viewmodel.AuthViewModel
import com.algoviz.plus.ui.home.HomeScreen

@Composable
fun PlaceholderScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    HomeScreen(
        onLogoutClick = { authViewModel.logout() }
    )
}
