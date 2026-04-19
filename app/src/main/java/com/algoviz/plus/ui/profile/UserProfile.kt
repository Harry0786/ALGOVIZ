package com.algoviz.plus.ui.profile

data class UserProfile(
    val name: String = "AlgoViz User",
    val username: String = "",
    val email: String = "user@algoviz.com",
    val phoneNumber: String = "",
    val avatarUrl: String? = null,
    val avatarColorIndex: Int = 0
)
