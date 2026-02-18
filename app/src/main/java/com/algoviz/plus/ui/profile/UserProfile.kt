package com.algoviz.plus.ui.profile

data class UserProfile(
    val name: String = "AlgoViz User",
    val email: String = "user@algoviz.com",
    val bio: String = "Algorithm enthusiast learning through visualization",
    val avatarUrl: String? = null,
    val studyGoal: String = "Master Data Structures & Algorithms",
    val skillLevel: String = "Intermediate",
    val avatarColorIndex: Int = 0
)
