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
    private val stringAlgorithms: StringAlgorithms
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
            "linear_search" -> searchingAlgorithms.linearSearch(initialArray)
            "binary_search" -> searchingAlgorithms.binarySearch(initialArray)
            // Graph algorithms
            "bfs" -> graphAlgorithms.bfs()
            "dfs" -> graphAlgorithms.dfs()
            "dijkstra" -> graphAlgorithms.dijkstra()
            "bellman_ford" -> graphAlgorithms.bellmanFord()
            "kruskal" -> graphAlgorithms.kruskalMST()
            "prim" -> graphAlgorithms.primMST()
            // Tree algorithms
            "bst_insertion" -> treeAlgorithms.bstInsertion()
            "bst_search" -> treeAlgorithms.bstSearch()
            "inorder_traversal" -> treeAlgorithms.inorderTraversal()
            "preorder_traversal" -> treeAlgorithms.preorderTraversal()
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
            else -> emptyList()
        }
    }
}
