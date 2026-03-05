package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BacktrackingAlgorithms @Inject constructor() {
    
    fun nQueens(n: Int = 4): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val board = Array(n) { IntArray(n) }
        val solutions = mutableListOf<Array<IntArray>>()
        var comparisons = 0
        
        steps.add(AlgorithmStep(
            description = "N-Queens problem: Place $n queens on ${n}x$n board",
            array = listOf(n),
            comparisons = 0
        ))
        
        fun isSafe(row: Int, col: Int): Boolean {
            comparisons++
            
            // Check left side
            for (j in 0 until col) {
                if (board[row][j] == 1) return false
            }
            
            // Check upper left diagonal
            var i = row
            var j = col
            while (i >= 0 && j >= 0) {
                if (board[i][j] == 1) return false
                i--
                j--
            }
            
            // Check lower left diagonal
            i = row
            j = col
            while (i < n && j >= 0) {
                if (board[i][j] == 1) return false
                i++
                j--
            }
            
            return true
        }
        
        fun solve(col: Int, queenCount: Int) {
            if (col >= n) {
                if (queenCount == n) {
                    steps.add(AlgorithmStep(
                        description = "Found solution #${solutions.size + 1}",
                        array = board.map { it.sum() },
                        sortedIndices = (0 until n).toSet(),
                        comparisons = comparisons
                    ))
                    solutions.add(Array(n) { i -> board[i].copyOf() })
                }
                return
            }
            
            for (row in 0 until n) {
                comparisons++
                
                if (isSafe(row, col)) {
                    board[row][col] = 1
                    
                    steps.add(AlgorithmStep(
                        description = "Placed queen at ($row, $col)",
                        array = board.map { it.sum() },
                        comparingIndices = setOf(row, col),
                        comparisons = comparisons
                    ))
                    
                    solve(col + 1, queenCount + 1)
                    
                    board[row][col] = 0
                    
                    steps.add(AlgorithmStep(
                        description = "Backtracking from ($row, $col)",
                        array = board.map { it.sum() },
                        comparingIndices = setOf(row, col),
                        comparisons = comparisons
                    ))
                } else {
                    steps.add(AlgorithmStep(
                        description = "Cannot place queen at ($row, $col) - unsafe position",
                        array = board.map { it.sum() },
                        comparingIndices = setOf(row, col),
                        comparisons = comparisons
                    ))
                }
            }
        }
        
        solve(0, 0)
        
        steps.add(AlgorithmStep(
            description = "$n-Queens: Found ${solutions.size} solution(s)",
            array = listOf(n, solutions.size),
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun sudokuSolver(): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        
        // Sample incomplete Sudoku board (0 = empty)
        val board = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )
        
        var comparisons = 0
        
        steps.add(AlgorithmStep(
            description = "Sudoku Solver using Backtracking",
            array = board.flatMap { it.toList() }.take(9)
        ))
        
        steps.add(AlgorithmStep(
            description = "Starting to fill empty cells (0s) with valid digits 1-9",
            array = (1..9).toList()
        ))
        
        var cellCount = 0
        
        fun isValid(row: Int, col: Int, num: Int): Boolean {
            comparisons++
            
            // Check row
            for (j in 0 until 9) {
                if (board[row][j] == num) return false
            }
            
            // Check column
            for (i in 0 until 9) {
                if (board[i][col] == num) return false
            }
            
            // Check 3x3 box
            val boxRow = (row / 3) * 3
            val boxCol = (col / 3) * 3
            for (i in boxRow until boxRow + 3) {
                for (j in boxCol until boxCol + 3) {
                    if (board[i][j] == num) return false
                }
            }
            
            return true
        }
        
        fun solve(): Boolean {
            for (row in 0 until 9) {
                for (col in 0 until 9) {
                    if (board[row][col] == 0) {
                        cellCount++
                        
                        for (num in 1..9) {
                            if (isValid(row, col, num)) {
                                board[row][col] = num
                                
                                if (cellCount <= 15) { // Only show first few steps
                                    steps.add(AlgorithmStep(
                                        description = "Cell ($row, $col): trying $num",
                                        array = board.flatMap { it.toList() }.take(9),
                                        comparingIndices = setOf(row, col),
                                        comparisons = comparisons
                                    ))
                                }
                                
                                if (solve()) return true
                                
                                board[row][col] = 0
                            }
                        }
                        return false
                    }
                }
            }
            return true
        }
        
        solve()
        
        steps.add(AlgorithmStep(
            description = "Sudoku solved! Filled $cellCount empty cells",
            array = board.flatMap { it.toList() }.take(9),
            comparisons = comparisons
        ))
        
        return steps
    }
}
