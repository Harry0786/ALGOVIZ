package com.algoviz.plus.domain.model

import java.util.Locale

data class StudyRoom(
    val id: String,
    val name: String,
    val description: String,
    val category: RoomCategory,
    val createdBy: String,
    val createdAt: Long,
    val memberCount: Int = 0,
    val maxMembers: Int = 50,
    val isPrivate: Boolean = false,
    val lastMessageAt: Long? = null,
    val lastMessage: String? = null,
    val isActive: Boolean = true
)

enum class RoomCategory(val displayName: String) {
    GENERAL("General Discussion"),
    ACADEMICS("Academics & Subjects"),
    PROBLEM_SOLVING("Problem Solving & DSA"),
    PROJECTS("Projects & Collaboration"),
    INTERNSHIPS("Internships & Placements"),
    CAREER("Career & Higher Studies"),
    EVENTS("Events, Clubs & Hackathons"),
    TECH_NEWS("Tech News & Trends"),
    RESOURCES("Resources & Notes"),
    HELP_DESK("Help Desk & Doubts");

    companion object {
        fun fromStorageCategory(rawCategory: String): RoomCategory {
            return when (rawCategory.uppercase(Locale.US)) {
                // Legacy coding-focused values mapped into broader buckets.
                "SORTING",
                "SEARCHING",
                "GRAPH",
                "TREE",
                "DYNAMIC_PROGRAMMING",
                "GREEDY",
                "BACKTRACKING",
                "STRINGS",
                "ARRAYS",
                "LINKED_LISTS",
                "STACKS_QUEUES",
                "HEAPS",
                "RECURSION",
                "MATH",
                "BIT_MANIPULATION",
                "CODING_INTERVIEW" -> PROBLEM_SOLVING

                "SYSTEM_DESIGN" -> PROJECTS

                else -> entries.find { it.name == rawCategory } ?: GENERAL
            }
        }
    }
}
