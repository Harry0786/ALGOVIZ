package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.AlgorithmStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchingAlgorithms @Inject constructor() {

    fun linearSearch(initialArray: List<Int>): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val target = initialArray[initialArray.size / 2]
        var comparisons = 0

        steps.add(
            AlgorithmStep(
                array = initialArray,
                description = "Starting Linear Search for $target"
            )
        )

        for (i in initialArray.indices) {
            comparisons++
            steps.add(
                AlgorithmStep(
                    array = initialArray,
                    comparingIndices = setOf(i),
                    currentIndex = i,
                    comparisons = comparisons,
                    description = "Checking index $i"
                )
            )

            if (initialArray[i] == target) {
                steps.add(
                    AlgorithmStep(
                        array = initialArray,
                        sortedIndices = setOf(i),
                        currentIndex = i,
                        comparisons = comparisons,
                        description = "Found $target at index $i"
                    )
                )
                return steps
            }
        }

        steps.add(
            AlgorithmStep(
                array = initialArray,
                comparisons = comparisons,
                description = "Target not found"
            )
        )

        return steps
    }

    fun binarySearch(initialArray: List<Int>): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.sorted()
        val target = array[array.size / 2]
        var comparisons = 0
        var left = 0
        var right = array.size - 1

        steps.add(
            AlgorithmStep(
                array = array,
                description = "Starting Binary Search for $target (array sorted)"
            )
        )

        while (left <= right) {
            val mid = (left + right) / 2
            comparisons++

            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(mid),
                    currentIndex = mid,
                    comparisons = comparisons,
                    description = "Checking middle index $mid"
                )
            )

            when {
                array[mid] == target -> {
                    steps.add(
                        AlgorithmStep(
                            array = array,
                            sortedIndices = setOf(mid),
                            currentIndex = mid,
                            comparisons = comparisons,
                            description = "Found $target at index $mid"
                        )
                    )
                    return steps
                }
                array[mid] < target -> {
                    left = mid + 1
                    steps.add(
                        AlgorithmStep(
                            array = array,
                            comparingIndices = setOf(mid),
                            currentIndex = mid,
                            comparisons = comparisons,
                            description = "$target is bigger, search right half"
                        )
                    )
                }
                else -> {
                    right = mid - 1
                    steps.add(
                        AlgorithmStep(
                            array = array,
                            comparingIndices = setOf(mid),
                            currentIndex = mid,
                            comparisons = comparisons,
                            description = "$target is smaller, search left half"
                        )
                    )
                }
            }
        }

        steps.add(
            AlgorithmStep(
                array = array,
                comparisons = comparisons,
                description = "Target not found"
            )
        )

        return steps
    }
}
