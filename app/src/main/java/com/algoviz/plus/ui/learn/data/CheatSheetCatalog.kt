package com.algoviz.plus.ui.learn.data

import com.algoviz.plus.ui.learn.model.LearnItem
import com.algoviz.plus.ui.learn.model.LearnSection
import com.algoviz.plus.ui.learn.model.LearnSheet
import com.algoviz.plus.ui.learn.model.LearnTopicTag

object CheatSheetCatalog {

    val sheets: List<LearnSheet> = listOf(
        LearnSheet(
            id = "love_babbar",
            influencer = "Love Babbar",
            title = "DSA 450 Sheet",
            structureNote = "Topic order follows the original 450 sheet buckets (arrays to trie). Complete section by section.",
            sections = listOf(
                LearnSection(
                    id = "lb_arrays",
                    title = "Arrays",
                    summary = "Core array manipulation, scanning, prefix ideas, and interval style problems.",
                    items = listOf(
                        LearnItem(
                            id = "lb_bubble_sort",
                            title = "Sorting intuition with adjacent swaps",
                            explanation = "Build understanding of iterative passes and in-place swap behavior before advanced sorting patterns.",
                            keyPoints = listOf(
                                "Pass-by-pass progress",
                                "Track invariants while iterating",
                                "Transition to faster sorts later"
                            ),
                            algorithmId = "bubble_sort",
                            tags = setOf(LearnTopicTag.SORTING)
                        ),
                        LearnItem(
                            id = "lb_kadane_pattern",
                            title = "Maximum subarray pattern",
                            explanation = "Learn rolling best-prefix logic used in contiguous sum optimization problems.",
                            keyPoints = listOf(
                                "Running sum reset logic",
                                "Prefix contribution intuition",
                                "Linear scan optimization"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        ),
                        LearnItem(
                            id = "lb_binary_search_pattern",
                            title = "Binary search mindset",
                            explanation = "Use low-high boundary reasoning for sorted lookups and answer-space style questions.",
                            keyPoints = listOf(
                                "Boundary invariants",
                                "Midpoint safety",
                                "Monotonic condition"
                            ),
                            algorithmId = "binary_search",
                            tags = setOf(LearnTopicTag.SEARCHING)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_matrix",
                    title = "Matrix",
                    summary = "Traversal, search, rotation, and rectangle-based matrix reasoning.",
                    items = listOf(
                        LearnItem(
                            id = "lb_matrix_traversal",
                            title = "2D traversal templates",
                            explanation = "Practice row-column boundaries, directional movement, and index guard checks.",
                            keyPoints = listOf(
                                "Boundary checks",
                                "Direction vectors",
                                "Visited handling"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        ),
                        LearnItem(
                            id = "lb_matrix_rotate",
                            title = "Matrix transform patterns",
                            explanation = "Understand transpose + reverse and related transformation moves for interview matrix problems.",
                            keyPoints = listOf(
                                "In-place transformation",
                                "Index mapping",
                                "Symmetry handling"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_strings",
                    title = "Strings",
                    summary = "Pattern matching, palindrome methods, hashing intuition, and parsing problems.",
                    items = listOf(
                        LearnItem(
                            id = "lb_kmp_intro",
                            title = "String matching fundamentals",
                            explanation = "Move from brute-force matching to prefix-function-based optimization.",
                            keyPoints = listOf(
                                "Prefix-suffix reuse",
                                "Mismatch fallback",
                                "Linear pattern search"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        ),
                        LearnItem(
                            id = "lb_lcs_string_bridge",
                            title = "LCS as string DP bridge",
                            explanation = "Use subsequence DP to solve classic string comparison and transformation tasks.",
                            keyPoints = listOf(
                                "State table design",
                                "Choice transitions",
                                "Reconstruction idea"
                            ),
                            algorithmId = "lcs",
                            tags = setOf(LearnTopicTag.DYNAMIC_PROGRAMMING)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_search_sort",
                    title = "Searching and Sorting",
                    summary = "Classic binary-search-on-answer and efficient sorting/selection patterns.",
                    items = listOf(
                        LearnItem(
                            id = "lb_merge_sort",
                            title = "Merge sort and inversion logic",
                            explanation = "Use divide-and-conquer merge flow for stable sorting and count-style variants.",
                            keyPoints = listOf(
                                "Split and merge",
                                "Stable ordering",
                                "Count while merging"
                            ),
                            algorithmId = "merge_sort",
                            tags = setOf(LearnTopicTag.SORTING)
                        ),
                        LearnItem(
                            id = "lb_binary_answer_space",
                            title = "Binary search on answer space",
                            explanation = "Apply feasibility checks with monotonic decisions to optimize range-based problems.",
                            keyPoints = listOf(
                                "Low/high answer bounds",
                                "Feasibility predicate",
                                "Minimize/maximize objective"
                            ),
                            algorithmId = "binary_search",
                            tags = setOf(LearnTopicTag.SEARCHING)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_linked_list",
                    title = "Linked List",
                    summary = "Pointers, reversal, cycle handling, partitioning, and list arithmetic.",
                    items = listOf(
                        LearnItem(
                            id = "lb_ll_pointer_patterns",
                            title = "Pointer movement patterns",
                            explanation = "Master slow-fast, dummy-head, and reversal windows for robust linked list operations.",
                            keyPoints = listOf(
                                "Two pointers",
                                "In-place reverse",
                                "Cycle detection"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_bit_manipulation",
                    title = "Bit Manipulation",
                    summary = "Set-bit tricks, XOR properties, and bitmask-driven optimizations.",
                    items = listOf(
                        LearnItem(
                            id = "lb_bit_basics",
                            title = "Bitmask fundamentals",
                            explanation = "Use masking, toggling, and XOR partition ideas for compact and fast solutions.",
                            keyPoints = listOf(
                                "Mask create and check",
                                "XOR cancellation",
                                "Power-of-two tests"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_greedy",
                    title = "Greedy",
                    summary = "Local-choice optimization where proof of correctness is essential.",
                    items = listOf(
                        LearnItem(
                            id = "lb_activity_selection",
                            title = "Selection by best local choice",
                            explanation = "Sort by a key and repeatedly pick the best valid next move.",
                            keyPoints = listOf(
                                "Sort by finish/weight key",
                                "Greedy proof idea",
                                "Counterexamples awareness"
                            ),
                            algorithmId = "activity_selection",
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_backtracking",
                    title = "Backtracking",
                    summary = "State-space search with pruning and incremental construction.",
                    items = listOf(
                        LearnItem(
                            id = "lb_n_queens",
                            title = "N-Queens pruning",
                            explanation = "Place row by row, validate constraints, and backtrack immediately on invalid branches.",
                            keyPoints = listOf(
                                "Constraint tracking",
                                "Branch pruning",
                                "Undo state"
                            ),
                            algorithmId = "n_queens",
                            tags = setOf(LearnTopicTag.RECURSION)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_dynamic_programming",
                    title = "Dynamic Programming",
                    summary = "Recurrence design, memoization, tabulation, and state compression.",
                    items = listOf(
                        LearnItem(
                            id = "lb_knapsack",
                            title = "Knapsack state transitions",
                            explanation = "Define item-capacity state and evaluate include/exclude decisions.",
                            keyPoints = listOf(
                                "State dimensions",
                                "Transition relation",
                                "Space optimization"
                            ),
                            algorithmId = "knapsack",
                            tags = setOf(LearnTopicTag.DYNAMIC_PROGRAMMING)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_stack_queue",
                    title = "Stacks and Queues",
                    summary = "Monotonic structures, expression handling, and window-driven queue tasks.",
                    items = listOf(
                        LearnItem(
                            id = "lb_monotonic_stack",
                            title = "Monotonic stack templates",
                            explanation = "Use increasing/decreasing stacks for next-greater and span-style problems.",
                            keyPoints = listOf(
                                "Push-pop invariant",
                                "Nearest greater/smaller",
                                "Linear-time amortization"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_binary_trees",
                    title = "Binary Trees",
                    summary = "Traversal patterns, view problems, diameter, and path-based logic.",
                    items = listOf(
                        LearnItem(
                            id = "lb_tree_traversals",
                            title = "Traversal strategy mapping",
                            explanation = "Choose preorder/inorder/postorder/level-order based on the question objective.",
                            keyPoints = listOf(
                                "Traversal intent",
                                "Queue vs recursion",
                                "View/path combinations"
                            ),
                            algorithmId = "inorder_traversal",
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_binary_search_tree",
                    title = "Binary Search Tree",
                    summary = "Ordered tree operations, predecessor-successor, and validation tasks.",
                    items = listOf(
                        LearnItem(
                            id = "lb_bst_ops",
                            title = "BST search and update operations",
                            explanation = "Use key ordering to reduce search space and keep structural properties intact.",
                            keyPoints = listOf(
                                "Order invariant",
                                "Successor/predecessor",
                                "Validation checks"
                            ),
                            algorithmId = "bst_search",
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_graphs",
                    title = "Graphs",
                    summary = "Traversal, shortest path, MST, topological order, and connectivity.",
                    items = listOf(
                        LearnItem(
                            id = "lb_bfs",
                            title = "BFS traversal and level reasoning",
                            explanation = "Build shortest path intuition for unweighted graphs and grid-style problems.",
                            keyPoints = listOf(
                                "Queue frontier",
                                "Visited control",
                                "Level-distance mapping"
                            ),
                            algorithmId = "bfs",
                            tags = setOf(LearnTopicTag.GRAPH)
                        ),
                        LearnItem(
                            id = "lb_dijkstra",
                            title = "Dijkstra shortest path",
                            explanation = "Use min-priority queue relaxation when all edges are non-negative.",
                            keyPoints = listOf(
                                "Distance relaxations",
                                "Priority queue usage",
                                "Non-negative edge rule"
                            ),
                            algorithmId = "dijkstra",
                            tags = setOf(LearnTopicTag.GRAPH)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_heap",
                    title = "Heap",
                    summary = "Priority queue patterns for top-k, median stream, and merge tasks.",
                    items = listOf(
                        LearnItem(
                            id = "lb_heap_basics",
                            title = "Heap ordering patterns",
                            explanation = "Choose min-heap or max-heap according to extraction goal and memory constraints.",
                            keyPoints = listOf(
                                "Top-k design",
                                "Merge streams",
                                "Running median idea"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "lb_trie",
                    title = "Trie",
                    summary = "Prefix queries, dictionary operations, and word-search style optimization.",
                    items = listOf(
                        LearnItem(
                            id = "lb_trie_prefix",
                            title = "Prefix tree operations",
                            explanation = "Implement insert/search/prefix checks and use trie nodes for fast string grouping.",
                            keyPoints = listOf(
                                "Character transitions",
                                "End markers",
                                "Prefix-based filtering"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                )
            )
        ),
        LearnSheet(
            id = "striver_a2z",
            influencer = "Striver (Take U Forward)",
            title = "A2Z DSA Sheet",
            structureNote = "Section flow follows the public A2Z progression buckets used in Striver's track.",
            sections = listOf(
                LearnSection(
                    id = "st_arrays",
                    title = "Arrays",
                    summary = "Easy to hard array problems with strong attention to two-pointer and prefix ideas.",
                    items = listOf(
                        LearnItem(
                            id = "st_insertion_sort",
                            title = "Insertion Sort invariants",
                            explanation = "Track sorted-prefix growth and stable insertion behavior.",
                            keyPoints = listOf(
                                "Sorted prefix",
                                "Shift and place",
                                "Best-case near sorted"
                            ),
                            algorithmId = "insertion_sort",
                            tags = setOf(LearnTopicTag.SORTING)
                        ),
                        LearnItem(
                            id = "st_array_patterns",
                            title = "Array pattern ladder",
                            explanation = "Move from brute force to optimized map/prefix/two-pointer approaches.",
                            keyPoints = listOf(
                                "Pattern recognition",
                                "Constraint-driven optimization",
                                "In-place updates"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_binary_search",
                    title = "Binary Search",
                    summary = "Classical search plus binary-search-on-answer-space problems.",
                    items = listOf(
                        LearnItem(
                            id = "st_bs_boundaries",
                            title = "Boundary and answer-space binary search",
                            explanation = "Design feasibility predicate and maintain tight search boundaries.",
                            keyPoints = listOf(
                                "Monotonic condition",
                                "First/last occurrence",
                                "Range minimization"
                            ),
                            algorithmId = "binary_search",
                            tags = setOf(LearnTopicTag.SEARCHING)
                        )
                    )
                ),
                LearnSection(
                    id = "st_linked_list",
                    title = "Linked List",
                    summary = "Reversal blocks, cycle handling, and pointer choreography under constraints.",
                    items = listOf(
                        LearnItem(
                            id = "st_ll_toolkit",
                            title = "Core pointer toolkit",
                            explanation = "Combine dummy nodes, two pointers, and in-place reversal for robust list solutions.",
                            keyPoints = listOf(
                                "Dummy head usage",
                                "Fast-slow pointer",
                                "Segment reverse"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_recursion",
                    title = "Recursion and Backtracking",
                    summary = "Subsequence patterns, combination generation, and constrained search.",
                    items = listOf(
                        LearnItem(
                            id = "st_n_queens",
                            title = "Backtracking decision tree",
                            explanation = "Try, validate, and undo choices while pruning early.",
                            keyPoints = listOf(
                                "Decision branching",
                                "Validity checks",
                                "Backtrack discipline"
                            ),
                            algorithmId = "n_queens",
                            tags = setOf(LearnTopicTag.RECURSION)
                        )
                    )
                ),
                LearnSection(
                    id = "st_bit_manipulation",
                    title = "Bit Manipulation",
                    summary = "Bitmask and XOR-based transformations for compact solution design.",
                    items = listOf(
                        LearnItem(
                            id = "st_bitmask_patterns",
                            title = "Bit operations pattern set",
                            explanation = "Use set/unset/toggle checks with XOR properties for low-overhead logic.",
                            keyPoints = listOf(
                                "Bitmask checks",
                                "XOR partition",
                                "Shift operations"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_stack_queue",
                    title = "Stack and Queues",
                    summary = "Expression conversion, monotonic stack, and deque/window design.",
                    items = listOf(
                        LearnItem(
                            id = "st_monotonic",
                            title = "Monotonic stack practice",
                            explanation = "Solve nearest-greater/smaller and collision style tasks with stack invariants.",
                            keyPoints = listOf(
                                "Push-pop rules",
                                "Candidate maintenance",
                                "Linear amortized complexity"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_sliding_window",
                    title = "Sliding Window and Two Pointers",
                    summary = "Window invariants for substring and subarray optimization.",
                    items = listOf(
                        LearnItem(
                            id = "st_window_invariant",
                            title = "Window invariant strategy",
                            explanation = "Expand and contract windows while maintaining exact validity conditions.",
                            keyPoints = listOf(
                                "Valid window condition",
                                "Frequency tracking",
                                "Two-pointer movement"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_heaps",
                    title = "Heaps",
                    summary = "Priority-based extraction for top-k, merging, and stream processing.",
                    items = listOf(
                        LearnItem(
                            id = "st_heap_priority",
                            title = "Priority queue workflows",
                            explanation = "Use heap ordering to process best-next element efficiently.",
                            keyPoints = listOf(
                                "Top-k selection",
                                "Stream updates",
                                "Custom comparator"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_greedy",
                    title = "Greedy",
                    summary = "Local optimal choices with proof-backed correctness.",
                    items = listOf(
                        LearnItem(
                            id = "st_activity_selection",
                            title = "Greedy scheduling logic",
                            explanation = "Choose the next best feasible option after sorting by the right key.",
                            keyPoints = listOf(
                                "Sort by decision key",
                                "Feasible incremental choice",
                                "Proof by exchange"
                            ),
                            algorithmId = "activity_selection",
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_binary_trees",
                    title = "Binary Trees",
                    summary = "Traversal combinations, depth metrics, and tree-path transformations.",
                    items = listOf(
                        LearnItem(
                            id = "st_tree_traversals",
                            title = "Traversal-first tree reasoning",
                            explanation = "Start from traversal goals and map to recursion or queue-based techniques.",
                            keyPoints = listOf(
                                "Traversal objective",
                                "Recursive decomposition",
                                "Level-based processing"
                            ),
                            algorithmId = "preorder_traversal",
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_bst",
                    title = "Binary Search Trees",
                    summary = "Ordered tree constraints for efficient search and updates.",
                    items = listOf(
                        LearnItem(
                            id = "st_bst_search",
                            title = "BST search strategy",
                            explanation = "Use node ordering to route left/right and solve predecessor-successor tasks.",
                            keyPoints = listOf(
                                "Order property",
                                "Log-time average search",
                                "Validation of BST"
                            ),
                            algorithmId = "bst_search",
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_graph",
                    title = "Graphs",
                    summary = "Traversal, topo-sort, shortest path, MST, and component-based tasks.",
                    items = listOf(
                        LearnItem(
                            id = "st_dfs",
                            title = "DFS traversal template",
                            explanation = "Explore deeply with visited tracking and recursion/stack alternatives.",
                            keyPoints = listOf(
                                "Adjacency representation",
                                "Visited control",
                                "Component discovery"
                            ),
                            algorithmId = "dfs",
                            tags = setOf(LearnTopicTag.GRAPH)
                        ),
                        LearnItem(
                            id = "st_bfs",
                            title = "BFS traversal template",
                            explanation = "Process graph layer by layer for distance and shortest path in unweighted settings.",
                            keyPoints = listOf(
                                "Queue frontier",
                                "Distance layering",
                                "Level transitions"
                            ),
                            algorithmId = "bfs",
                            tags = setOf(LearnTopicTag.GRAPH)
                        )
                    )
                ),
                LearnSection(
                    id = "st_dp",
                    title = "Dynamic Programming",
                    summary = "1D/2D DP, subsequences, partition DP, and optimization transitions.",
                    items = listOf(
                        LearnItem(
                            id = "st_lcs",
                            title = "LCS state design",
                            explanation = "Define states on prefixes and transition on match/mismatch.",
                            keyPoints = listOf(
                                "State matrix",
                                "Transition relation",
                                "Tabulation and optimization"
                            ),
                            algorithmId = "lcs",
                            tags = setOf(LearnTopicTag.DYNAMIC_PROGRAMMING)
                        ),
                        LearnItem(
                            id = "st_knapsack",
                            title = "0/1 Knapsack recurrence",
                            explanation = "Learn include/exclude decision transitions with capacity constraints.",
                            keyPoints = listOf(
                                "Decision branching",
                                "Memoization to tabulation",
                                "Space compression"
                            ),
                            algorithmId = "knapsack",
                            tags = setOf(LearnTopicTag.DYNAMIC_PROGRAMMING)
                        ),
                        LearnItem(
                            id = "st_dijkstra",
                            title = "Weighted shortest path with PQ",
                            explanation = "Bridge graph and DP-like relaxation thinking with priority queue extraction.",
                            keyPoints = listOf(
                                "Relax edges",
                                "Distance array",
                                "Stale entry skip"
                            ),
                            algorithmId = "dijkstra",
                            tags = setOf(LearnTopicTag.GRAPH)
                        )
                    )
                ),
                LearnSection(
                    id = "st_tries",
                    title = "Tries",
                    summary = "Prefix-optimized string dictionaries and word query structures.",
                    items = listOf(
                        LearnItem(
                            id = "st_trie_core",
                            title = "Trie build and query",
                            explanation = "Insert words by character path and support prefix/complete word queries.",
                            keyPoints = listOf(
                                "Node children map",
                                "Word end marker",
                                "Prefix traversal"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                ),
                LearnSection(
                    id = "st_strings_hard",
                    title = "Strings (Hard)",
                    summary = "Advanced string matching and transformation patterns.",
                    items = listOf(
                        LearnItem(
                            id = "st_kmp",
                            title = "KMP prefix-function workflow",
                            explanation = "Avoid full restart on mismatch by reusing prefix information.",
                            keyPoints = listOf(
                                "LPS array",
                                "Mismatch fallback",
                                "Linear scan"
                            ),
                            tags = setOf(LearnTopicTag.DATA_STRUCTURES)
                        )
                    )
                )
            )
        )
    )

    fun findSheet(sheetId: String): LearnSheet? = sheets.firstOrNull { it.id == sheetId }
}
