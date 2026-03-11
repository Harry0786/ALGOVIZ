package com.algoviz.plus.domain.model

data class StudyRoom(
    val id: String,
    val name: String,
    val description: String,
    val category: RoomCategory,
    val createdBy: String,
    val createdAt: Long,
    val memberCount: Int = 0,
    val lastMessageAt: Long? = null,
    val lastMessage: String? = null,
    val isActive: Boolean = true
)

enum class RoomCategory(val displayName: String) {
    SORTING("Sorting Algorithms"),
    SEARCHING("Searching & Hashing"),
    GRAPH("Graph Algorithms"),
    TREE("Tree & BST"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    GREEDY("Greedy Algorithms"),
    BACKTRACKING("Backtracking"),
    STRINGS("String Algorithms"),
    ARRAYS("Arrays & Lists"),
    LINKED_LISTS("Linked Lists"),
    STACKS_QUEUES("Stacks & Queues"),
    HEAPS("Heaps & Priority Queues"),
    RECURSION("Recursion"),
    MATH("Mathematical Algorithms"),
    BIT_MANIPULATION("Bit Manipulation"),
    SYSTEM_DESIGN("System Design"),
    CODING_INTERVIEW("Coding Interview Prep"),
    GENERAL("General Discussion")
}
