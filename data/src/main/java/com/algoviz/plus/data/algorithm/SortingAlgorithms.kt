package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.AlgorithmStep
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SortingAlgorithms @Inject constructor() {
    
    fun bubbleSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.toMutableList()
        var comparisons = 0
        var swaps = 0
        val n = array.size
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            description = "Starting Bubble Sort with ${array.size} elements"
        ))
        
        for (i in 0 until n - 1) {
            var swapped = false
            for (j in 0 until n - i - 1) {
                // Comparing
                comparisons++
                steps.add(AlgorithmStep(
                    array = array.toList(),
                    comparingIndices = setOf(j, j + 1),
                    sortedIndices = (n - i until n).toSet(),
                    comparisons = comparisons,
                    swaps = swaps,
                    description = "Comparing ${array[j]} and ${array[j + 1]}"
                ))
                
                if (array[j] > array[j + 1]) {
                    // Swapping
                    swaps++
                    val temp = array[j]
                    array[j] = array[j + 1]
                    array[j + 1] = temp
                    swapped = true
                    
                    steps.add(AlgorithmStep(
                        array = array.toList(),
                        swappingIndices = setOf(j, j + 1),
                        sortedIndices = (n - i until n).toSet(),
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Swapped ${array[j + 1]} and ${array[j]}"
                    ))
                }
            }
            
            if (!swapped) break
        }
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            sortedIndices = array.indices.toSet(),
            comparisons = comparisons,
            swaps = swaps,
            description = "Sorting complete!"
        ))
        
        return steps
    }
    
    fun selectionSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.toMutableList()
        var comparisons = 0
        var swaps = 0
        val n = array.size
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            description = "Starting Selection Sort"
        ))
        
        for (i in 0 until n - 1) {
            var minIdx = i
            
            for (j in i + 1 until n) {
                comparisons++
                steps.add(AlgorithmStep(
                    array = array.toList(),
                    comparingIndices = setOf(minIdx, j),
                    sortedIndices = (0 until i).toSet(),
                    currentIndex = i,
                    comparisons = comparisons,
                    swaps = swaps,
                    description = "Comparing ${array[minIdx]} and ${array[j]}"
                ))
                
                if (array[j] < array[minIdx]) {
                    minIdx = j
                }
            }
            
            if (minIdx != i) {
                swaps++
                val temp = array[i]
                array[i] = array[minIdx]
                array[minIdx] = temp
                
                steps.add(AlgorithmStep(
                    array = array.toList(),
                    swappingIndices = setOf(i, minIdx),
                    sortedIndices = (0..i).toSet(),
                    comparisons = comparisons,
                    swaps = swaps,
                    description = "Swapped ${array[minIdx]} and ${array[i]}"
                ))
            }
        }
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            sortedIndices = array.indices.toSet(),
            comparisons = comparisons,
            swaps = swaps,
            description = "Sorting complete!"
        ))
        
        return steps
    }
    
    fun insertionSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.toMutableList()
        var comparisons = 0
        var swaps = 0
        val n = array.size
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            sortedIndices = setOf(0),
            description = "Starting Insertion Sort"
        ))
        
        for (i in 1 until n) {
            val key = array[i]
            var j = i - 1
            
            while (j >= 0) {
                comparisons++
                steps.add(AlgorithmStep(
                    array = array.toList(),
                    comparingIndices = setOf(j, j + 1),
                    sortedIndices = (0..i).toSet(),
                    currentIndex = i,
                    comparisons = comparisons,
                    swaps = swaps,
                    description = "Comparing ${array[j]} and $key"
                ))
                
                if (array[j] <= key) break
                
                swaps++
                array[j + 1] = array[j]
                j--
            }
            
            array[j + 1] = key
            
            steps.add(AlgorithmStep(
                array = array.toList(),
                sortedIndices = (0..i).toSet(),
                comparisons = comparisons,
                swaps = swaps,
                description = "Inserted $key at position ${j + 1}"
            ))
        }
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            sortedIndices = array.indices.toSet(),
            comparisons = comparisons,
            swaps = swaps,
            description = "Sorting complete!"
        ))
        
        return steps
    }
    
    fun mergeSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        var comparisons = 0
        var swaps = 0
        
        steps.add(AlgorithmStep(
            array = initialArray,
            description = "Starting Merge Sort"
        ))
        
        // Simplified merge sort for visualization
        val result = mergeSortRecursive(initialArray, steps, comparisons, swaps)
        
        steps.add(AlgorithmStep(
            array = result.first,
            sortedIndices = result.first.indices.toSet(),
            comparisons = result.second,
            swaps = result.third,
            description = "Sorting complete!"
        ))
        
        return steps
    }
    
    private fun mergeSortRecursive(
        arr: List<Int>,
        steps: MutableList<AlgorithmStep>,
        comp: Int,
        sw: Int
    ): Triple<List<Int>, Int, Int> {
        if (arr.size <= 1) return Triple(arr, comp, sw)
        
        val mid = arr.size / 2
        val left = mergeSortRecursive(arr.subList(0, mid), steps, comp, sw)
        val right = mergeSortRecursive(arr.subList(mid, arr.size), steps, left.second, left.third)
        
        return merge(left.first, right.first, steps, right.second, right.third)
    }
    
    private fun merge(
        left: List<Int>,
        right: List<Int>,
        steps: MutableList<AlgorithmStep>,
        comp: Int,
        sw: Int
    ): Triple<List<Int>, Int, Int> {
        var comparisons = comp
        var swaps = sw
        val result = mutableListOf<Int>()
        var i = 0
        var j = 0
        
        while (i < left.size && j < right.size) {
            comparisons++
            if (left[i] <= right[j]) {
                result.add(left[i])
                i++
            } else {
                result.add(right[j])
                j++
            }
        }
        
        result.addAll(left.subList(i, left.size))
        result.addAll(right.subList(j, right.size))
        
        steps.add(AlgorithmStep(
            array = result,
            comparisons = comparisons,
            swaps = swaps,
            description = "Merged two subarrays"
        ))
        
        return Triple(result, comparisons, swaps)
    }
    
    fun quickSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.toMutableList()
        var comparisons = 0
        var swaps = 0
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            description = "Starting Quick Sort"
        ))
        
        quickSortRecursive(array, 0, array.size - 1, steps, comparisons, swaps)
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            sortedIndices = array.indices.toSet(),
            description = "Sorting complete!"
        ))
        
        return steps
    }
    
    private fun quickSortRecursive(
        array: MutableList<Int>,
        low: Int,
        high: Int,
        steps: MutableList<AlgorithmStep>,
        comp: Int,
        sw: Int
    ): Pair<Int, Int> {
        var comparisons = comp
        var swaps = sw
        
        if (low < high) {
            val pivotResult = partition(array, low, high, steps, comparisons, swaps)
            val pi = pivotResult.first
            comparisons = pivotResult.second
            swaps = pivotResult.third
            
            val leftResult = quickSortRecursive(array, low, pi - 1, steps, comparisons, swaps)
            val rightResult = quickSortRecursive(array, pi + 1, high, steps, leftResult.first, leftResult.second)
            
            comparisons = rightResult.first
            swaps = rightResult.second
        }
        
        return Pair(comparisons, swaps)
    }
    
    private fun partition(
        array: MutableList<Int>,
        low: Int,
        high: Int,
        steps: MutableList<AlgorithmStep>,
        comp: Int,
        sw: Int
    ): Triple<Int, Int, Int> {
        var comparisons = comp
        var swaps = sw
        val pivot = array[high]
        var i = low - 1
        
        for (j in low until high) {
            comparisons++
            steps.add(AlgorithmStep(
                array = array.toList(),
                comparingIndices = setOf(j, high),
                currentIndex = high,
                comparisons = comparisons,
                swaps = swaps,
                description = "Comparing ${array[j]} with pivot $pivot"
            ))
            
            if (array[j] < pivot) {
                i++
                if (i != j) {
                    swaps++
                    val temp = array[i]
                    array[i] = array[j]
                    array[j] = temp
                    
                    steps.add(AlgorithmStep(
                        array = array.toList(),
                        swappingIndices = setOf(i, j),
                        currentIndex = high,
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Swapped ${array[j]} and ${array[i]}"
                    ))
                }
            }
        }
        
        swaps++
        val temp = array[i + 1]
        array[i + 1] = array[high]
        array[high] = temp
        
        steps.add(AlgorithmStep(
            array = array.toList(),
            swappingIndices = setOf(i + 1, high),
            comparisons = comparisons,
            swaps = swaps,
            description = "Pivot $pivot placed at position ${i + 1}"
        ))
        
        return Triple(i + 1, comparisons, swaps)
    }

    fun heapSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.toMutableList()
        var comparisons = 0
        var swaps = 0
        val n = array.size

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                description = "Starting Heap Sort"
            )
        )

        fun heapify(size: Int, i: Int) {
            var largest = i
            val left = 2 * i + 1
            val right = 2 * i + 2

            if (left < size) {
                comparisons++
                steps.add(
                    AlgorithmStep(
                        array = array.toList(),
                        comparingIndices = setOf(largest, left),
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Comparing for heap"
                    )
                )
                if (array[left] > array[largest]) largest = left
            }

            if (right < size) {
                comparisons++
                steps.add(
                    AlgorithmStep(
                        array = array.toList(),
                        comparingIndices = setOf(largest, right),
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Comparing for heap"
                    )
                )
                if (array[right] > array[largest]) largest = right
            }

            if (largest != i) {
                swaps++
                val temp = array[i]
                array[i] = array[largest]
                array[largest] = temp

                steps.add(
                    AlgorithmStep(
                        array = array.toList(),
                        swappingIndices = setOf(i, largest),
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Swapped to maintain heap"
                    )
                )

                heapify(size, largest)
            }
        }

        for (i in n / 2 - 1 downTo 0) {
            heapify(n, i)
        }

        for (i in n - 1 downTo 1) {
            swaps++
            val temp = array[0]
            array[0] = array[i]
            array[i] = temp

            steps.add(
                AlgorithmStep(
                    array = array.toList(),
                    swappingIndices = setOf(0, i),
                    sortedIndices = (i until n).toSet(),
                    comparisons = comparisons,
                    swaps = swaps,
                    description = "Moved max to position $i"
                )
            )

            heapify(i, 0)
        }

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                sortedIndices = array.indices.toSet(),
                comparisons = comparisons,
                swaps = swaps,
                description = "Sorting complete!"
            )
        )

        return steps
    }

    fun shellSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val array = initialArray.toMutableList()
        var comparisons = 0
        var swaps = 0
        val n = array.size

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                description = "Starting Shell Sort"
            )
        )

        var gap = n / 2
        while (gap > 0) {
            for (i in gap until n) {
                val temp = array[i]
                var j = i

                while (j >= gap) {
                    comparisons++
                    steps.add(
                        AlgorithmStep(
                            array = array.toList(),
                            comparingIndices = setOf(j - gap, j),
                            comparisons = comparisons,
                            swaps = swaps,
                            description = "Comparing gap elements"
                        )
                    )

                    if (array[j - gap] <= temp) break

                    swaps++
                    array[j] = array[j - gap]
                    steps.add(
                        AlgorithmStep(
                            array = array.toList(),
                            swappingIndices = setOf(j - gap, j),
                            comparisons = comparisons,
                            swaps = swaps,
                            description = "Shifting ${array[j]}"
                        )
                    )

                    j -= gap
                }

                array[j] = temp
                steps.add(
                    AlgorithmStep(
                        array = array.toList(),
                        comparisons = comparisons,
                        swaps = swaps,
                        description = "Placed $temp at index $j"
                    )
                )
            }
            gap /= 2
        }

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                sortedIndices = array.indices.toSet(),
                comparisons = comparisons,
                swaps = swaps,
                description = "Sorting complete!"
            )
        )

        return steps
    }

    fun countingSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        if (initialArray.isEmpty()) return steps

        val min = initialArray.minOrNull() ?: 0
        val max = initialArray.maxOrNull() ?: 0
        val range = max - min + 1
        val count = IntArray(range)
        val output = MutableList(initialArray.size) { 0 }
        var swaps = 0

        steps.add(
            AlgorithmStep(
                array = initialArray,
                description = "Starting Counting Sort"
            )
        )

        for (value in initialArray) {
            count[value - min]++
        }

        var outputIndex = 0
        for (i in count.indices) {
            while (count[i] > 0) {
                val value = i + min
                output[outputIndex] = value
                swaps++
                steps.add(
                    AlgorithmStep(
                        array = output.toList(),
                        sortedIndices = (0..outputIndex).toSet(),
                        swaps = swaps,
                        description = "Placing $value"
                    )
                )
                outputIndex++
                count[i]--
            }
        }

        steps.add(
            AlgorithmStep(
                array = output.toList(),
                sortedIndices = output.indices.toSet(),
                swaps = swaps,
                description = "Sorting complete!"
            )
        )

        return steps
    }

    fun radixSort(initialArray: List<Int>): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        if (initialArray.isEmpty()) return steps

        val array = initialArray.toMutableList()
        var swaps = 0
        val max = array.maxOrNull() ?: 0

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                description = "Starting Radix Sort"
            )
        )

        var exp = 1
        while (max / exp > 0) {
            swaps = countingSortByDigit(array, exp, steps, swaps)
            steps.add(
                AlgorithmStep(
                    array = array.toList(),
                    swaps = swaps,
                    description = "Completed digit sort (exp=$exp)"
                )
            )
            exp *= 10
        }

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                sortedIndices = array.indices.toSet(),
                swaps = swaps,
                description = "Sorting complete!"
            )
        )

        return steps
    }

    private fun countingSortByDigit(
        array: MutableList<Int>,
        exp: Int,
        steps: MutableList<AlgorithmStep>,
        sw: Int
    ): Int {
        val output = MutableList(array.size) { 0 }
        val count = IntArray(10)
        var swaps = sw

        for (value in array) {
            val digit = (value / exp) % 10
            count[digit]++
        }

        for (i in 1 until 10) {
            count[i] += count[i - 1]
        }

        for (i in array.size - 1 downTo 0) {
            val value = array[i]
            val digit = (value / exp) % 10
            output[count[digit] - 1] = value
            count[digit]--
            swaps++
        }

        for (i in array.indices) {
            array[i] = output[i]
        }

        steps.add(
            AlgorithmStep(
                array = array.toList(),
                swaps = swaps,
                description = "Updated array for exp=$exp"
            )
        )

        return swaps
    }
}
