package com.algoviz.plus.ui.learn.model

enum class LearnTopicTag {
    SORTING,
    GRAPH,
    DATA_STRUCTURES,
    SEARCHING,
    DYNAMIC_PROGRAMMING,
    RECURSION
}

data class LearnItem(
    val id: String,
    val title: String,
    val explanation: String,
    val keyPoints: List<String>,
    val bruteForceApproach: String = "",
    val optimalApproach: String = "",
    val algorithmId: String? = null,
    val tags: Set<LearnTopicTag> = emptySet()
)

data class LearnPlaylist(
    val id: String,
    val name: String,
    val itemIds: Set<String>
)

data class LearnSection(
    val id: String,
    val title: String,
    val summary: String,
    val items: List<LearnItem>
)

data class LearnSheet(
    val id: String,
    val influencer: String,
    val title: String,
    val structureNote: String,
    val sections: List<LearnSection>
) {
    val allItems: List<LearnItem>
        get() = sections.flatMap { it.items }
}
