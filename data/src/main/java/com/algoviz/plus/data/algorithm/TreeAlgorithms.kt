package com.algoviz.plus.data.algorithm

import com.algoviz.plus.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TreeAlgorithms @Inject constructor() {
    
    private data class BSTNode(
        var value: Int,
        var left: BSTNode? = null,
        var right: BSTNode? = null
    )
    
    private fun buildTreeData(root: BSTNode?, activeNode: Int? = null, 
                              visitedNodes: Set<Int> = emptySet(),
                              highlightedNodes: Set<Int> = emptySet()): TreeData {
        val nodes = mutableListOf<TreeNode>()
        var nodeId = 0
        
        fun traverse(node: BSTNode?, parentId: Int?, isLeft: Boolean?, level: Int) {
            if (node == null) return
            
            val currentId = nodeId++
            nodes.add(TreeNode(
                id = currentId,
                value = node.value,
                parentId = parentId,
                isLeftChild = isLeft,
                level = level
            ))
            
            traverse(node.left, currentId, true, level + 1)
            traverse(node.right, currentId, false, level + 1)
        }
        
        traverse(root, null, null, 0)
        
        return TreeData(
            nodes = nodes,
            activeNode = activeNode,
            visitedNodes = visitedNodes,
            highlightedNodes = highlightedNodes
        )
    }
    
    fun bstInsertion(values: List<Int> = listOf(50, 30, 70, 20, 40, 60, 80)): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        var root: BSTNode? = null
        var comparisons = 0
        
        steps.add(AlgorithmStep(
            description = "Starting BST insertion with values: ${values.joinToString(", ")}",
            treeData = TreeData(nodes = emptyList())
        ))
        
        for (value in values) {
            if (root == null) {
                root = BSTNode(value)
                steps.add(AlgorithmStep(
                    description = "Inserted $value as root",
                    comparisons = comparisons,
                    treeData = buildTreeData(root, highlightedNodes = setOf(0))
                ))
                continue
            }
            
            var current = root
            var parent: BSTNode? = null
            var isLeftChild = false
            val visitedValues = mutableSetOf<Int>()
            
            while (current != null) {
                comparisons++
                visitedValues.add(current.value)
                parent = current
                
                steps.add(AlgorithmStep(
                    description = "Comparing $value with ${current.value}",
                    comparisons = comparisons,
                    treeData = buildTreeData(root, visitedNodes = visitedValues)
                ))
                
                if (value < current.value) {
                    current = current.left
                    isLeftChild = true
                } else if (value > current.value) {
                    current = current.right
                    isLeftChild = false
                } else {
                    steps.add(AlgorithmStep(
                        description = "Value $value already exists in tree",
                        comparisons = comparisons,
                        treeData = buildTreeData(root, visitedNodes = visitedValues)
                    ))
                    break
                }
            }
            
            if (current == null && parent != null) {
                if (isLeftChild) {
                    parent.left = BSTNode(value)
                } else {
                    parent.right = BSTNode(value)
                }
                
                visitedValues.add(value)
                steps.add(AlgorithmStep(
                    description = "Inserted $value as ${if (isLeftChild) "left" else "right"} child of ${parent.value}",
                    comparisons = comparisons,
                    treeData = buildTreeData(root, highlightedNodes = visitedValues)
                ))
            }
        }
        
        steps.add(AlgorithmStep(
            description = "BST construction complete with ${values.size} nodes",
            comparisons = comparisons,
            treeData = buildTreeData(root)
        ))
        
        return steps
    }
    
    fun bstSearch(initialValues: List<Int> = listOf(50, 30, 70, 20, 40, 60, 80), 
                  searchValue: Int = 40): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        var root: BSTNode? = null
        
        // Build the tree first
        for (value in initialValues) {
            if (root == null) {
                root = BSTNode(value)
            } else {
                var current: BSTNode? = root
                while (current != null) {
                    when {
                        value < current.value -> {
                            if (current.left == null) {
                                current.left = BSTNode(value)
                                break
                            }
                            current = current.left
                        }
                        value > current.value -> {
                            if (current.right == null) {
                                current.right = BSTNode(value)
                                break
                            }
                            current = current.right
                        }
                        else -> break
                    }
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Searching for value $searchValue in BST",
            treeData = buildTreeData(root)
        ))
        
        var current = root
        val visitedValues = mutableSetOf<Int>()
        var comparisons = 0
        var found = false
        
        while (current != null) {
            comparisons++
            val currentValue = current.value
            visitedValues.add(currentValue)
            
            steps.add(AlgorithmStep(
                description = "Comparing $searchValue with $currentValue",
                comparisons = comparisons,
                treeData = buildTreeData(root, visitedNodes = visitedValues)
            ))
            
            if (searchValue == currentValue) {
                found = true
                steps.add(AlgorithmStep(
                    description = "Found $searchValue! Search successful.",
                    comparisons = comparisons,
                    treeData = buildTreeData(root, visitedNodes = visitedValues, highlightedNodes = setOf(currentValue))
                ))
                break
            } else if (searchValue < currentValue) {
                steps.add(AlgorithmStep(
                    description = "$searchValue < $currentValue, going left",
                    comparisons = comparisons,
                    treeData = buildTreeData(root, visitedNodes = visitedValues)
                ))
                current = current.left
            } else {
                steps.add(AlgorithmStep(
                    description = "$searchValue > $currentValue, going right",
                    comparisons = comparisons,
                    treeData = buildTreeData(root, visitedNodes = visitedValues)
                ))
                current = current.right
            }
        }
        
        if (!found) {
            steps.add(AlgorithmStep(
                description = "Value $searchValue not found in tree",
                comparisons = comparisons,
                treeData = buildTreeData(root, visitedNodes = visitedValues)
            ))
        }
        
        return steps
    }
    
    fun inorderTraversal(values: List<Int> = listOf(50, 30, 70, 20, 40, 60, 80)): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        var root: BSTNode? = null
        
        // Build the tree
        for (value in values) {
            if (root == null) {
                root = BSTNode(value)
            } else {
                var current: BSTNode? = root
                while (current != null) {
                    if (value < current.value) {
                        if (current.left == null) {
                            current.left = BSTNode(value)
                            break
                        }
                        current = current.left
                    } else if (value > current.value) {
                        if (current.right == null) {
                            current.right = BSTNode(value)
                            break
                        }
                        current = current.right
                    } else {
                        break
                    }
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Starting in-order traversal (Left → Root → Right)",
            treeData = buildTreeData(root)
        ))
        
        val visitedNodes = mutableSetOf<Int>()
        val result = mutableListOf<Int>()
        
        fun inorder(node: BSTNode?) {
            if (node == null) return
            
            inorder(node.left)
            
            visitedNodes.add(node.value)
            result.add(node.value)
            
            steps.add(AlgorithmStep(
                description = "Visiting node ${node.value}. Traversal so far: ${result.joinToString(", ")}",
                treeData = buildTreeData(root, visitedNodes = visitedNodes, highlightedNodes = setOf(node.value))
            ))
            
            inorder(node.right)
        }
        
        inorder(root)
        
        steps.add(AlgorithmStep(
            description = "In-order traversal complete: ${result.joinToString(", ")}",
            treeData = buildTreeData(root, visitedNodes = visitedNodes)
        ))
        
        return steps
    }
    
    fun preorderTraversal(values: List<Int> = listOf(50, 30, 70, 20, 40, 60, 80)): List<AlgorithmStep> {
        val steps = mutableListOf<AlgorithmStep>()
        var root: BSTNode? = null
        
        // Build the tree
        for (value in values) {
            if (root == null) {
                root = BSTNode(value)
            } else {
                var current: BSTNode? = root
                while (current != null) {
                    if (value < current.value) {
                        if (current.left == null) {
                            current.left = BSTNode(value)
                            break
                        }
                        current = current.left
                    } else if (value > current.value) {
                        if (current.right == null) {
                            current.right = BSTNode(value)
                            break
                        }
                        current = current.right
                    } else {
                        break
                    }
                }
            }
        }
        
        steps.add(AlgorithmStep(
            description = "Starting pre-order traversal (Root → Left → Right)",
            treeData = buildTreeData(root)
        ))
        
        val visitedNodes = mutableSetOf<Int>()
        val result = mutableListOf<Int>()
        
        fun preorder(node: BSTNode?) {
            if (node == null) return
            
            visitedNodes.add(node.value)
            result.add(node.value)
            
            steps.add(AlgorithmStep(
                description = "Visiting node ${node.value}. Traversal so far: ${result.joinToString(", ")}",
                treeData = buildTreeData(root, visitedNodes = visitedNodes, highlightedNodes = setOf(node.value))
            ))
            
            preorder(node.left)
            preorder(node.right)
        }
        
        preorder(root)
        
        steps.add(AlgorithmStep(
            description = "Pre-order traversal complete: ${result.joinToString(", ")}",
            treeData = buildTreeData(root, visitedNodes = visitedNodes)
        ))
        
        return steps
    }
}
