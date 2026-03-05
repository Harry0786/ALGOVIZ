package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DPAlgorithms @Inject constructor() {
    
    fun longestCommonSubsequence(
        str1: String = "AGGTAB",
        str2: String = "GXTXAYB"
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val m = str1.length
        val n = str2.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        
        steps.add(AlgorithmStep(
            description = "Finding LCS of \"$str1\" and \"$str2\"",
            matrix = dp.map { it.toList() },
            array = listOf(m, n)
        ))
        
        var comparisons = 0
        
        for (i in 0..m) {
            for (j in 0..n) {
                if (i == 0 || j == 0) {
                    dp[i][j] = 0
                    steps.add(AlgorithmStep(
                        description = "Initializing dp[$i][$j] = 0 (base case)",
                        matrix = dp.map { it.toList() },
                        array = listOf(i, j),
                        comparingIndices = setOf(i, j)
                    ))
                } else if (str1[i - 1] == str2[j - 1]) {
                    comparisons++
                    dp[i][j] = dp[i - 1][j - 1] + 1
                    steps.add(AlgorithmStep(
                        description = "Match: '${str1[i-1]}' == '${str2[j-1]}', dp[$i][$j] = dp[${i-1}][${j-1}] + 1 = ${dp[i][j]}",
                        matrix = dp.map { it.toList() },
                        array = listOf(i, j),
                        comparingIndices = setOf(i, j),
                        comparisons = comparisons
                    ))
                } else {
                    comparisons++
                    dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
                    steps.add(AlgorithmStep(
                        description = "No match: '${str1[i-1]}' != '${str2[j-1]}', dp[$i][$j] = max(${dp[i-1][j]}, ${dp[i][j-1]}) = ${dp[i][j]}",
                        matrix = dp.map { it.toList() },
                        array = listOf(i, j),
                        comparingIndices = setOf(i, j),
                        comparisons = comparisons
                    ))
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "LCS length is ${dp[m][n]}",
            matrix = dp.map { it.toList() },
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun knapsack01(
        weights: List<Int> = listOf(2, 3, 4, 5),
        values: List<Int> = listOf(3, 4, 5, 6),
        capacity: Int = 8
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val n = weights.size
        val dp = Array(n + 1) { IntArray(capacity + 1) }
        
        steps.add(AlgorithmStep(
            description = "0/1 Knapsack: capacity=$capacity, items=${weights.size}",
            matrix = dp.map { it.toList() },
            array = weights + values
        ))
        
        var comparisons = 0
        
        for (i in 0..n) {
            for (w in 0..capacity) {
                if (i == 0 || w == 0) {
                    dp[i][w] = 0
                    steps.add(AlgorithmStep(
                        description = "Base case: dp[$i][$w] = 0",
                        matrix = dp.map { it.toList() },
                        array = listOf(i, w),
                        comparingIndices = setOf(i, w)
                    ))
                } else if (weights[i - 1] <= w) {
                    comparisons++
                    val include = values[i - 1] + dp[i - 1][w - weights[i - 1]]
                    val exclude = dp[i - 1][w]
                    dp[i][w] = max(include, exclude)
                    
                    steps.add(AlgorithmStep(
                        description = "Item ${i-1}: weight=${weights[i-1]}, value=${values[i-1]}. " +
                                "Include=$include, Exclude=$exclude. Choose ${dp[i][w]}",
                        matrix = dp.map { it.toList() },
                        array = listOf(i, w),
                        comparingIndices = setOf(i, w),
                        comparisons = comparisons
                    ))
                } else {
                    dp[i][w] = dp[i - 1][w]
                    steps.add(AlgorithmStep(
                        description = "Item ${i-1} too heavy (${weights[i-1]} > $w), exclude it",
                        matrix = dp.map { it.toList() },
                        array = listOf(i, w),
                        comparingIndices = setOf(i, w),
                        comparisons = comparisons
                    ))
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Maximum value achievable: ${dp[n][capacity]}",
            matrix = dp.map { it.toList() },
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun longestIncreasingSubsequence(
        arr: List<Int> = listOf(10, 9, 2, 5, 3, 7, 101, 18)
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val n = arr.size
        val dp = IntArray(n) { 1 }
        
        steps.add(AlgorithmStep(
            description = "Finding LIS in array: ${arr.joinToString(", ")}",
            array = arr,
            matrix = listOf(dp.toList())
        ))
        
        var comparisons = 0
        
        for (i in 1 until n) {
            for (j in 0 until i) {
                comparisons++
                
                if (arr[i] > arr[j]) {
                    val newLength = dp[j] + 1
                    if (newLength > dp[i]) {
                        dp[i] = newLength
                        
                        steps.add(AlgorithmStep(
                            description = "arr[$i]=${arr[i]} > arr[$j]=${arr[j]}, update dp[$i] = ${dp[i]}",
                            array = arr,
                            matrix = listOf(dp.toList()),
                            comparingIndices = setOf(i, j),
                            comparisons = comparisons
                        ))
                    }
                } else {
                    steps.add(AlgorithmStep(
                        description = "arr[$i]=${arr[i]} <= arr[$j]=${arr[j]}, skip",
                        array = arr,
                        matrix = listOf(dp.toList()),
                        comparingIndices = setOf(i, j),
                        comparisons = comparisons
                    ))
                }
            }
        }
        
        val maxLength = dp.maxOrNull() ?: 0
        steps.add(AlgorithmStep(
            description = "Longest increasing subsequence length: $maxLength",
            array = arr,
            matrix = listOf(dp.toList()),
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun coinChange(
        coins: List<Int> = listOf(1, 2, 5),
        amount: Int = 11
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val dp = IntArray(amount + 1) { Int.MAX_VALUE }
        dp[0] = 0
        
        steps.add(AlgorithmStep(
            description = "Coin Change: coins=${coins.joinToString()}, amount=$amount",
            array = coins,
            matrix = listOf(dp.toList())
        ))
        
        var comparisons = 0
        
        for (i in 1..amount) {
            for (coin in coins) {
                comparisons++
                
                if (coin <= i && dp[i - coin] != Int.MAX_VALUE) {
                    val newCount = dp[i - coin] + 1
                    if (newCount < dp[i]) {
                        dp[i] = newCount
                        
                        steps.add(AlgorithmStep(
                            description = "For amount $i, using coin $coin: dp[$i] = ${dp[i]} coins",
                            array = coins + listOf(i),
                            matrix = listOf(dp.map { if (it == Int.MAX_VALUE) -1 else it }),
                            comparingIndices = setOf(i),
                            comparisons = comparisons
                        ))
                    }
                }
            }
        }
        
        val result = if (dp[amount] == Int.MAX_VALUE) -1 else dp[amount]
        steps.add(AlgorithmStep(
            description = if (result == -1) "Amount cannot be made with given coins" 
                         else "Minimum coins needed: $result",
            array = coins,
            matrix = listOf(dp.map { if (it == Int.MAX_VALUE) -1 else it }),
            comparisons = comparisons
        ))
        
        return steps
    }
}
