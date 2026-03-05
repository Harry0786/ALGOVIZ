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
    val array: List<Int> = emptyList(),
    val comparingIndices: Set<Int> = emptySet(),
    val swappingIndices: Set<Int> = emptySet(),
    val sortedIndices: Set<Int> = emptySet(),
    val currentIndex: Int? = null,
    val comparisons: Int = 0,
    val swaps: Int = 0,
    val description: String = "",
    // Graph visualization data
    val graphData: GraphData? = null,
    // Tree visualization data
    val treeData: TreeData? = null,
    // Matrix visualization data (for DP algorithms)
    val matrix: List<List<Int>>? = null
)

data class GraphData(
    val nodes: List<GraphNode>,
    val edges: List<GraphEdge>,
    val visitedNodes: Set<Int> = emptySet(),
    val activeNodes: Set<Int> = emptySet(),
    val processedNodes: Set<Int> = emptySet(),
    val activeEdges: Set<Pair<Int, Int>> = emptySet(),
    val distances: Map<Int, Int> = emptyMap()
)

data class GraphNode(
    val id: Int,
    val x: Float,
    val y: Float,
    val label: String = id.toString()
)

data class GraphEdge(
    val from: Int,
    val to: Int,
    val weight: Int = 1,
    val isDirected: Boolean = false
)

data class TreeData(
    val nodes: List<TreeNode>,
    val activeNode: Int? = null,
    val visitedNodes: Set<Int> = emptySet(),
    val highlightedNodes: Set<Int> = emptySet()
)

data class TreeNode(
    val id: Int,
    val value: Int,
    val parentId: Int? = null,
    val isLeftChild: Boolean? = null,
    val level: Int = 0
)
