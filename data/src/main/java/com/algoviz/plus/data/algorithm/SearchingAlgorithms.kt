package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.AlgorithmStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchingAlgorithms @Inject constructor() {

    fun linearSearch(initialArray: List<Int>, targetInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val target = targetInput ?: initialArray[initialArray.size / 2]
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

    fun binarySearch(initialArray: List<Int>, targetInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.sorted()
        val target = targetInput ?: array[array.size / 2]
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

    fun jumpSearch(initialArray: List<Int>, targetInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.sorted()
        val target = targetInput ?: array[array.size / 2]
        val n = array.size
        val jump = kotlin.math.sqrt(n.toDouble()).toInt().coerceAtLeast(1)
        var comparisons = 0
        var prev = 0
        var curr = jump

        steps.add(
            AlgorithmStep(
                array = array,
                description = "Starting Jump Search for $target with block size $jump"
            )
        )

        while (prev < n && array[minOf(curr, n) - 1] < target) {
            comparisons++
            val checkIndex = minOf(curr, n) - 1
            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(checkIndex),
                    currentIndex = checkIndex,
                    comparisons = comparisons,
                    description = "Jumping to block ending at index $checkIndex"
                )
            )
            prev = curr
            curr += jump
            if (prev >= n) break
        }

        for (i in prev until minOf(curr, n)) {
            comparisons++
            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(i),
                    currentIndex = i,
                    comparisons = comparisons,
                    description = "Linear scan in block at index $i"
                )
            )
            if (array[i] == target) {
                steps.add(
                    AlgorithmStep(
                        array = array,
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
                array = array,
                comparisons = comparisons,
                description = "Target not found"
            )
        )
        return steps
    }

    fun interpolationSearch(initialArray: List<Int>, targetInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.sorted()
        val target = targetInput ?: array[array.size / 2]
        var low = 0
        var high = array.lastIndex
        var comparisons = 0

        steps.add(
            AlgorithmStep(
                array = array,
                description = "Starting Interpolation Search for $target"
            )
        )

        while (low <= high && target >= array[low] && target <= array[high]) {
            if (array[high] == array[low]) {
                comparisons++
                val index = low
                val found = array[index] == target
                steps.add(
                    AlgorithmStep(
                        array = array,
                        comparingIndices = setOf(index),
                        currentIndex = index,
                        comparisons = comparisons,
                        sortedIndices = if (found) setOf(index) else emptySet(),
                        description = if (found) "Found $target at index $index" else "Target not found"
                    )
                )
                return steps
            }

            val position = low + ((target - array[low]) * (high - low) / (array[high] - array[low]))
            comparisons++

            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(position),
                    currentIndex = position,
                    comparisons = comparisons,
                    description = "Estimated probe position is $position"
                )
            )

            when {
                array[position] == target -> {
                    steps.add(
                        AlgorithmStep(
                            array = array,
                            sortedIndices = setOf(position),
                            currentIndex = position,
                            comparisons = comparisons,
                            description = "Found $target at index $position"
                        )
                    )
                    return steps
                }

                array[position] < target -> {
                    low = position + 1
                }

                else -> {
                    high = position - 1
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

    fun exponentialSearch(initialArray: List<Int>, targetInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.sorted()
        val target = targetInput ?: array[array.size / 2]
        var comparisons = 0

        steps.add(
            AlgorithmStep(
                array = array,
                description = "Starting Exponential Search for $target"
            )
        )

        comparisons++
        if (array[0] == target) {
            steps.add(
                AlgorithmStep(
                    array = array,
                    sortedIndices = setOf(0),
                    currentIndex = 0,
                    comparisons = comparisons,
                    description = "Found $target at index 0"
                )
            )
            return steps
        }

        var bound = 1
        while (bound < array.size && array[bound] <= target) {
            comparisons++
            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(bound),
                    currentIndex = bound,
                    comparisons = comparisons,
                    description = "Expanding search bound to $bound"
                )
            )
            bound *= 2
        }

        var left = bound / 2
        var right = minOf(bound, array.lastIndex)

        while (left <= right) {
            val mid = (left + right) / 2
            comparisons++
            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(mid),
                    currentIndex = mid,
                    comparisons = comparisons,
                    description = "Binary search in range [$left, $right], checking $mid"
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

                array[mid] < target -> left = mid + 1
                else -> right = mid - 1
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
