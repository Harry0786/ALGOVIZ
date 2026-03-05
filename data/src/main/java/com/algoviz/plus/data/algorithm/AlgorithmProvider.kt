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
        ),
        // Graph Algorithms
        Algorithm(
            id = "bfs",
            name = "Breadth-First Search",
            category = AlgorithmCategory.GRAPH,
            description = "A graph traversal algorithm that explores vertices level by level using a queue.",
            timeComplexity = ComplexityInfo(
                best = "O(V + E)",
                average = "O(V + E)",
                worst = "O(V + E)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(V)",
                average = "O(V)",
                worst = "O(V)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 7
        ),
        Algorithm(
            id = "dfs",
            name = "Depth-First Search",
            category = AlgorithmCategory.GRAPH,
            description = "A graph traversal algorithm that explores as far as possible along each branch before backtracking.",
            timeComplexity = ComplexityInfo(
                best = "O(V + E)",
                average = "O(V + E)",
                worst = "O(V + E)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(V)",
                average = "O(V)",
                worst = "O(V)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 7
        ),
        Algorithm(
            id = "dijkstra",
            name = "Dijkstra's Algorithm",
            category = AlgorithmCategory.GRAPH,
            description = "A shortest path algorithm that finds the minimum distance from a source to all vertices in a weighted graph.",
            timeComplexity = ComplexityInfo(
                best = "O((V + E) log V)",
                average = "O((V + E) log V)",
                worst = "O((V + E) log V)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(V)",
                average = "O(V)",
                worst = "O(V)"
            ),
            difficultyLevel = DifficultyLevel.ADVANCED,
            defaultArraySize = 6
        ),
        // Tree Algorithms
        Algorithm(
            id = "bst_insertion",
            name = "BST Insertion",
            category = AlgorithmCategory.TREE,
            description = "Insert values into a Binary Search Tree maintaining the BST property.",
            timeComplexity = ComplexityInfo(
                best = "O(log n)",
                average = "O(log n)",
                worst = "O(n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 7
        ),
        Algorithm(
            id = "bst_search",
            name = "BST Search",
            category = AlgorithmCategory.TREE,
            description = "Search for a value in a Binary Search Tree using the BST property.",
            timeComplexity = ComplexityInfo(
                best = "O(log n)",
                average = "O(log n)",
                worst = "O(n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(1)",
                worst = "O(1)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 7
        ),
        Algorithm(
            id = "inorder_traversal",
            name = "Inorder Traversal",
            category = AlgorithmCategory.TREE,
            description = "Traverse a binary tree in Left-Root-Right order, producing sorted output for BST.",
            timeComplexity = ComplexityInfo(
                best = "O(n)",
                average = "O(n)",
                worst = "O(n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(h)",
                average = "O(h)",
                worst = "O(h)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER,
            defaultArraySize = 7
        ),
        Algorithm(
            id = "preorder_traversal",
            name = "Preorder Traversal",
            category = AlgorithmCategory.TREE,
            description = "Traverse a binary tree in Root-Left-Right order, useful for copying trees.",
            timeComplexity = ComplexityInfo(
                best = "O(n)",
                average = "O(n)",
                worst = "O(n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(h)",
                average = "O(h)",
                worst = "O(h)"
            ),
            difficultyLevel = DifficultyLevel.BEGINNER,
            defaultArraySize = 7
        ),
        // Dynamic Programming Algorithms
        Algorithm(
            id = "lcs",
            name = "Longest Common Subsequence",
            category = AlgorithmCategory.DYNAMIC_PROGRAMMING,
            description = "Find the longest subsequence common to two sequences using dynamic programming.",
            timeComplexity = ComplexityInfo(
                best = "O(m × n)",
                average = "O(m × n)",
                worst = "O(m × n)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(m × n)",
                average = "O(m × n)",
                worst = "O(m × n)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 7
        ),
        Algorithm(
            id = "knapsack",
            name = "0/1 Knapsack",
            category = AlgorithmCategory.DYNAMIC_PROGRAMMING,
            description = "Maximize value of items in a knapsack with weight constraint using dynamic programming.",
            timeComplexity = ComplexityInfo(
                best = "O(n × W)",
                average = "O(n × W)",
                worst = "O(n × W)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(n × W)",
                average = "O(n × W)",
                worst = "O(n × W)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 4
        ),
        Algorithm(
            id = "lis",
            name = "Longest Increasing Subsequence",
            category = AlgorithmCategory.DYNAMIC_PROGRAMMING,
            description = "Find the length of the longest subsequence where elements are in increasing order.",
            timeComplexity = ComplexityInfo(
                best = "O(n²)",
                average = "O(n²)",
                worst = "O(n²)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(n)",
                average = "O(n)",
                worst = "O(n)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 8
        ),
        Algorithm(
            id = "coin_change",
            name = "Coin Change",
            category = AlgorithmCategory.DYNAMIC_PROGRAMMING,
            description = "Find minimum number of coins needed to make a given amount using dynamic programming.",
            timeComplexity = ComplexityInfo(
                best = "O(n × amount)",
                average = "O(n × amount)",
                worst = "O(n × amount)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(amount)",
                average = "O(amount)",
                worst = "O(amount)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 3
        ),
        // Greedy Algorithms
        Algorithm(
            id = "activity_selection",
            name = "Activity Selection",
            category = AlgorithmCategory.GREEDY,
            description = "Select maximum number of non-overlapping activities using greedy approach",
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
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 6
        ),
        Algorithm(
            id = "huffman_coding",
            name = "Huffman Coding",
            category = AlgorithmCategory.GREEDY,
            description = "Build optimal prefix-free code for efficient data compression",
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
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 6
        ),
        // Backtracking Algorithms
        Algorithm(
            id = "n_queens",
            name = "N-Queens Problem",
            category = AlgorithmCategory.BACKTRACKING,
            description = "Place N queens on NxN chessboard such that no two queens threaten each other",
            timeComplexity = ComplexityInfo(
                best = "O(N!)",
                average = "O(N!)",
                worst = "O(N!)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(N)",
                average = "O(N)",
                worst = "O(N)"
            ),
            difficultyLevel = DifficultyLevel.ADVANCED,
            defaultArraySize = 4
        ),
        Algorithm(
            id = "sudoku_solver",
            name = "Sudoku Solver",
            category = AlgorithmCategory.BACKTRACKING,
            description = "Solve Sudoku puzzle using backtracking with constraint satisfaction",
            timeComplexity = ComplexityInfo(
                best = "O(1)",
                average = "O(n^m)",
                worst = "O(n^m)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(n²)",
                average = "O(n²)",
                worst = "O(n²)"
            ),
            difficultyLevel = DifficultyLevel.ADVANCED,
            defaultArraySize = 9
        ),
        // String Matching Algorithms
        Algorithm(
            id = "kmp",
            name = "KMP Pattern Matching",
            category = AlgorithmCategory.GREEDY,
            description = "Knuth-Morris-Pratt algorithm for efficient pattern matching in strings",
            timeComplexity = ComplexityInfo(
                best = "O(n + m)",
                average = "O(n + m)",
                worst = "O(n + m)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(m)",
                average = "O(m)",
                worst = "O(m)"
            ),
            difficultyLevel = DifficultyLevel.ADVANCED,
            defaultArraySize = 19
        ),
        // Additional Graph Algorithms
        Algorithm(
            id = "bellman_ford",
            name = "Bellman-Ford Algorithm",
            category = AlgorithmCategory.GRAPH,
            description = "Find shortest path from source to all vertices, handles negative edge weights",
            timeComplexity = ComplexityInfo(
                best = "O(VE)",
                average = "O(VE)",
                worst = "O(VE)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(V)",
                average = "O(V)",
                worst = "O(V)"
            ),
            difficultyLevel = DifficultyLevel.ADVANCED,
            defaultArraySize = 5
        ),
        Algorithm(
            id = "kruskal",
            name = "Kruskal's MST",
            category = AlgorithmCategory.GRAPH,
            description = "Find Minimum Spanning Tree using greedy edge selection with Union-Find",
            timeComplexity = ComplexityInfo(
                best = "O(E log E)",
                average = "O(E log E)",
                worst = "O(E log E)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(V + E)",
                average = "O(V + E)",
                worst = "O(V + E)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 6
        ),
        Algorithm(
            id = "prim",
            name = "Prim's MST",
            category = AlgorithmCategory.GRAPH,
            description = "Find Minimum Spanning Tree by growing a single component",
            timeComplexity = ComplexityInfo(
                best = "O(E log V)",
                average = "O(E log V)",
                worst = "O(V²)"
            ),
            spaceComplexity = ComplexityInfo(
                best = "O(V + E)",
                average = "O(V + E)",
                worst = "O(V + E)"
            ),
            difficultyLevel = DifficultyLevel.INTERMEDIATE,
            defaultArraySize = 6
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
