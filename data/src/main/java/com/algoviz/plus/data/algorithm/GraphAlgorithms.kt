package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Singleton
class GraphAlgorithms @Inject constructor() {
    
    private fun createCircularGraphLayout(nodeCount: Int): List<GraphNode> {
        val nodes = mutableListOf<GraphNode>()
        val radius = 0.35f
        val centerX = 0.5f
        val centerY = 0.5f
        
        for (i in 0 until nodeCount) {
            val angle = 2 * PI * i / nodeCount - PI / 2
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            nodes.add(GraphNode(id = i, x = x, y = y, label = i.toString()))
        }
        
        return nodes
    }
    
    fun bfs(nodeCount: Int = 7, startNode: Int = 0): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val nodes = createCircularGraphLayout(nodeCount)
        
        // Create edges for a sample graph (connected graph)
        val edges = mutableListOf<GraphEdge>()
        for (i in 0 until nodeCount) {
            // Connect to next node in circle
            edges.add(GraphEdge(from = i, to = (i + 1) % nodeCount, weight = 1))
            // Add some cross connections
            if (i + 3 < nodeCount) {
                edges.add(GraphEdge(from = i, to = i + 3, weight = 1))
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Starting BFS from node $startNode",
            graphData = GraphData(nodes = nodes, edges = edges)
        ))
        
        val visited = mutableSetOf<Int>()
        val queue: Queue<Int> = LinkedList()
        queue.offer(startNode)
        visited.add(startNode)
        
        steps.add(AlgorithmStep(
            description = "Added node $startNode to queue",
            graphData = GraphData(
                nodes = nodes,
                edges = edges,
                visitedNodes = visited.toSet(),
                activeNodes = setOf(startNode)
            )
        ))
        
        var comparisons = 0
        
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            
            steps.add(AlgorithmStep(
                description = "Processing node $current",
                comparisons = comparisons,
                graphData = GraphData(
                    nodes = nodes,
                    edges = edges,
                    visitedNodes = visited.toSet(),
                    activeNodes = setOf(current),
                    processedNodes = visited.filter { it != current }.toSet()
                )
            ))
            
            // Get neighbors
            val neighbors = edges.filter { it.from == current }.map { it.to }
            
            for (neighbor in neighbors) {
                comparisons++
                
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    queue.offer(neighbor)
                    
                    steps.add(AlgorithmStep(
                        description = "Discovered node $neighbor from node $current",
                        comparisons = comparisons,
                        graphData = GraphData(
                            nodes = nodes,
                            edges = edges,
                            visitedNodes = visited.toSet(),
                            activeNodes = setOf(neighbor),
                            processedNodes = visited.filter { it != neighbor && it != current }.toSet(),
                            activeEdges = setOf(Pair(current, neighbor))
                        )
                    ))
                } else {
                    steps.add(AlgorithmStep(
                        description = "Node $neighbor already visited",
                        comparisons = comparisons,
                        graphData = GraphData(
                            nodes = nodes,
                            edges = edges,
                            visitedNodes = visited.toSet(),
                            processedNodes = visited.filter { it != current }.toSet(),
                            activeNodes = setOf(current)
                        )
                    ))
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "BFS traversal complete. Visited ${visited.size} nodes.",
            comparisons = comparisons,
            graphData = GraphData(
                nodes = nodes,
                edges = edges,
                visitedNodes = visited.toSet(),
                processedNodes = visited.toSet()
            )
        ))
        
        return steps
    }
    
    fun dfs(nodeCount: Int = 7, startNode: Int = 0): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val nodes = createCircularGraphLayout(nodeCount)
        
        // Create edges for a sample graph
        val edges = mutableListOf<GraphEdge>()
        for (i in 0 until nodeCount) {
            edges.add(GraphEdge(from = i, to = (i + 1) % nodeCount, weight = 1))
            if (i + 3 < nodeCount) {
                edges.add(GraphEdge(from = i, to = i + 3, weight = 1))
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Starting DFS from node $startNode",
            graphData = GraphData(nodes = nodes, edges = edges)
        ))
        
        val visited = mutableSetOf<Int>()
        val stack = Stack<Int>()
        var comparisons = 0
        
        fun dfsRecursive(node: Int) {
            visited.add(node)
            
            steps.add(AlgorithmStep(
                description = "Visiting node $node",
                comparisons = comparisons,
                graphData = GraphData(
                    nodes = nodes,
                    edges = edges,
                    visitedNodes = visited.toSet(),
                    activeNodes = setOf(node)
                )
            ))
            
            val neighbors = edges.filter { it.from == node }.map { it.to }
            
            for (neighbor in neighbors) {
                comparisons++
                
                if (neighbor !in visited) {
                    steps.add(AlgorithmStep(
                        description = "Exploring edge from $node to $neighbor",
                        comparisons = comparisons,
                        graphData = GraphData(
                            nodes = nodes,
                            edges = edges,
                            visitedNodes = visited.toSet(),
                            activeNodes = setOf(node, neighbor),
                            activeEdges = setOf(Pair(node, neighbor))
                        )
                    ))
                    
                    dfsRecursive(neighbor)
                    
                    steps.add(AlgorithmStep(
                        description = "Backtracking to node $node",
                        comparisons = comparisons,
                        graphData = GraphData(
                            nodes = nodes,
                            edges = edges,
                            visitedNodes = visited.toSet(),
                            activeNodes = setOf(node),
                            processedNodes = visited.toSet()
                        )
                    ))
                }
            }
        }
        
        dfsRecursive(startNode)
        
        steps.add(AlgorithmStep(
            description = "DFS traversal complete. Visited ${visited.size} nodes.",
            comparisons = comparisons,
            graphData = GraphData(
                nodes = nodes,
                edges = edges,
                visitedNodes = visited.toSet(),
                processedNodes = visited.toSet()
            )
        ))
        
        return steps
    }
    
    fun dijkstra(nodeCount: Int = 6, startNode: Int = 0, endNode: Int = 5): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val nodes = createCircularGraphLayout(nodeCount)
        
        // Create weighted edges
        val edges = mutableListOf<GraphEdge>()
        for (i in 0 until nodeCount) {
            val weight = (1..5).random()
            edges.add(GraphEdge(from = i, to = (i + 1) % nodeCount, weight = weight))
            if (i + 2 < nodeCount) {
                edges.add(GraphEdge(from = i, to = i + 2, weight = (2..7).random()))
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Starting Dijkstra's algorithm from node $startNode to node $endNode",
            graphData = GraphData(nodes = nodes, edges = edges)
        ))
        
        val distances = mutableMapOf<Int, Int>()
        val visited = mutableSetOf<Int>()
        val priorityQueue = PriorityQueue<Pair<Int, Int>>(compareBy { it.second })
        
        for (i in 0 until nodeCount) {
            distances[i] = if (i == startNode) 0 else Int.MAX_VALUE
        }
        
        priorityQueue.offer(Pair(startNode, 0))
        
        var comparisons = 0
        
        while (priorityQueue.isNotEmpty()) {
            val (currentNode, currentDist) = priorityQueue.poll()
            
            if (currentNode in visited) continue
            
            visited.add(currentNode)
            
            steps.add(AlgorithmStep(
                description = "Processing node $currentNode with distance ${distances[currentNode]}",
                comparisons = comparisons,
                graphData = GraphData(
                    nodes = nodes,
                    edges = edges,
                    visitedNodes = visited.toSet(),
                    activeNodes = setOf(currentNode),
                    distances = distances.toMap()
                )
            ))
            
            if (currentNode == endNode) {
                steps.add(AlgorithmStep(
                    description = "Reached destination! Shortest distance: ${distances[endNode]}",
                    comparisons = comparisons,
                    graphData = GraphData(
                        nodes = nodes,
                        edges = edges,
                        visitedNodes = visited.toSet(),
                        processedNodes = visited.toSet(),
                        distances = distances.toMap()
                    )
                ))
                break
            }
            
            val neighbors = edges.filter { it.from == currentNode }
            
            for (edge in neighbors) {
                comparisons++
                val neighbor = edge.to
                val newDist = distances[currentNode]!! + edge.weight
                
                if (newDist < distances[neighbor]!!) {
                    distances[neighbor] = newDist
                    priorityQueue.offer(Pair(neighbor, newDist))
                    
                    steps.add(AlgorithmStep(
                        description = "Updated distance to node $neighbor: $newDist (via node $currentNode)",
                        comparisons = comparisons,
                        graphData = GraphData(
                            nodes = nodes,
                            edges = edges,
                            visitedNodes = visited.toSet(),
                            activeNodes = setOf(currentNode, neighbor),
                            activeEdges = setOf(Pair(currentNode, neighbor)),
                            distances = distances.toMap()
                        )
                    ))
                }
            }
        }
        
        return steps
    }
    
    fun bellmanFord(nodeCount: Int = 5, startNode: Int = 0, endNode: Int = 4): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val nodes = createCircularGraphLayout(nodeCount)
        
        // Create weighted edges with some negative weights
        val edges = mutableListOf<GraphEdge>()
        edges.add(GraphEdge(from = 0, to = 1, weight = -1))
        edges.add(GraphEdge(from = 0, to = 2, weight = 4))
        edges.add(GraphEdge(from = 1, to = 2, weight = 3))
        edges.add(GraphEdge(from = 1, to = 3, weight = 2))
        edges.add(GraphEdge(from = 1, to = 4, weight = 2))
        edges.add(GraphEdge(from = 3, to = 2, weight = 5))
        edges.add(GraphEdge(from = 3, to = 1, weight = 1))
        edges.add(GraphEdge(from = 4, to = 3, weight = -3))
        
        steps.add(AlgorithmStep(
            description = "Bellman-Ford: Find shortest path from node $startNode (handles negative weights)",
            graphData = GraphData(nodes = nodes, edges = edges)
        ))
        
        val distances = mutableMapOf<Int, Int>()
        for (i in 0 until nodeCount) {
            distances[i] = if (i == startNode) 0 else Int.MAX_VALUE
        }
        
        var comparisons = 0
        
        // Relax edges n-1 times
        for (iteration in 0 until nodeCount - 1) {
            for (edge in edges) {
                comparisons++
                
                if (distances[edge.from]!! != Int.MAX_VALUE &&
                    distances[edge.from]!! + edge.weight < distances[edge.to]!!) {
                    
                    distances[edge.to] = distances[edge.from]!! + edge.weight
                    
                    steps.add(AlgorithmStep(
                        description = "Iteration ${iteration + 1}: Updated distance to node ${edge.to}: ${distances[edge.to]}",
                        graphData = GraphData(
                            nodes = nodes,
                            edges = edges,
                            activeNodes = setOf(edge.from, edge.to),
                            activeEdges = setOf(Pair(edge.from, edge.to)),
                            distances = distances.toMap()
                        ),
                        comparisons = comparisons
                    ))
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Shortest distances from node $startNode: ${distances.toList().joinToString { (k, v) -> "$k→$v" }}",
            graphData = GraphData(
                nodes = nodes,
                edges = edges,
                distances = distances.toMap()
            ),
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun kruskalMST(nodeCount: Int = 6): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val nodes = createCircularGraphLayout(nodeCount)
        
        // Create weighted edges for MST
        val edges = mutableListOf<GraphEdge>()
        edges.add(GraphEdge(from = 0, to = 1, weight = 4))
        edges.add(GraphEdge(from = 0, to = 2, weight = 2))
        edges.add(GraphEdge(from = 1, to = 2, weight = 1))
        edges.add(GraphEdge(from = 1, to = 3, weight = 5))
        edges.add(GraphEdge(from = 2, to = 3, weight = 8))
        edges.add(GraphEdge(from = 2, to = 4, weight = 10))
        edges.add(GraphEdge(from = 3, to = 4, weight = 2))
        edges.add(GraphEdge(from = 3, to = 5, weight = 6))
        edges.add(GraphEdge(from = 4, to = 5, weight = 3))
        
        steps.add(AlgorithmStep(
            description = "Kruskal's Algorithm: Minimum Spanning Tree",
            graphData = GraphData(nodes = nodes, edges = edges)
        ))
        
        // Sort edges by weight
        val sortedEdges = edges.sortedBy { it.weight }
        
        steps.add(AlgorithmStep(
            description = "Sorted edges by weight: ${sortedEdges.joinToString { "${it.from}-${it.to}(${it.weight})" }}",
            graphData = GraphData(nodes = nodes, edges = sortedEdges)
        ))
        
        // Union-Find
        val parent = IntArray(nodeCount) { it }
        
        fun find(x: Int): Int {
            if (parent[x] != x) {
                parent[x] = find(parent[x])
            }
            return parent[x]
        }
        
        fun union(x: Int, y: Int): Boolean {
            val px = find(x)
            val py = find(y)
            return if (px != py) {
                parent[px] = py
                true
            } else {
                false
            }
        }
        
        val mstEdges = mutableListOf<GraphEdge>()
        var comparisons = 0
        var weight = 0
        
        for (edge in sortedEdges) {
            comparisons++
            
            if (union(edge.from, edge.to)) {
                mstEdges.add(edge)
                weight += edge.weight
                
                steps.add(AlgorithmStep(
                    description = "Added edge ${edge.from}-${edge.to} (weight ${edge.weight}) to MST",
                    graphData = GraphData(
                        nodes = nodes,
                        edges = mstEdges,
                        activeEdges = setOf(Pair(edge.from, edge.to))
                    ),
                    comparisons = comparisons
                ))
            } else {
                steps.add(AlgorithmStep(
                    description = "Skipped edge ${edge.from}-${edge.to} (would create cycle)",
                    comparisons = comparisons,
                    graphData = GraphData(nodes = nodes, edges = mstEdges)
                ))
            }
            
            if (mstEdges.size == nodeCount - 1) break
        }
        
        steps.add(AlgorithmStep(
            description = "MST complete: ${mstEdges.size} edges, total weight: $weight",
            graphData = GraphData(nodes = nodes, edges = mstEdges),
            comparisons = comparisons
        ))
        
        return steps
    }
    
    fun primMST(nodeCount: Int = 6, startNode: Int = 0): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        val nodes = createCircularGraphLayout(nodeCount)
        
        // Create weighted edges for MST
        val adjacency = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()
        for (i in 0 until nodeCount) {
            adjacency[i] = mutableListOf()
        }
        
        // Add edges
        val allEdges = listOf(
            Triple(0, 1, 4), Triple(0, 2, 2),
            Triple(1, 2, 1), Triple(1, 3, 5),
            Triple(2, 3, 8), Triple(2, 4, 10),
            Triple(3, 4, 2), Triple(3, 5, 6),
            Triple(4, 5, 3)
        )
        
        val edges = mutableListOf<GraphEdge>()
        for ((u, v, w) in allEdges) {
            adjacency[u]!!.add(Pair(v, w))
            adjacency[v]!!.add(Pair(u, w))
            edges.add(GraphEdge(from = u, to = v, weight = w))
        }
        
        steps.add(AlgorithmStep(
            description = "Prim's Algorithm: Minimum Spanning Tree starting from node $startNode",
            graphData = GraphData(nodes = nodes, edges = edges)
        ))
        
        val inMST = mutableSetOf<Int>()
        val mstEdges = mutableListOf<GraphEdge>()
        var comparisons = 0
        var weight = 0
        
        inMST.add(startNode)
        steps.add(AlgorithmStep(
            description = "Added node $startNode to MST",
            graphData = GraphData(
                nodes = nodes,
                edges = mstEdges,
                visitedNodes = inMST
            )
        ))
        
        while (inMST.size < nodeCount) {
            var minWeight = Int.MAX_VALUE
            var minU = -1
            var minV = -1
            
            // Find minimum weight edge from MST to non-MST vertex
            for (u in inMST) {
                for ((v, w) in adjacency[u]!!) {
                    comparisons++
                    if (v !in inMST && w < minWeight) {
                        minWeight = w
                        minU = u
                        minV = v
                    }
                }
            }
            
            if (minU != -1) {
                inMST.add(minV)
                mstEdges.add(GraphEdge(from = minU, to = minV, weight = minWeight))
                weight += minWeight
                
                steps.add(AlgorithmStep(
                    description = "Added edge $minU-$minV (weight $minWeight) to MST",
                    graphData = GraphData(
                        nodes = nodes,
                        edges = mstEdges,
                        activeEdges = setOf(Pair(minU, minV)),
                        visitedNodes = inMST
                    ),
                    comparisons = comparisons
                ))
            }
        }
        
        steps.add(AlgorithmStep(
            description = "MST complete: ${mstEdges.size} edges, total weight: $weight",
            graphData = GraphData(nodes = nodes, edges = mstEdges),
            comparisons = comparisons
        ))
        
        return steps
    }
}
