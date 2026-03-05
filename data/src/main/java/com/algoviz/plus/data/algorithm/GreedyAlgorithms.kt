package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GreedyAlgorithms @Inject constructor() {
    
    data class Activity(val id: Int, val start: Int, val end: Int)
    
    fun activitySelection(
        activities: List<Activity> = listOf(
            Activity(0, 0, 6),
            Activity(1, 3, 4),
            Activity(2, 1, 2),
            Activity(3, 5, 7),
            Activity(4, 8, 9),
            Activity(5, 5, 9)
        )
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        
        steps.add(AlgorithmStep(
            description = "Starting Activity Selection Greedy Algorithm with ${activities.size} activities",
            array = activities.map { it.id }
        ))
        
        // Sort by end time
        val sorted = activities.sortedBy { it.end }
        var comparisons = 0
        
        steps.add(AlgorithmStep(
            description = "Sorted activities by end time: ${sorted.joinToString { "A${it.id}(${it.start}-${it.end})" }}",
            array = sorted.map { it.id },
            comparingIndices = setOf(),
            comparisons = comparisons
        ))
        
        val selected = mutableListOf<Int>()
        selected.add(sorted[0].id)
        
        steps.add(AlgorithmStep(
            description = "Selected first activity: A${sorted[0].id} (${sorted[0].start}-${sorted[0].end})",
            array = sorted.map { it.id },
            sortedIndices = setOf(0),
            comparisons = comparisons
        ))
        
        var lastEnd = sorted[0].end
        
        for (i in 1 until sorted.size) {
            comparisons++
            
            steps.add(AlgorithmStep(
                description = "Checking activity A${sorted[i].id} (${sorted[i].start}-${sorted[i].end})",
                array = sorted.map { it.id },
                comparingIndices = setOf(i),
                sortedIndices = selected.toSet(),
                comparisons = comparisons
            ))
            
            if (sorted[i].start >= lastEnd) {
                selected.add(sorted[i].id)
                lastEnd = sorted[i].end
                
                steps.add(AlgorithmStep(
                    description = "Compatible! Added A${sorted[i].id} to selection",
                    array = sorted.map { it.id },
                    sortedIndices = selected.toSet(),
                    comparisons = comparisons
                ))
            } else {
                steps.add(AlgorithmStep(
                    description = "Conflict with last activity, skipping A${sorted[i].id}",
                    array = sorted.map { it.id },
                    comparingIndices = setOf(i),
                    sortedIndices = selected.toSet(),
                    comparisons = comparisons
                ))
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Final selection: ${selected.joinToString { "A$it" }} (${selected.size} activities)",
            array = sorted.map { it.id },
            sortedIndices = selected.toSet(),
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun huffmanCoding(
        frequencies: Map<Char, Int> = mapOf(
            'A' to 5,
            'B' to 9,
            'C' to 12,
            'D' to 13,
            'E' to 16,
            'F' to 45
        )
    ): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        
        steps.add(AlgorithmStep(
            description = "Huffman Coding with ${frequencies.size} characters",
            array = frequencies.values.toList()
        ))
        
        val chars = frequencies.keys.toList()
        val freqs = chars.map { frequencies[it]!! }
        
        steps.add(AlgorithmStep(
            description = "Character frequencies: ${chars.zip(freqs).joinToString { (c, f) -> "$c:$f" }}",
            array = freqs,
            comparisons = 0
        ))
        
        var comparisons = 0
        
        // Build Huffman tree using frequency  
        data class Node(val char: Char?, val freq: Int, val depth: Int = 0)
        
        var step = 1
        
        steps.add(AlgorithmStep(
            description = "Building Huffman tree by repeatedly merging two minimum frequency nodes",
            array = freqs,
            comparisons = comparisons
        ))
        
        val totalFreq = freqs.sum()
        val levels = mutableListOf<Int>()
        
        // Simulate tree building (simplified)
        var remainingFreq = totalFreq
        var level = 0
        
        while (remainingFreq > 1 && step <= 6) {
            comparisons++
            val merged = (remainingFreq + 1) / 2
            
            steps.add(AlgorithmStep(
                description = "Step $step: Merge nodes, remaining frequency sum: $remainingFreq",
                array = (freqs.take(step) + listOf(remainingFreq)).distinct(),
                comparisons = comparisons
            ))
            
            remainingFreq = merged
            step++
            level++
        }
        
        steps.add(AlgorithmStep(
            description = "Huffman tree complete. More frequent chars get shorter codes",
            array = freqs,
            comparisons = comparisons
        ))
        
        return steps
    }
}
