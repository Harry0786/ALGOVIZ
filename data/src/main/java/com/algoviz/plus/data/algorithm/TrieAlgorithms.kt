package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.AlgorithmStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrieAlgorithms @Inject constructor() {

    fun trieOperations(wordsInput: String? = null): List<AlgorithmStep> {
        val words = wordsInput
            ?.split(Regex("[,\\s]+"))
            ?.map { it.trim().lowercase() }
            ?.filter { it.isNotBlank() }
            ?.take(10)
            ?.ifEmpty { null }
            ?: listOf("cat", "car", "care", "dog", "dot")

        val steps = mutableListOf<AlgorithmStep>()
        val insertedPrefixes = mutableSetOf<String>()
        var comparisons = 0

        steps.add(
            AlgorithmStep(
                array = words.map { it.length },
                description = "Building Trie from words: ${words.joinToString()}"
            )
        )

        words.forEachIndexed { wordIndex, word ->
            var current = ""
            word.forEachIndexed { charIndex, ch ->
                comparisons++
                current += ch
                val existed = insertedPrefixes.contains(current)
                insertedPrefixes.add(current)

                steps.add(
                    AlgorithmStep(
                        array = words.map { it.length },
                        comparingIndices = setOf(wordIndex),
                        currentIndex = wordIndex,
                        comparisons = comparisons,
                        description = if (existed) {
                            "Prefix '$current' already exists, move deeper"
                        } else {
                            "Create node for '$current' at character ${charIndex + 1}"
                        }
                    )
                )
            }

            steps.add(
                AlgorithmStep(
                    array = words.map { it.length },
                    sortedIndices = setOf(wordIndex),
                    currentIndex = wordIndex,
                    comparisons = comparisons,
                    description = "Mark end of word '$word'"
                )
            )
        }

        val probe = words.first()
        steps.add(
            AlgorithmStep(
                array = words.map { it.length },
                description = "Search demo: checking whether '$probe' exists in Trie"
            )
        )

        var prefix = ""
        probe.forEachIndexed { i, c ->
            comparisons++
            prefix += c
            steps.add(
                AlgorithmStep(
                    array = words.map { it.length },
                    currentIndex = i,
                    comparisons = comparisons,
                    description = if (insertedPrefixes.contains(prefix)) {
                        "Found prefix '$prefix'"
                    } else {
                        "Missing prefix '$prefix', word not found"
                    }
                )
            )
        }

        steps.add(
            AlgorithmStep(
                array = words.map { it.length },
                comparisons = comparisons,
                description = "Trie ready: fast word lookup by shared prefixes"
            )
        )

        return steps
    }
}