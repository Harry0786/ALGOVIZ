package com.algoviz.plus.domain.model

data class VisualizationState(
    val array: List<Int> = emptyList(),
    val comparingIndices: Set<Int> = emptySet(),
    val swappingIndices: Set<Int> = emptySet(),
    val sortedIndices: Set<Int> = emptySet(),
    val currentIndex: Int? = null,
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val comparisons: Int = 0,
    val swaps: Int = 0,
    val isPlaying: Boolean = false,
    val isComplete: Boolean = false,
    val speed: PlaybackSpeed = PlaybackSpeed.NORMAL
)

enum class PlaybackSpeed(val displayName: String, val delayMs: Long) {
    SLOW("0.5x", 1000),
    NORMAL("1x", 500),
    FAST("1.5x", 250),
    VERY_FAST("2x", 100)
}

data class AlgorithmStep(
    val array: List<Int>,
    val comparingIndices: Set<Int> = emptySet(),
    val swappingIndices: Set<Int> = emptySet(),
    val sortedIndices: Set<Int> = emptySet(),
    val currentIndex: Int? = null,
    val comparisons: Int = 0,
    val swaps: Int = 0,
    val description: String = ""
)
