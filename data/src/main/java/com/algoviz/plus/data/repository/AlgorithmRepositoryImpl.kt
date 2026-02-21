package com.algoviz.plus.data.repository

import com.algoviz.plus.data.algorithm.AlgorithmProvider
import com.algoviz.plus.data.algorithm.SearchingAlgorithms
import com.algoviz.plus.data.algorithm.SortingAlgorithms
import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.model.AlgorithmCategory
import com.algoviz.plus.domain.model.AlgorithmStep
import com.algoviz.plus.domain.repository.AlgorithmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class AlgorithmRepositoryImpl @Inject constructor(
    private val algorithmProvider: AlgorithmProvider,
    private val sortingAlgorithms: SortingAlgorithms,
    private val searchingAlgorithms: SearchingAlgorithms
) : AlgorithmRepository {
    
    override fun getAllAlgorithms(): Flow<List<Algorithm>> {
        return flowOf(algorithmProvider.getAllAlgorithms())
    }
    
    override fun getAlgorithmsByCategory(category: AlgorithmCategory): Flow<List<Algorithm>> {
        return flowOf(algorithmProvider.getAlgorithmsByCategory(category))
    }
    
    override fun getAlgorithmById(id: String): Algorithm? {
        return algorithmProvider.getAlgorithmById(id)
    }
    
    override suspend fun generateSteps(algorithmId: String, initialArray: List<Int>): List<AlgorithmStep> {
        return when (algorithmId) {
            "bubble_sort" -> sortingAlgorithms.bubbleSort(initialArray)
            "selection_sort" -> sortingAlgorithms.selectionSort(initialArray)
            "insertion_sort" -> sortingAlgorithms.insertionSort(initialArray)
            "merge_sort" -> sortingAlgorithms.mergeSort(initialArray)
            "quick_sort" -> sortingAlgorithms.quickSort(initialArray)
            "heap_sort" -> sortingAlgorithms.heapSort(initialArray)
            "shell_sort" -> sortingAlgorithms.shellSort(initialArray)
            "counting_sort" -> sortingAlgorithms.countingSort(initialArray)
            "radix_sort" -> sortingAlgorithms.radixSort(initialArray)
            "linear_search" -> searchingAlgorithms.linearSearch(initialArray)
            "binary_search" -> searchingAlgorithms.binarySearch(initialArray)
            else -> emptyList()
        }
    }
}
