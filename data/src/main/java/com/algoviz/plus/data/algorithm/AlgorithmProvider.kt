package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.model.AlgorithmCategory
import com.algoviz.plus.domain.model.ComplexityInfo
import com.algoviz.plus.domain.model.DifficultyLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlgorithmProvider @Inject constructor() {
    
    private val algorithms = listOf(
        Algorithm(
            id = "bubble_sort",
            name = "Bubble Sort",
            category = AlgorithmCategory.SORTING,
            description = "A simple sorting algorithm that repeatedly steps through the list, compares adjacent elements and swaps them if they are in the wrong order.",
            timeComplexity = ComplexityInfo(
                best = "O(n)",
                average = "O(n²)",
                worst = "O(n²)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER
        ),
        Algorithm(
            id = "selection_sort",
            name = "Selection Sort",
            category = AlgorithmCategory.SORTING,
            description = "An in-place comparison sorting algorithm that divides the input list into two parts: sorted and unsorted, repeatedly selecting the smallest element.",
            timeComplexity = ComplexityInfo(
                best = "O(n²)",
                average = "O(n²)",
                worst = "O(n²)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER
        ),
        Algorithm(
            id = "insertion_sort",
            name = "Insertion Sort",
            category = AlgorithmCategory.SORTING,
            description = "A simple sorting algorithm that builds the final sorted array one item at a time, inserting each element into its proper position.",
            timeComplexity = ComplexityInfo(
                best = "O(n)",
                average = "O(n²)",
                worst = "O(n²)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER
        ),
        Algorithm(
            id = "merge_sort",
            name = "Merge Sort",
            category = AlgorithmCategory.SORTING,
            description = "An efficient, stable sorting algorithm that uses divide and conquer approach, dividing the array into smaller subarrays and merging them back.",
            timeComplexity = ComplexityInfo(
                best = "O(n log n)",
                average = "O(n log n)",
                worst = "O(n log n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(n)",
                average = "O(n)",
                worst = "O(n)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE
        ),
        Algorithm(
            id = "quick_sort",
            name = "Quick Sort",
            category = AlgorithmCategory.SORTING,
            description = "A highly efficient sorting algorithm using divide and conquer, selecting a 'pivot' element and partitioning the array around it.",
            timeComplexity = ComplexityInfo(
                best = "O(n log n)",
                average = "O(n log n)",
                worst = "O(n²)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(log n)",
                average = "O(log n)",
                worst = "O(n)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE
        ),
        Algorithm(
            id = "heap_sort",
            name = "Heap Sort",
            category = AlgorithmCategory.SORTING,
            description = "A comparison-based sorting algorithm that uses a binary heap data structure.",
            timeComplexity = ComplexityInfo(
                best = "O(n log n)",
                average = "O(n log n)",
                worst = "O(n log n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE
        ),
        Algorithm(
            id = "shell_sort",
            name = "Shell Sort",
            category = AlgorithmCategory.SORTING,
            description = "An in-place comparison sort that generalizes insertion sort by allowing exchanges of far apart elements.",
            timeComplexity = ComplexityInfo(
                best = "O(n log n)",
                average = "O(n^(3/2))",
                worst = "O(n^2)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE
        ),
        Algorithm(
            id = "counting_sort",
            name = "Counting Sort",
            category = AlgorithmCategory.SORTING,
            description = "A non-comparison sorting algorithm that counts the number of objects with distinct key values.",
            timeComplexity = ComplexityInfo(
                best = "O(n + k)",
                average = "O(n + k)",
                worst = "O(n + k)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(k)",
                average = "O(k)",
                worst = "O(k)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE
        ),
        Algorithm(
            id = "radix_sort",
            name = "Radix Sort",
            category = AlgorithmCategory.SORTING,
            description = "A non-comparison sorting algorithm that processes integer keys digit by digit.",
            timeComplexity = ComplexityInfo(
                best = "O(nk)",
                average = "O(nk)",
                worst = "O(nk)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(n + k)",
                average = "O(n + k)",
                worst = "O(n + k)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE
        ),
        Algorithm(
            id = "linear_search",
            name = "Linear Search",
            category = AlgorithmCategory.SEARCHING,
            description = "A simple search algorithm that checks each element in sequence until the target is found.",
            timeComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(n)",
                worst = "O(n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER
        ),
        Algorithm(
            id = "binary_search",
            name = "Binary Search",
            category = AlgorithmCategory.SEARCHING,
            description = "A search algorithm that repeatedly divides a sorted array to find the target value.",
            timeComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(log n)",
                worst = "O(log n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER
        )
    )
    
    fun getAllAlgorithms(): List<Algorithm> = algorithms
    
    fun getAlgorithmsByCategory(category: AlgorithmCategory): List<Algorithm> {
        return algorithms.filter { it.category == category }
    }
    
    fun getAlgorithmById(id: String): Algorithm? {
        return algorithms.find { it.id == id }
    }
}
