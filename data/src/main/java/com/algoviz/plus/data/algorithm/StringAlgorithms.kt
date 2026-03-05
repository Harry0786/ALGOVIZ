package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringAlgorithms @Inject constructor() {
    
    fun kmpPatternMatching(
        text: String = "ABABDABACDABABCABAB",
        pattern: String = "ABABCABAB"
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        
        steps.add(AlgorithmStep(
            description = "KMP Pattern Matching: Find \"$pattern\" in \"$text\"",
            array = listOf(text.length, pattern.length)
        ))
        
        // Build LPS (Longest Proper Prefix which is also Suffix) array
        val lps = IntArray(pattern.length)
        var len = 0
        var i = 1
        var comparisons = 0
        
        steps.add(AlgorithmStep(
            description = "Building LPS array for pattern \"$pattern\"",
            array = pattern.map { it.code },
            comparisons = 0
        ))
        
        while (i < pattern.length) {
            comparisons++
            
            if (pattern[i] == pattern[len]) {
                len++
                lps[i] = len
                steps.add(AlgorithmStep(
                    description = "Match at position $i: '${pattern[i]}' == '${pattern[len - 1]}', LPS[$i] = $len",
                    array = lps.toList(),
                    comparingIndices = setOf(i, len),
                    comparisons = comparisons
                ))
                i++
            } else {
                if (len != 0) {
                    len = lps[len - 1]
                    steps.add(AlgorithmStep(
                        description = "Mismatch at position $i, backtracking using LPS",
                        array = lps.toList(),
                        comparingIndices = setOf(i, len),
                        comparisons = comparisons
                    ))
                } else {
                    lps[i] = 0
                    i++
                    steps.add(AlgorithmStep(
                        description = "No match at position $i, moving forward",
                        array = lps.toList(),
                        comparisons = comparisons
                    ))
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "LPS array complete: ${lps.toList()}",
            array = lps.toList(),
            comparisons = comparisons
        ))
        
        // Search pattern in text
        val matches = mutableListOf<Int>()
        i = 0
        var j = 0
        
        steps.add(AlgorithmStep(
            description = "Searching for pattern in text",
            array = text.map { it.code },
            comparisons = comparisons
        ))
        
        while (i < text.length) {
            comparisons++
            
            if (pattern[j] == text[i]) {
                steps.add(AlgorithmStep(
                    description = "Text[$i]='${text[i]}' matches Pattern[$j]='${pattern[j]}'",
                    array = text.map { it.code },
                    comparingIndices = setOf(i, j),
                    comparisons = comparisons
                ))
                i++
                j++
            } else {
                steps.add(AlgorithmStep(
                    description = "Text[$i]='${text[i]}' != Pattern[$j]='${pattern[j]}'",
                    array = text.map { it.code },
                    comparingIndices = setOf(i, j),
                    comparisons = comparisons
                ))
            }
            
            if (j == pattern.length) {
                matches.add(i - j)
                steps.add(AlgorithmStep(
                    description = "Pattern found at index ${i - j}",
                    array = text.map { it.code },
                    sortedIndices = setOf(i - j),
                    comparisons = comparisons
                ))
                j = lps[j - 1]
            } else if (i < text.length && pattern[j] != text[i]) {
                if (j != 0) {
                    j = lps[j - 1]
                } else {
                    i++
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Pattern \"$pattern\" found ${matches.size} time(s) at indices: ${matches.joinToString()}",
            array = text.map { it.code },
            sortedIndices = matches.toSet(),
            comparisons = comparisons
        ))
        
        return steps
    }
}
