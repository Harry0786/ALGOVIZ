package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.AlgorithmStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DivideAndConquerAlgorithms @Inject constructor() {

    fun ternarySearch(initialArray: List<Int>, targetInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val array = initialArray.sorted()
        val target = targetInput ?: array[array.size / 2]
        val steps = mutableListOf<AlgorithmStep>()
        var left = 0
        var right = array.lastIndex
        var comparisons = 0

        steps.add(AlgorithmStep(array = array, description = "Starting Ternary Search for $target"))

        while (left <= right) {
            val third = (right - left) / 3
            val mid1 = left + third
            val mid2 = right - third
            comparisons += 2

            steps.add(
                AlgorithmStep(
                    array = array,
                    comparingIndices = setOf(mid1, mid2),
                    currentIndex = mid1,
                    comparisons = comparisons,
                    description = "Checking two pivots at $mid1 and $mid2"
                )
            )

            when {
                array[mid1] == target -> {
                    steps.add(
                        AlgorithmStep(
                            array = array,
                            sortedIndices = setOf(mid1),
                            currentIndex = mid1,
                            comparisons = comparisons,
                            description = "Found $target at index $mid1"
                        )
                    )
                    return steps
                }

                array[mid2] == target -> {
                    steps.add(
                        AlgorithmStep(
                            array = array,
                            sortedIndices = setOf(mid2),
                            currentIndex = mid2,
                            comparisons = comparisons,
                            description = "Found $target at index $mid2"
                        )
                    )
                    return steps
                }

                target < array[mid1] -> right = mid1 - 1
                target > array[mid2] -> left = mid2 + 1
                else -> {
                    left = mid1 + 1
                    right = mid2 - 1
                }
            }
        }

        steps.add(AlgorithmStep(array = array, comparisons = comparisons, description = "Target not found"))
        return steps
    }

    fun quickSelect(initialArray: List<Int>, kIndexInput: Int? = null): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val arr = initialArray.toMutableList()
        val steps = mutableListOf<AlgorithmStep>()
        var comparisons = 0
        var swaps = 0
        val k = (kIndexInput ?: (arr.size / 2)).coerceIn(0, arr.lastIndex)

        steps.add(AlgorithmStep(array = arr.toList(), description = "Starting Quick Select for kth=$k"))

        fun partition(left: Int, right: Int): Int {
            val pivot = arr[right]
            var i = left

            for (j in left until right) {
                comparisons++
                steps.add(
                    AlgorithmStep(
                        array = arr.toList(),
                        comparingIndices = setOf(j, right),
                        currentIndex = j,
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Comparing ${arr[j]} with pivot $pivot"
                    )
                )

                if (arr[j] <= pivot) {
                    if (i != j) {
                        arr[i] = arr[j].also { arr[j] = arr[i] }
                        swaps++
                        steps.add(
                            AlgorithmStep(
                                array = arr.toList(),
                                swappingIndices = setOf(i, j),
                                currentIndex = i,
                                comparisons = comparisons,
                                swaps = swaps,
                                description = "Swapped positions $i and $j"
                            )
                        )
                    }
                    i++
                }
            }

            arr[i] = arr[right].also { arr[right] = arr[i] }
            swaps++
            steps.add(
                AlgorithmStep(
                    array = arr.toList(),
                    swappingIndices = setOf(i, right),
                    currentIndex = i,
                    comparisons = comparisons,
                    swaps = swaps,
                    description = "Placed pivot at index $i"
                )
            )
            return i
        }

        var left = 0
        var right = arr.lastIndex
        while (left <= right) {
            val pivotIndex = partition(left, right)
            when {
                pivotIndex == k -> {
                    steps.add(
                        AlgorithmStep(
                            array = arr.toList(),
                            sortedIndices = setOf(pivotIndex),
                            currentIndex = pivotIndex,
                            comparisons = comparisons,
                            swaps = swaps,
                            description = "Quick Select answer is ${arr[pivotIndex]} at index $pivotIndex"
                        )
                    )
                    return steps
                }

                pivotIndex < k -> left = pivotIndex + 1
                else -> right = pivotIndex - 1
            }
        }

        return steps
    }

    fun maximumSubarray(initialArray: List<Int>): List<AlgorithmStep> {
        if (initialArray.isEmpty()) return emptyList()

        val arr = initialArray.take(10)
        val steps = mutableListOf<AlgorithmStep>()
        var comparisons = 0

        data class Result(val sum: Int, val l: Int, val r: Int)

        steps.add(AlgorithmStep(array = arr, description = "Starting Maximum Subarray (Divide and Conquer)"))

        fun crossing(low: Int, mid: Int, high: Int): Result {
            var leftSum = Int.MIN_VALUE
            var sum = 0
            var maxLeft = mid

            for (i in mid downTo low) {
                sum += arr[i]
                comparisons++
                if (sum > leftSum) {
                    leftSum = sum
                    maxLeft = i
                }
                steps.add(
                    AlgorithmStep(
                        array = arr,
                        comparingIndices = setOf(i),
                        currentIndex = i,
                        comparisons = comparisons,
                        description = "Cross-left accumulating sum=$sum"
                    )
                )
            }

            var rightSum = Int.MIN_VALUE
            sum = 0
            var maxRight = mid + 1
            for (j in mid + 1..high) {
                sum += arr[j]
                comparisons++
                if (sum > rightSum) {
                    rightSum = sum
                    maxRight = j
                }
                steps.add(
                    AlgorithmStep(
                        array = arr,
                        comparingIndices = setOf(j),
                        currentIndex = j,
                        comparisons = comparisons,
                        description = "Cross-right accumulating sum=$sum"
                    )
                )
            }

            return Result(leftSum + rightSum, maxLeft, maxRight)
        }

        fun solve(low: Int, high: Int): Result {
            if (low == high) return Result(arr[low], low, low)
            val mid = (low + high) / 2

            val left = solve(low, mid)
            val right = solve(mid + 1, high)
            val cross = crossing(low, mid, high)

            return when {
                left.sum >= right.sum && left.sum >= cross.sum -> left
                right.sum >= left.sum && right.sum >= cross.sum -> right
                else -> cross
            }
        }

        val result = solve(0, arr.lastIndex)
        steps.add(
            AlgorithmStep(
                array = arr,
                sortedIndices = (result.l..result.r).toSet(),
                currentIndex = result.r,
                comparisons = comparisons,
                description = "Maximum subarray sum is ${result.sum}, range [${result.l}, ${result.r}]"
            )
        )
        return steps
    }
}