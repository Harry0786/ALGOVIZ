package com.algoviz.plus.ui.algorithms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.R
import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.model.AlgorithmCategory
import com.algoviz.plus.domain.model.PlaybackSpeed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgorithmVisualizationScreen(
    onBackClick: () -> Unit,
    viewModel: VisualizationViewModel = hiltViewModel()
) {
    val algorithm by viewModel.algorithm.collectAsStateWithLifecycle()
    val state by viewModel.visualizationState.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val customInputError by viewModel.customInputError.collectAsStateWithLifecycle()
    val algorithmParameterInput by viewModel.algorithmParameterInput.collectAsStateWithLifecycle()
    val algorithmParameterError by viewModel.algorithmParameterError.collectAsStateWithLifecycle()
    val algorithmInputSpec = remember(algorithm?.id) { viewModel.getAlgorithmInputSpec() }
    var customInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.generateSteps()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.30f,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.20f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = algorithm?.name ?: "Loading...",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VisualizationTab(
                state = state,
                algorithm = algorithm,
                isGenerating = isGenerating,
                customInput = customInput,
                customInputError = customInputError,
                algorithmInputSpec = algorithmInputSpec,
                algorithmParameterInput = algorithmParameterInput,
                algorithmParameterError = algorithmParameterError,
                getCurrentStepData = { viewModel.getCurrentStepData() },
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                onStepForward = { viewModel.stepForward() },
                onStepBackward = { viewModel.stepBackward() },
                onReset = { viewModel.reset() },
                onSpeedChange = { viewModel.setSpeed(it) },
                onGenerateNew = { viewModel.generateInitialArray() },
                onAlgorithmParameterChange = { viewModel.setAlgorithmParameterInput(it) },
                onApplyAlgorithmInput = { viewModel.generateSteps() },
                onCustomInputChange = {
                    customInput = it
                    if (customInputError != null) {
                        viewModel.clearCustomInputError()
                    }
                },
                onApplyCustomInput = { viewModel.applyCustomInput(customInput) }
            )
        }
    }
}

@Composable
private fun ComplexityBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = Color(0xFF2E3137)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = Color(0xFFD8D8D8)
        )
    }
}

private data class AlgorithmKnowledge(
    val keyConcept: String,
    val pseudocode: List<String>,
    val applications: List<String>
)

@Composable
private fun KnowledgeSection(algorithm: Algorithm) {
    val knowledge = remember(algorithm.id) { knowledgeForAlgorithm(algorithm) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        InfoAccordionCard(
            title = "KEY CONCEPT",
            expanded = true,
            onToggle = {},
            content = {
                Text(
                    text = knowledge.keyConcept,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    lineHeight = 29.sp
                )
            }
        )

        InfoAccordionCard(
            title = "PSEUDOCODE",
            expanded = true,
            onToggle = {},
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    knowledge.pseudocode.forEachIndexed { index, line ->
                        Text(
                            text = "${index + 1}.  ${line.uppercase()}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.80f),
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        )

        InfoAccordionCard(
            title = "APPLICATIONS",
            expanded = true,
            onToggle = {},
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    knowledge.applications.forEach { line ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "•", color = Color.White, fontSize = 16.sp)
                            Text(
                                text = line,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun InfoAccordionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1A1C20).copy(alpha = 0.88f),
        border = BorderStroke(1.dp, Color(0xFFDADADA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }

            if (expanded) content()
        }
    }
}

private fun knowledgeForAlgorithm(algorithm: Algorithm): AlgorithmKnowledge {
    return when (algorithm.id) {
        "trie_operations" -> AlgorithmKnowledge(
            keyConcept = "Trie stores words letter-by-letter, sharing common prefixes. Think of it like a city map where roads with same starting path are shared.",
            pseudocode = listOf(
                "Start at root node",
                "For each character in word, move/create child node",
                "After last character, mark end-of-word",
                "For search, follow characters one by one",
                "Word exists only if end-of-word mark is true"
            ),
            applications = listOf(
                "Auto-complete suggestions in keyboards and search bars",
                "Spell checking and dictionary matching",
                "Prefix search in contacts and product catalogs"
            )
        )

        "quick_select" -> AlgorithmKnowledge(
            keyConcept = "Quick Select finds one rank (k-th smallest) without fully sorting. It keeps only the side where the answer can exist.",
            pseudocode = listOf(
                "Pick a pivot",
                "Partition values smaller and bigger around pivot",
                "If pivot index is k, answer found",
                "If k is left of pivot, repeat on left side",
                "Else repeat on right side"
            ),
            applications = listOf(
                "Median and percentile analytics",
                "Top-k and ranking systems",
                "Fast threshold selection in dashboards"
            )
        )

        else -> when (algorithm.category) {
            AlgorithmCategory.SORTING -> AlgorithmKnowledge(
                keyConcept = "Sorting arranges data in order so decisions become easier and faster, like arranging books by number before searching.",
                pseudocode = listOf(
                    "Read input list",
                    "Compare elements based on algorithm rule",
                    "Reorder elements when needed",
                    "Repeat until list is ordered",
                    "Return ordered list"
                ),
                applications = listOf(
                    "Leaderboards and score ranking",
                    "Report generation and data presentation",
                    "Preparing data for faster searching"
                )
            )

            AlgorithmCategory.SEARCHING -> AlgorithmKnowledge(
                keyConcept = "Searching means finding one required item in a collection, like finding one name in a phone list.",
                pseudocode = listOf(
                    "Set target value",
                    "Inspect one or more candidate positions",
                    "If target found, return position",
                    "Else narrow or continue the search",
                    "If no candidates left, report not found"
                ),
                applications = listOf(
                    "Contact lookup and search features",
                    "Finding records in business data",
                    "Search bars in apps and websites"
                )
            )

            AlgorithmCategory.GRAPH -> AlgorithmKnowledge(
                keyConcept = "Graph algorithms solve route and connection problems, like planning travel between cities or data links in a network.",
                pseudocode = listOf(
                    "Choose start node",
                    "Visit nodes using algorithm strategy",
                    "Track visited/processed nodes",
                    "Update path or cost information",
                    "Stop when objective is reached"
                ),
                applications = listOf(
                    "Navigation and shortest routes",
                    "Social network and recommendation systems",
                    "Network design and optimization"
                )
            )

            AlgorithmCategory.TREE -> AlgorithmKnowledge(
                keyConcept = "Tree algorithms handle parent-child structures, like folders in a computer or categories in a shopping app.",
                pseudocode = listOf(
                    "Start from root node",
                    "Move left/right or child nodes by rule",
                    "Process node when visited",
                    "Continue recursively/iteratively",
                    "Return required result"
                ),
                applications = listOf(
                    "Folder structures and menu systems",
                    "Fast lookup in ordered datasets",
                    "Expression parsing and decision flows"
                )
            )

            AlgorithmCategory.DYNAMIC_PROGRAMMING -> AlgorithmKnowledge(
                keyConcept = "Dynamic Programming breaks a big problem into smaller repeated parts and stores answers to avoid recomputing.",
                pseudocode = listOf(
                    "Define smaller subproblems",
                    "Create table/cache for subproblem answers",
                    "Fill answers in safe order",
                    "Reuse cached values when needed",
                    "Return final answer from table"
                ),
                applications = listOf(
                    "Resource planning and optimization",
                    "Sequence comparison in bio/text data",
                    "Cost minimization and budgeting models"
                )
            )

            AlgorithmCategory.GREEDY -> AlgorithmKnowledge(
                keyConcept = "Greedy algorithms choose the best immediate step each time, aiming for a good final outcome quickly.",
                pseudocode = listOf(
                    "Sort or prioritize candidate choices",
                    "Pick best local option",
                    "Lock chosen option",
                    "Repeat with remaining options",
                    "Return built solution"
                ),
                applications = listOf(
                    "Scheduling meetings or tasks",
                    "Compression and coding",
                    "Network and cost-saving planning"
                )
            )

            AlgorithmCategory.BACKTRACKING -> AlgorithmKnowledge(
                keyConcept = "Backtracking tries a path, and if it fails, it rolls back and tries another path, like solving a maze.",
                pseudocode = listOf(
                    "Try one valid option",
                    "Move to next decision",
                    "If dead-end, undo previous choice",
                    "Try alternate option",
                    "Stop when complete valid solution is found"
                ),
                applications = listOf(
                    "Puzzle solvers like Sudoku",
                    "Constraint-based planning",
                    "Combinational search problems"
                )
            )

            AlgorithmCategory.DIVIDE_AND_CONQUER -> AlgorithmKnowledge(
                keyConcept = "Divide and Conquer splits a problem into smaller pieces, solves them, then combines results.",
                pseudocode = listOf(
                    "If problem is small, solve directly",
                    "Divide into smaller subproblems",
                    "Solve each subproblem",
                    "Combine sub-results",
                    "Return merged result"
                ),
                applications = listOf(
                    "Fast searching and selection",
                    "Parallel processing workflows",
                    "Large-scale data computation"
                )
            )
        }
    }
}

@Composable
private fun VisualizationTab(
    state: com.algoviz.plus.domain.model.VisualizationState,
    algorithm: com.algoviz.plus.domain.model.Algorithm?,
    isGenerating: Boolean,
    customInput: String,
    customInputError: String?,
    algorithmInputSpec: VisualizationViewModel.AlgorithmInputSpec?,
    algorithmParameterInput: String,
    algorithmParameterError: String?,
    getCurrentStepData: () -> com.algoviz.plus.domain.model.AlgorithmStep?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onReset: () -> Unit,
    onSpeedChange: (PlaybackSpeed) -> Unit,
    onGenerateNew: () -> Unit,
    onAlgorithmParameterChange: (String) -> Unit,
    onApplyAlgorithmInput: () -> Unit,
    onCustomInputChange: (String) -> Unit,
    onApplyCustomInput: () -> Unit
) {
    val maxStep = if (state.totalSteps > 0) state.totalSteps - 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        algorithm?.let {
            OverviewCard(algorithm = it)
            KnowledgeSection(algorithm = it)
        }

        PlaybackControlsCard(
            state = state,
            isGenerating = isGenerating,
            onPlay = onPlay,
            onPause = onPause,
            onStepForward = onStepForward,
            onStepBackward = onStepBackward,
            onReset = onReset,
            onSpeedChange = onSpeedChange,
            onGenerateNew = onGenerateNew
        )

        CustomHistogramInputCard(
            customInput = customInput,
            customInputError = customInputError,
            onCustomInputChange = onCustomInputChange,
            onApplyCustomInput = onApplyCustomInput,
            onPause = onPause
        )

        StatsStrip(state = state, maxStep = maxStep)

        VisualizationPanel(
            state = state,
            algorithm = algorithm,
            isGenerating = isGenerating,
            getCurrentStepData = getCurrentStepData
        )

        ComplexityAndFlowSection(algorithm = algorithm)

        AlgorithmLifecycleSection()

        BenchmarkComparisonSection(currentAlgorithm = algorithm)
    }
}

@Composable
private fun OverviewCard(algorithm: Algorithm?) {
    algorithm ?: return

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1A1C20).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFFDADADA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ComplexityBadge(algorithm.timeComplexity.average)
                    ComplexityBadge(algorithm.spaceComplexity.average)
                }
            }

            Text(
                text = algorithm.description,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.82f),
                lineHeight = 30.sp
            )
        }
    }
}

@Composable
private fun PlaybackControlsCard(
    state: com.algoviz.plus.domain.model.VisualizationState,
    isGenerating: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onReset: () -> Unit,
    onSpeedChange: (PlaybackSpeed) -> Unit,
    onGenerateNew: () -> Unit
) {
    val maxStep = if (state.totalSteps > 0) state.totalSteps - 1 else 0
    var speedMenuExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GhostIconButton(
                imageVector = Icons.Default.Refresh,
                enabled = !isGenerating && state.totalSteps > 0,
                onClick = {
                    speedMenuExpanded = false
                    onReset()
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            GhostIconButton(
                imageVector = Icons.Default.SkipPrevious,
                enabled = !isGenerating && state.currentStep > 0 && !state.isPlaying,
                onClick = {
                    speedMenuExpanded = false
                    onStepBackward()
                }
            )
            Spacer(modifier = Modifier.width(14.dp))

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.size(62.dp)
            ) {
                Box(
                    modifier = Modifier.clickable {
                        speedMenuExpanded = false
                        if (state.isPlaying) onPause() else onPlay()
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))
            GhostIconButton(
                imageVector = Icons.Default.SkipNext,
                enabled = !isGenerating && state.currentStep < state.totalSteps - 1 && !state.isPlaying,
                onClick = {
                    speedMenuExpanded = false
                    onStepForward()
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            GhostIconButton(
                imageVector = Icons.Default.Shuffle,
                enabled = !isGenerating && !state.isPlaying,
                onClick = {
                    speedMenuExpanded = false
                    onPause()
                    onGenerateNew()
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROGRESS ${state.currentStep}/$maxStep",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            LinearProgressIndicator(
                progress = if (maxStep > 0) state.currentStep.toFloat() / maxStep else 0f,
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp),
                color = Color(0xFFE7E7E7),
                trackColor = Color.White.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFF111111),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
            ) {
                Text(
                    text = state.speed.displayName.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        DropdownMenu(
            expanded = speedMenuExpanded,
            onDismissRequest = { speedMenuExpanded = false }
        ) {
            PlaybackSpeed.entries.forEach { speed ->
                DropdownMenuItem(
                    text = { Text(speed.displayName) },
                    onClick = {
                        onSpeedChange(speed)
                        speedMenuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GhostIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.Transparent,
        modifier = Modifier.size(34.dp)
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                tint = if (enabled) Color.White else Color.White.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun CustomHistogramInputCard(
    customInput: String,
    customInputError: String?,
    onCustomInputChange: (String) -> Unit,
    onApplyCustomInput: () -> Unit,
    onPause: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1A1C20).copy(alpha = 0.90f),
        border = BorderStroke(1.dp, Color(0xFFDADADA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "CUSTOM HISTOGRAM INPUT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customInput,
                    onValueChange = onCustomInputChange,
                    singleLine = true,
                    placeholder = {
                        Text("e.g. 10, 50, 30, 90", color = Color.White.copy(alpha = 0.35f))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier.clickable {
                        onPause()
                        onApplyCustomInput()
                    }
                ) {
                    Text(
                        text = "APPLY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }

            if (customInputError != null) {
                Text(
                    text = customInputError,
                    color = Color(0xFFFDA4AF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun StatsStrip(
    state: com.algoviz.plus.domain.model.VisualizationState,
    maxStep: Int
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1C20).copy(alpha = 0.92f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SmallStat(label = "PROGRESS", value = "${(if (maxStep > 0) (state.currentStep.toFloat() / maxStep * 100).toInt() else 0)}%")
            SmallStat(label = "COMPARES", value = "${state.comparisons}")
            SmallStat(label = "SWAPS", value = "${state.swaps}")
            SmallStat(label = "SORTED", value = "${state.sortedIndices.size}/${state.array.size}")
        }
    }
}

@Composable
private fun SmallStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun VisualizationPanel(
    state: com.algoviz.plus.domain.model.VisualizationState,
    algorithm: com.algoviz.plus.domain.model.Algorithm?,
    isGenerating: Boolean,
    getCurrentStepData: () -> com.algoviz.plus.domain.model.AlgorithmStep?
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF17191D).copy(alpha = 0.96f),
        border = BorderStroke(1.dp, Color(0xFF2A2D31)),
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "",
                    fontSize = 1.sp,
                    color = Color.Transparent
                )
                Text(
                    text = "VISUALIZATION",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.35f),
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isGenerating) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                when (algorithm?.category) {
                    com.algoviz.plus.domain.model.AlgorithmCategory.GRAPH -> {
                        val graphData = getCurrentStepData()?.graphData
                        if (graphData != null) GraphVisualization(graphData)
                    }
                    com.algoviz.plus.domain.model.AlgorithmCategory.TREE -> {
                        val treeData = getCurrentStepData()?.treeData
                        if (treeData != null) TreeVisualization(treeData)
                    }
                    com.algoviz.plus.domain.model.AlgorithmCategory.DYNAMIC_PROGRAMMING -> {
                        val matrix = getCurrentStepData()?.matrix
                        if (matrix != null) MatrixVisualization(matrix, state.comparingIndices)
                    }
                    else -> {
                        BarChartVisualization(
                            data = state.array,
                            comparingIndices = state.comparingIndices,
                            swappingIndices = state.swappingIndices,
                            sortedIndices = state.sortedIndices,
                            currentIndex = state.currentIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplexityAndFlowSection(algorithm: Algorithm?) {
    algorithm ?: return

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "COMPLEXITY & FLOW",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.35f),
            letterSpacing = 1.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1C20).copy(alpha = 0.92f),
                modifier = Modifier.weight(1.3f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "TIME COMPLEXITY", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp)
                    ComplexityRow("Best Case", algorithm.timeComplexity.best)
                    ComplexityRow("Average", algorithm.timeComplexity.average)
                    ComplexityRow("Worst Case", algorithm.timeComplexity.worst)
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1A1C20).copy(alpha = 0.92f),
                modifier = Modifier.weight(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "SPACE COMPLEXITY", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp)
                    Text(text = algorithm.spaceComplexity.average, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(text = "AUXILIARY", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1C20).copy(alpha = 0.92f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "GROWTH RATE VISUALIZATION", color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp)
                Row(modifier = Modifier.height(84.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                    GrowthBar(label = "O(n²)", heightFactor = 1.0f, color = Color.White.copy(alpha = 0.26f))
                    GrowthBar(label = "O(n log n)", heightFactor = 0.58f, color = Color.White.copy(alpha = 0.26f))
                    GrowthBar(label = "O(n*1)", heightFactor = 0.42f, color = Color.White.copy(alpha = 0.26f))
                }
            }
        }
    }
}

@Composable
private fun ComplexityRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GrowthBar(label: String, heightFactor: Float, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Box(
            modifier = Modifier
                .width(42.dp)
                .height((72 * heightFactor).dp)
                .background(color, RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
    }
}

@Composable
private fun AlgorithmLifecycleSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = "ALGORITHM LIFECYCLE", fontSize = 12.sp, color = Color.White.copy(alpha = 0.35f), letterSpacing = 1.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            LifecycleStepCard("INPUT", true, Modifier.weight(1f))
            LifecycleStepCard("COMPARE", false, Modifier.weight(1f))
            LifecycleStepCard("SWAP", false, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LifecycleStepCard(label: String, active: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (active) Color.White else Color(0xFF1A1C20).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = modifier.height(70.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (active) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun BenchmarkComparisonSection(currentAlgorithm: Algorithm?) {
    if (currentAlgorithm?.category != AlgorithmCategory.SORTING) return

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1A1C20).copy(alpha = 0.92f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "BENCHMARK COMPARISON", fontSize = 10.sp, color = Color.White.copy(alpha = 0.35f), letterSpacing = 0.8.sp)
            BenchmarkRow(currentAlgorithm.name, 1.0f, currentAlgorithm.timeComplexity.average)
            BenchmarkRow("Selection", 0.72f, "O(n²)")
            BenchmarkRow("Insertion", 0.55f, "O(n²)")
        }
    }
}

@Composable
private fun BenchmarkRow(label: String, progress: Float, complexity: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text = label.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(76.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.weight(1f).height(6.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.18f)
        )
        Text(text = complexity, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2D1B69),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BarChartVisualization(
    data: List<Int>,
    comparingIndices: Set<Int>,
    swappingIndices: Set<Int>,
    sortedIndices: Set<Int>,
    currentIndex: Int?
) {
    if (data.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data to visualize",
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Enhanced Legend
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("Unsorted", Color(0xFF64748B), Color(0xFF94A3B8))
                LegendItem("Comparing", Color(0xFFF59E0B), Color(0xFFFBBF24))
                LegendItem("Swapping", Color(0xFFDC2626), Color(0xFFEF4444))
                LegendItem("Sorted", Color(0xFF059669), Color(0xFF10B981))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Chart with better spacing
        val maxValue = data.maxOrNull() ?: 1
        val barAreaHeight = 140.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .padding(6.dp)
                .drawBehind {
                    val lineCount = 4
                    val step = size.height / (lineCount + 1)
                    for (i in 1..lineCount) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, step * i),
                            end = Offset(size.width, step * i),
                            strokeWidth = 1f
                        )
                    }
                    drawLine(
                        color = Color.White.copy(alpha = 0.12f),
                        start = Offset(0f, size.height - 2f),
                        end = Offset(size.width, size.height - 2f),
                        strokeWidth = 2f
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Value text above bar
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = value.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bar area keeps a consistent baseline
                        val (startColor, endColor) = when {
                            sortedIndices.contains(index) -> Color(0xFF059669) to Color(0xFF10B981)
                            swappingIndices.contains(index) -> Color(0xFFDC2626) to Color(0xFFEF4444)
                            comparingIndices.contains(index) -> Color(0xFFF59E0B) to Color(0xFFFBBF24)
                            else -> Color(0xFF64748B) to Color(0xFF94A3B8)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(barAreaHeight),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            val fraction = (value.toFloat() / maxValue).coerceIn(0f, 1f)
                            val barHeight = (barAreaHeight.value * fraction).dp.coerceAtLeast(16.dp)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(barHeight)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                                    .shadow(4.dp, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp), clip = false)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(startColor, endColor)
                                        ),
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                    )
                                    .then(
                                        if (swappingIndices.contains(index) || comparingIndices.contains(index)) {
                                            Modifier.background(
                                                Color.White.copy(alpha = 0.2f),
                                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.6f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Index label with current-step highlight
                        val indexBadgeColor = when {
                            swappingIndices.contains(index) -> Color(0xFFEF4444).copy(alpha = 0.35f)
                            comparingIndices.contains(index) -> Color(0xFFF59E0B).copy(alpha = 0.35f)
                            else -> Color(0xFF5EEAD4).copy(alpha = 0.28f)
                        }
                        val indexTextColor = when {
                            swappingIndices.contains(index) -> Color(0xFFFECACA)
                            comparingIndices.contains(index) -> Color(0xFFFDE68A)
                            else -> Color(0xFF9AF3E5)
                        }

                        Surface(
                            shape = CircleShape,
                            color = indexBadgeColor
                        ) {
                            Text(
                                text = index.toString(),
                                color = indexTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun LegendItem(label: String, startColor: Color, endColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(startColor, endColor)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GraphVisualization(
    graphData: com.algoviz.plus.domain.model.GraphData
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw edges first (so they appear behind nodes)
        graphData.edges.forEach { edge ->
            val fromNode = graphData.nodes.find { it.id == edge.from } ?: return@forEach
            val toNode = graphData.nodes.find { it.id == edge.to } ?: return@forEach

            val isActive = graphData.activeEdges.contains(Pair(edge.from, edge.to))
            val edgeColor = if (isActive) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.3f)
            val strokeWidth = if (isActive) 4f else 2f

            drawLine(
                color = edgeColor,
                start = Offset(fromNode.x * canvasWidth, fromNode.y * canvasHeight),
                end = Offset(toNode.x * canvasWidth, toNode.y * canvasHeight),
                strokeWidth = strokeWidth
            )

            // Draw weight label
            if (edge.weight > 1 || graphData.distances.isNotEmpty()) {
                val midX = (fromNode.x + toNode.x) / 2 * canvasWidth
                val midY = (fromNode.y + toNode.y) / 2 * canvasHeight

                drawCircle(
                    color = Color(0xFF1E293B),
                    radius = 18f,
                    center = Offset(midX, midY)
                )
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        edge.weight.toString(),
                        midX,
                        midY + 8f,
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        // Draw nodes
        graphData.nodes.forEach { node ->
            val x = node.x * canvasWidth
            val y = node.y * canvasHeight
            val nodeRadius = 40f

            val nodeColor = when {
                graphData.activeNodes.contains(node.id) -> Color(0xFFF59E0B)
                graphData.processedNodes.contains(node.id) -> Color(0xFF10B981)
                graphData.visitedNodes.contains(node.id) -> Color(0xFF3B82F6)
                else -> Color(0xFF64748B)
            }

            // Draw node circle
            drawCircle(
                color = nodeColor,
                radius = nodeRadius,
                center = Offset(x, y)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.2f),
                radius = nodeRadius - 3f,
                center = Offset(x, y),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Draw node label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    node.label,
                    x,
                    y + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }

            // Draw distance if available
            graphData.distances[node.id]?.let { dist ->
                if (dist != Int.MAX_VALUE) {
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            "d:$dist",
                            x,
                            y - nodeRadius - 10f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#FDE68A")
                                textSize = 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TreeVisualization(
    treeData: com.algoviz.plus.domain.model.TreeData
) {
    if (treeData.nodes.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Empty tree",
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val maxLevel = treeData.nodes.maxOfOrNull { it.level } ?: 0
        val levelHeight = if (maxLevel > 0) canvasHeight / (maxLevel + 2) else canvasHeight / 2

        // Calculate positions for each node
        val positions = mutableMapOf<Int, Pair<Float, Float>>()
        val nodesPerLevel = mutableMapOf<Int, Int>()

        treeData.nodes.forEach { node ->
            nodesPerLevel[node.level] = (nodesPerLevel[node.level] ?: 0) + 1
        }

        val levelCounters = mutableMapOf<Int, Int>()

        treeData.nodes.forEach { node ->
            val level = node.level
            val counter = levelCounters[level] ?: 0
            levelCounters[level] = counter + 1

            val totalInLevel = nodesPerLevel[level] ?: 1
            val x = canvasWidth * (counter + 1) / (totalInLevel + 1)
            val y = levelHeight * (level + 1)

            positions[node.id] = Pair(x, y)
        }

        // Draw edges
        treeData.nodes.forEach { node ->
            if (node.parentId != null) {
                val childPos = positions[node.id]!!
                val parentPos = positions[node.parentId]!!

                val edgeColor = if (treeData.visitedNodes.contains(node.value)) {
                    Color(0xFF3B82F6).copy(alpha = 0.6f)
                } else {
                    Color.White.copy(alpha = 0.3f)
                }

                drawLine(
                    color = edgeColor,
                    start = Offset(parentPos.first, parentPos.second),
                    end = Offset(childPos.first, childPos.second),
                    strokeWidth = 3f
                )
            }
        }

        // Draw nodes
        treeData.nodes.forEach { node ->
            val pos = positions[node.id]!!
            val nodeRadius = 35f

            val nodeColor = when {
                treeData.highlightedNodes.contains(node.value) -> Color(0xFF10B981)
                treeData.activeNode == node.id || treeData.visitedNodes.contains(node.value) -> Color(0xFF3B82F6)
                else -> Color(0xFF64748B)
            }

            // Draw node circle
            drawCircle(
                color = nodeColor,
                radius = nodeRadius,
                center = Offset(pos.first, pos.second)
            )

            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = nodeRadius - 2f,
                center = Offset(pos.first, pos.second),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Draw node value
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    node.value.toString(),
                    pos.first,
                    pos.second + 10f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}

@Composable
private fun MatrixVisualization(
    matrix: List<List<Int>>,
    activeIndices: Set<Int>
) {
    if (matrix.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No matrix data",
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        matrix.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.Center
            ) {
                row.forEachIndexed { colIndex, value ->
                    val isActive = activeIndices.contains(rowIndex) || activeIndices.contains(colIndex)
                    val backgroundColor = when {
                        value > 0 && isActive -> Color(0xFF3B82F6).copy(alpha = 0.4f)
                        value > 0 -> Color(0xFF10B981).copy(alpha = 0.3f)
                        else -> Color.White.copy(alpha = 0.05f)
                    }

                    Surface(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(40.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = backgroundColor,
                        border = BorderStroke(
                            1.dp,
                            if (isActive) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (value == -1) "-" else value.toString(),
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeTab(algorithm: com.algoviz.plus.domain.model.Algorithm?) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF252046),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Code implementation coming soon...",
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

