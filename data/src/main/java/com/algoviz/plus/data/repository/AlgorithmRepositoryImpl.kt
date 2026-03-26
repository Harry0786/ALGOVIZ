package com.algoviz.plus.data.repository

import com.algoviz.plus.data.algorithm.*
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
    private val searchingAlgorithms: SearchingAlgorithms,
    private val graphAlgorithms: GraphAlgorithms,
    private val treeAlgorithms: TreeAlgorithms,
    private val dpAlgorithms: DPAlgorithms,
    private val greedyAlgorithms: GreedyAlgorithms,
    private val backtrackingAlgorithms: BacktrackingAlgorithms,
    private val stringAlgorithms: StringAlgorithms,
    private val divideAndConquerAlgorithms: DivideAndConquerAlgorithms,
    private val trieAlgorithms: TrieAlgorithms
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
    
    override suspend fun generateSteps(
        algorithmId: String,
        initialArray: List<Int>,
        extraInput: Map<String, String>
    ): List<AlgorithmStep> {
        val targetInput = extraInput["target"]?.toIntOrNull()
        val kIndexInput = extraInput["kIndex"]?.toIntOrNull()
        val wordsInput = extraInput["words"]

        return when (algorithmId) {
            // Sorting algorithms
            "bubble_sort" -> sortingAlgorithms.bubbleSort(initialArray)
            "selection_sort" -> sortingAlgorithms.selectionSort(initialArray)
            "insertion_sort" -> sortingAlgorithms.insertionSort(initialArray)
            "merge_sort" -> sortingAlgorithms.mergeSort(initialArray)
            "quick_sort" -> sortingAlgorithms.quickSort(initialArray)
            "heap_sort" -> sortingAlgorithms.heapSort(initialArray)
            "shell_sort" -> sortingAlgorithms.shellSort(initialArray)
            "counting_sort" -> sortingAlgorithms.countingSort(initialArray)
            "radix_sort" -> sortingAlgorithms.radixSort(initialArray)
            // Searching algorithms
            "linear_search" -> searchingAlgorithms.linearSearch(initialArray, targetInput)
            "binary_search" -> searchingAlgorithms.binarySearch(initialArray, targetInput)
            "jump_search" -> searchingAlgorithms.jumpSearch(initialArray, targetInput)
            "interpolation_search" -> searchingAlgorithms.interpolationSearch(initialArray, targetInput)
            "exponential_search" -> searchingAlgorithms.exponentialSearch(initialArray, targetInput)
            // Graph algorithms
            "bfs" -> graphAlgorithms.bfs()
            "dfs" -> graphAlgorithms.dfs()
            "dijkstra" -> graphAlgorithms.dijkstra()
            "bellman_ford" -> graphAlgorithms.bellmanFord()
            "kruskal" -> graphAlgorithms.kruskalMST()
            "prim" -> graphAlgorithms.primMST()
            // Tree algorithms
            "bst_insertion" -> treeAlgorithms.bstInsertion()
            "bst_search" -> treeAlgorithms.bstSearch(searchValue = targetInput ?: 40)
            "inorder_traversal" -> treeAlgorithms.inorderTraversal()
            "preorder_traversal" -> treeAlgorithms.preorderTraversal()
            "trie_operations" -> trieAlgorithms.trieOperations(wordsInput)
            // Dynamic Programming algorithms
            "lcs" -> dpAlgorithms.longestCommonSubsequence()
            "knapsack" -> dpAlgorithms.knapsack01()
            "lis" -> dpAlgorithms.longestIncreasingSubsequence()
            "coin_change" -> dpAlgorithms.coinChange()
            // Greedy algorithms
            "activity_selection" -> greedyAlgorithms.activitySelection()
            "huffman_coding" -> greedyAlgorithms.huffmanCoding()
            // Backtracking algorithms
            "n_queens" -> backtrackingAlgorithms.nQueens()
            "sudoku_solver" -> backtrackingAlgorithms.sudokuSolver()
            // String matching algorithms
            "kmp" -> stringAlgorithms.kmpPatternMatching()
            // Divide and Conquer algorithms
            "ternary_search" -> divideAndConquerAlgorithms.ternarySearch(initialArray, targetInput)
            "quick_select" -> divideAndConquerAlgorithms.quickSelect(initialArray, kIndexInput)
            "maximum_subarray_dc" -> divideAndConquerAlgorithms.maximumSubarray(initialArray)
            else -> emptyList()
        }
    }
}
