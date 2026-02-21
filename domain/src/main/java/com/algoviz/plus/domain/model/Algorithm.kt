package com.algoviz.plus.domain.model

data class Algorithm(
    val id: String,
    val name: String,
    val category: AlgorithmCategory,
    val description: String,
    val timeComplexity: ComplexityInfo,
    val spaceComplexity: ComplexityInfo,
    val difficultyLevel: DifficultyLevel,
    val defaultArraySize: Int = 6
)

enum class AlgorithmCategory(val displayName: String) {
    SORTING("Sorting"),
    SEARCHING("Searching"),
    GRAPH("Graph"),
    TREE("Tree"),
    DYNAMIC_PROGRAMMING("Dynamic Programming"),
    GREEDY("Greedy"),
    BACKTRACKING("Backtracking"),
    DIVIDE_AND_CONQUER("Divide & Conquer")
}

data class ComplexityInfo(
    val best: String,
    val average: String,
    val worst: String
)

enum class DifficultyLevel(val displayName: String, val color: Long) {
    BEGINNER("Beginner", 0xFF10B981),
    INTERMEDIATE("Intermediate", 0xFFF59E0B),
    ADVANCED("Advanced", 0xFFEF4444)
}
