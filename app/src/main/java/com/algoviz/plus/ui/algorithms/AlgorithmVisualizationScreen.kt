package com.algoviz.plus.ui.algorithms

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    
    LaunchedEffect(Unit) {
        viewModel.generateSteps()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1344),
                        Color(0xFF2D1B69),
                        Color(0xFF3D2080)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
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
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = algorithm?.name ?: "Loading...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VisualizationTab(
                state = state,
                algorithm = algorithm,
                isGenerating = isGenerating,
                getCurrentStepData = { viewModel.getCurrentStepData() },
                onPlay = { viewModel.play() },
                onPause = { viewModel.pause() },
                onStepForward = { viewModel.stepForward() },
                onStepBackward = { viewModel.stepBackward() },
                onReset = { viewModel.reset() },
                onSpeedChange = { viewModel.setSpeed(it) },
                onGenerateNew = { viewModel.generateInitialArray() }
            )
        }
    }
}

@Composable
private fun ComplexityBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            fontSize = 11.sp,
            color = Color(0xFF5EEAD4)
        )
    }
}

@Composable
private fun VisualizationTab(
    state: com.algoviz.plus.domain.model.VisualizationState,
    algorithm: com.algoviz.plus.domain.model.Algorithm?,
    isGenerating: Boolean,
    getCurrentStepData: () -> com.algoviz.plus.domain.model.AlgorithmStep?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStepForward: () -> Unit,
    onStepBackward: () -> Unit,
    onReset: () -> Unit,
    onSpeedChange: (PlaybackSpeed) -> Unit,
    onGenerateNew: () -> Unit
) {
    val maxStep = if (state.totalSteps > 0) state.totalSteps - 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description Card
        algorithm?.let {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ComplexityBadge("Time: ${it.timeComplexity.average}")
                        ComplexityBadge("Space: ${it.spaceComplexity.average}")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = it.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Playback Controls
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF2D1B69)
        ) {
            var speedMenuExpanded by remember { mutableStateOf(false) }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    LinearProgressIndicator(
                        progress = if (maxStep > 0) state.currentStep.toFloat() / maxStep else 0f,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF5EEAD4),
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                    
                    Text(
                        text = "${state.currentStep} / $maxStep",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    
                    // Speed Control
                    var speedMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        Surface(
                            onClick = { speedMenuExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Speed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = state.speed.displayName,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset
                    IconButton(
                        onClick = {
                            speedMenuExpanded = false
                            onReset()
                        },
                        enabled = !isGenerating && state.totalSteps > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = if (state.totalSteps > 0) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Step Backward
                    IconButton(
                        onClick = {
                            speedMenuExpanded = false
                            onStepBackward()
                        },
                        enabled = !isGenerating && state.currentStep > 0 && !state.isPlaying
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Step Backward",
                            tint = if (!state.isPlaying && state.currentStep > 0) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Play/Pause
                    FloatingActionButton(
                        onClick = {
                            speedMenuExpanded = false
                            if (state.isPlaying) onPause() else onPlay()
                        },
                        containerColor = Color(0xFF5EEAD4),
                        contentColor = Color(0xFF1A1344),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Step Forward
                    IconButton(
                        onClick = {
                            speedMenuExpanded = false
                            onStepForward()
                        },
                        enabled = !isGenerating && state.currentStep < state.totalSteps - 1 && !state.isPlaying
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Step Forward",
                            tint = if (!state.isPlaying && state.currentStep < state.totalSteps - 1) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Generate New
                    IconButton(
                        onClick = { 
                            speedMenuExpanded = false
                            onPause()
                            onGenerateNew()
                        },
                        enabled = !isGenerating && !state.isPlaying
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Generate New",
                            tint = if (!state.isPlaying) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
        
        // Stats Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                value = "${(if (maxStep > 0) (state.currentStep.toFloat() / maxStep * 100).toInt() else 0)}%",
                label = "Progress",
                color = Color(0xFF06B6D4)
            )
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                value = "${state.comparisons}",
                label = "Comparisons",
                color = Color(0xFFF59E0B)
            )
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                value = "${state.swaps}",
                label = "Swaps",
                color = Color(0xFFEF4444)
            )
            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                value = "${state.sortedIndices.size}/${state.array.size}",
                label = "Sorted",
                color = Color(0xFF10B981)
            )
        }

        // Current Step Info
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Current Step: ${state.currentStep} / $maxStep",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (state.comparingIndices.isNotEmpty() || state.swappingIndices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when {
                            state.swappingIndices.isNotEmpty() -> "Swapping elements at positions ${state.swappingIndices.joinToString()}"
                            state.comparingIndices.isNotEmpty() -> "Comparing elements at positions ${state.comparingIndices.joinToString()}"
                            else -> "Processing..."
                        },
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Visualization Chart
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF252046),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            if (isGenerating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF5EEAD4))
                        Text(
                            text = "Generating steps...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Switch visualization based on algorithm category
                when (algorithm?.category) {
                    com.algoviz.plus.domain.model.AlgorithmCategory.GRAPH -> {
                        // Get the current step's graphData
                        val graphData = getCurrentStepData()?.graphData
                        if (graphData != null) {
                            GraphVisualization(graphData = graphData)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No graph data available",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    com.algoviz.plus.domain.model.AlgorithmCategory.TREE -> {
                        val treeData = getCurrentStepData()?.treeData
                        if (treeData != null) {
                            TreeVisualization(treeData = treeData)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No tree data available",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    com.algoviz.plus.domain.model.AlgorithmCategory.DYNAMIC_PROGRAMMING -> {
                        val matrix = getCurrentStepData()?.matrix
                        if (matrix != null) {
                            MatrixVisualization(
                                matrix = matrix,
                                activeIndices = state.comparingIndices
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matrix data available",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    else -> {
                        // Default to bar chart for sorting and searching
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

        LearnMoreSection(algorithmId = algorithm?.id)
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

private data class LearnMoreContent(
    val keyConcepts: List<String>,
    val pseudocode: List<String>,
    val applications: List<String>
)

@Composable
private fun LearnMoreSection(algorithmId: String?) {
    val content = getLearnMoreContent(algorithmId)
    var conceptsExpanded by remember { mutableStateOf(true) }
    var pseudocodeExpanded by remember { mutableStateOf(false) }
    var applicationsExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.White
                )
                Text(
                    text = "Learn More",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            LearnMoreDropdown(
                title = "Key Concepts",
                expanded = conceptsExpanded,
                onToggle = { conceptsExpanded = !conceptsExpanded }
            ) {
                LearnMoreList(items = content.keyConcepts)
            }

            LearnMoreDropdown(
                title = "Pseudocode",
                expanded = pseudocodeExpanded,
                onToggle = { pseudocodeExpanded = !pseudocodeExpanded }
            ) {
                LearnMoreCodeList(lines = content.pseudocode)
            }

            LearnMoreDropdown(
                title = "Applications",
                expanded = applicationsExpanded,
                onToggle = { applicationsExpanded = !applicationsExpanded }
            ) {
                LearnMoreList(items = content.applications)
            }
        }
    }
}

@Composable
private fun LearnMoreDropdown(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    }
}

@Composable
private fun LearnMoreList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEachIndexed { index, text ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}.",
                    fontSize = 12.sp,
                    color = Color(0xFF9AF3E5)
                )
                Text(
                    text = text,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun LearnMoreCodeList(lines: List<String>) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.Black.copy(alpha = 0.2f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            lines.forEach { line ->
                Text(
                    text = line,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

private fun getLearnMoreContent(algorithmId: String?): LearnMoreContent {
    return when (algorithmId) {
        "bubble_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Repeatedly compare adjacent elements",
                "Swap if out of order",
                "Largest element bubbles to the end each pass"
            ),
            pseudocode = listOf(
                "repeat",
                "  swapped = false",
                "  for i = 0 to n-2",
                "    if A[i] > A[i+1] swap",
                "  end",
                "until not swapped"
            ),
            applications = listOf(
                "Teaching basic sorting",
                "Small datasets",
                "Detecting if array is already sorted"
            )
        )
        "selection_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Select the minimum each pass",
                "Grow the sorted prefix",
                "In-place with minimal swaps"
            ),
            pseudocode = listOf(
                "for i = 0 to n-2",
                "  min = i",
                "  for j = i+1 to n-1",
                "    if A[j] < A[min] min = j",
                "  swap A[i], A[min]"
            ),
            applications = listOf(
                "Small lists",
                "Memory-limited environments",
                "Educational demos"
            )
        )
        "insertion_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Builds a sorted prefix",
                "Insert current item in correct position",
                "Efficient on nearly sorted data"
            ),
            pseudocode = listOf(
                "for i = 1 to n-1",
                "  key = A[i]",
                "  j = i-1",
                "  while j>=0 and A[j] > key",
                "    A[j+1] = A[j]",
                "  A[j+1] = key"
            ),
            applications = listOf(
                "Small or nearly sorted arrays",
                "Online sorting",
                "Hybrid sorting algorithms"
            )
        )
        "merge_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Divide and conquer",
                "Stable sorting",
                "Merge sorted halves"
            ),
            pseudocode = listOf(
                "mergeSort(A)",
                "  if size <= 1 return",
                "  split into L and R",
                "  mergeSort(L); mergeSort(R)",
                "  merge(L, R)"
            ),
            applications = listOf(
                "Large datasets",
                "Linked lists",
                "External sorting"
            )
        )
        "quick_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Partition around a pivot",
                "Divide and conquer",
                "In-place average O(n log n)"
            ),
            pseudocode = listOf(
                "quickSort(A, low, high)",
                "  if low < high",
                "    p = partition(A)",
                "    quickSort(A, low, p-1)",
                "    quickSort(A, p+1, high)"
            ),
            applications = listOf(
                "General-purpose sorting",
                "In-memory sorting",
                "Library sort implementations"
            )
        )
        "heap_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Build a max heap",
                "Swap root with last",
                "Heapify remaining elements"
            ),
            pseudocode = listOf(
                "buildMaxHeap(A)",
                "for i = n-1 downTo 1",
                "  swap A[0], A[i]",
                "  heapify(0, i)"
            ),
            applications = listOf(
                "Guaranteed O(n log n)",
                "Memory constrained systems",
                "Priority queues"
            )
        )
        "shell_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Gapped insertion sort",
                "Reduce gap each pass",
                "Faster than insertion on random data"
            ),
            pseudocode = listOf(
                "gap = n/2",
                "while gap > 0",
                "  gapped insertion sort",
                "  gap = gap/2"
            ),
            applications = listOf(
                "Medium-sized arrays",
                "In-place sorting",
                "When simple code is needed"
            )
        )
        "counting_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Count frequency of keys",
                "Prefix sums to place values",
                "Non-comparison sort"
            ),
            pseudocode = listOf(
                "count keys",
                "prefix sum counts",
                "place values into output"
            ),
            applications = listOf(
                "Small integer ranges",
                "Stable sorting",
                "Radix sort subroutine"
            )
        )
        "radix_sort" -> LearnMoreContent(
            keyConcepts = listOf(
                "Sort by digits from least significant",
                "Stable digit sort each pass",
                "Non-comparison sort"
            ),
            pseudocode = listOf(
                "for exp = 1,10,100...",
                "  countingSortByDigit(A, exp)"
            ),
            applications = listOf(
                "Large integer sorting",
                "Fixed-length keys",
                "Sorting strings"
            )
        )
        "linear_search" -> LearnMoreContent(
            keyConcepts = listOf(
                "Check each element in order",
                "Stop when found",
                "No preprocessing required"
            ),
            pseudocode = listOf(
                "for i = 0 to n-1",
                "  if A[i] == target return i",
                "return not found"
            ),
            applications = listOf(
                "Small arrays",
                "Unsorted data",
                "One-off searches"
            )
        )
        "binary_search" -> LearnMoreContent(
            keyConcepts = listOf(
                "Requires sorted array",
                "Divide search range in half",
                "O(log n) time"
            ),
            pseudocode = listOf(
                "low = 0, high = n-1",
                "while low <= high",
                "  mid = (low+high)/2",
                "  compare A[mid] with target"
            ),
            applications = listOf(
                "Fast lookups",
                "Search in sorted lists",
                "Standard library usage"
            )
        )
        "bfs" -> LearnMoreContent(
            keyConcepts = listOf(
                "Level-by-level traversal",
                "Uses a queue data structure",
                "Finds shortest path in unweighted graphs"
            ),
            pseudocode = listOf(
                "queue.enqueue(start)",
                "while queue not empty",
                "  node = queue.dequeue()",
                "  visit node",
                "  enqueue unvisited neighbors"
            ),
            applications = listOf(
                "Shortest path in unweighted graphs",
                "Level-order tree traversal",
                "Web crawlers, social networks"
            )
        )
        "dfs" -> LearnMoreContent(
            keyConcepts = listOf(
                "Go deep before going wide",
                "Uses recursion or stack",
                "Explores all paths from root"
            ),
            pseudocode = listOf(
                "visit(node)",
                "  mark node as visited",
                "  for each neighbor",
                "    if not visited",
                "      visit(neighbor)"
            ),
            applications = listOf(
                "Cycle detection",
                "Topological sorting",
                "Path finding, maze solving"
            )
        )
        "dijkstra" -> LearnMoreContent(
            keyConcepts = listOf(
                "Shortest path in weighted graphs",
                "Greedy algorithm with priority queue",
                "Non-negative edge weights"
            ),
            pseudocode = listOf(
                "dist[start] = 0, others = ∞",
                "while unvisited nodes",
                "  u = node with min dist",
                "  for each neighbor v",
                "    relax(u, v)"
            ),
            applications = listOf(
                "GPS navigation",
                "Network routing protocols",
                "Game pathfinding"
            )
        )
        "bst_insertion" -> LearnMoreContent(
            keyConcepts = listOf(
                "Maintains BST property",
                "Left < Parent < Right",
                "O(h) time where h is height"
            ),
            pseudocode = listOf(
                "if tree is empty",
                "  create root",
                "else compare with current",
                "  go left if smaller",
                "  go right if larger"
            ),
            applications = listOf(
                "Maintaining sorted data",
                "Database indexing",
                "Symbol tables"
            )
        )
        "bst_search" -> LearnMoreContent(
            keyConcepts = listOf(
                "Binary search on tree structure",
                "Compare and go left or right",
                "Average O(log n) time"
            ),
            pseudocode = listOf(
                "while current != null",
                "  if value == current.val return",
                "  else if value < current.val",
                "    current = current.left",
                "  else current = current.right"
            ),
            applications = listOf(
                "Fast lookups in sorted data",
                "Dictionary implementations",
                "Range queries"
            )
        )
        "inorder_traversal" -> LearnMoreContent(
            keyConcepts = listOf(
                "Left → Root → Right order",
                "Produces sorted output for BST",
                "Recursive or stack-based"
            ),
            pseudocode = listOf(
                "inorder(node)",
                "  if node != null",
                "    inorder(node.left)",
                "    visit(node)",
                "    inorder(node.right)"
            ),
            applications = listOf(
                "Getting sorted elements from BST",
                "Expression tree evaluation",
                "Syntax tree processing"
            )
        )
        "preorder_traversal" -> LearnMoreContent(
            keyConcepts = listOf(
                "Root → Left → Right order",
                "Process root before children",
                "Used for copying trees"
            ),
            pseudocode = listOf(
                "preorder(node)",
                "  if node != null",
                "    visit(node)",
                "    preorder(node.left)",
                "    preorder(node.right)"
            ),
            applications = listOf(
                "Creating a copy of tree",
                "Prefix expression notation",
                "Tree serialization"
            )
        )
        "lcs" -> LearnMoreContent(
            keyConcepts = listOf(
                "Dynamic programming on 2D table",
                "Match characters or take max",
                "O(m×n) time and space"
            ),
            pseudocode = listOf(
                "for i,j in strings",
                "  if s1[i]==s2[j]",
                "    dp[i][j] = dp[i-1][j-1]+1",
                "  else dp[i][j] = max(dp[i-1][j],dp[i][j-1])"
            ),
            applications = listOf(
                "Diff algorithms",
                "DNA sequence alignment",
                "Version control systems"
            )
        )
        "knapsack" -> LearnMoreContent(
            keyConcepts = listOf(
                "Choose items to maximize value",
                "Weight constraint",
                "DP table: items × capacity"
            ),
            pseudocode = listOf(
                "for i in items",
                "  for w in weights",
                "    if item[i] fits",
                "      dp[i][w] = max(include, exclude)"
            ),
            applications = listOf(
                "Resource allocation",
                "Budget optimization",
                "Cargo loading"
            )
        )
        "lis" -> LearnMoreContent(
            keyConcepts = listOf(
                "Find longest increasing subsequence",
                "dp[i] = max length ending at i",
                "O(n²) or O(n log n) possible"
            ),
            pseudocode = listOf(
                "for each element i",
                "  for each j < i",
                "    if A[j] < A[i]",
                "      dp[i] = max(dp[i], dp[j]+1)"
            ),
            applications = listOf(
                "Stock price analysis",
                "Patience sorting",
                "Time series analysis"
            )
        )
        "coin_change" -> LearnMoreContent(
            keyConcepts = listOf(
                "Minimum coins to make amount",
                "DP: dp[i] = min coins for amount i",
                "Consider all coin denominations"
            ),
            pseudocode = listOf(
                "dp[0] = 0, others = ∞",
                "for amount in 1..target",
                "  for coin in coins",
                "    dp[amount] = min(dp[amount], dp[amount-coin]+1)"
            ),
            applications = listOf(
                "Currency change systems",
                "Vending machines",
                "Payment optimization"
            )
        )
        "activity_selection" -> LearnMoreContent(
            keyConcepts = listOf(
                "Greedy algorithm for scheduling",
                "Select non-overlapping activities",
                "Always pick earliest finishing activity"
            ),
            pseudocode = listOf(
                "sort by finish time",
                "select first activity",
                "for remaining activities",
                "  if start >= last finish",
                "    select this activity"
            ),
            applications = listOf(
                "Conference room scheduling",
                "Task scheduling",
                "Resource allocation"
            )
        )
        "huffman_coding" -> LearnMoreContent(
            keyConcepts = listOf(
                "Build optimal prefix-free code",
                "Greedy tree construction",
                "Variable-length encoding"
            ),
            pseudocode = listOf(
                "create heap of nodes",
                "while more than one node",
                "  extract two min nodes",
                "  create parent with their sum",
                "  insert parent back"
            ),
            applications = listOf(
                "Data compression (DEFLATE, MP3)",
                "Optimal prefix-free coding",
                "Information theory"
            )
        )
        "n_queens" -> LearnMoreContent(
            keyConcepts = listOf(
                "Place n queens safely on board",
                "No two queens can attack",
                "Backtracking to explore solutions"
            ),
            pseudocode = listOf(
                "solve(row)",
                "  if row == n return solution",
                "  for col in 0..n-1",
                "    if isSafe(row, col)",
                "      place queen",
                "      solve(row+1)",
                "      backtrack"
            ),
            applications = listOf(
                "Constraint satisfaction",
                "Puzzle solving",
                "Artificial intelligence search"
            )
        )
        "sudoku_solver" -> LearnMoreContent(
            keyConcepts = listOf(
                "Fill 9×9 grid with digits 1-9",
                "Backtracking with constraint checking",
                "Rows, columns, 3×3 boxes unique"
            ),
            pseudocode = listOf(
                "findEmpty()",
                "  for each cell",
                "    try digit 1-9",
                "    if valid recurse",
                "    else backtrack",
                "  return no solution"
            ),
            applications = listOf(
                "Puzzle games",
                "Constraint satisfaction",
                "Combinatorial search"
            )
        )
        "kmp" -> LearnMoreContent(
            keyConcepts = listOf(
                "Knuth-Morris-Pratt string matching",
                "Build failure function (LPS array)",
                "Efficiently skip mismatches"
            ),
            pseudocode = listOf(
                "build LPS array for pattern",
                "i=0, j=0",
                "while i < text length",
                "  if match or j==0",
                "    move both pointers",
                "  else j = LPS[j-1]"
            ),
            applications = listOf(
                "Text searching",
                "Pattern matching",
                "DNA sequence analysis"
            )
        )
        "bellman_ford" -> LearnMoreContent(
            keyConcepts = listOf(
                "Shortest path with negative edges",
                "Relax all edges repeatedly",
                "Detect negative cycles"
            ),
            pseudocode = listOf(
                "dist[src] = 0",
                "for i = 1 to n-1",
                "  for each edge (u,v,w)",
                "    dist[v] = min(dist[v], dist[u]+w)",
                "check for negative cycles"
            ),
            applications = listOf(
                "Network routing",
                "Negative edge handling",
                "Cycle detection"
            )
        )
        "kruskal" -> LearnMoreContent(
            keyConcepts = listOf(
                "Minimum spanning tree (MST)",
                "Kruskal's greedy algorithm",
                "Sort edges, add if no cycle (Union-Find)"
            ),
            pseudocode = listOf(
                "sort edges by weight",
                "for each edge in sorted order",
                "  if endpoints in different sets",
                "    add to MST",
                "    union sets"
            ),
            applications = listOf(
                "Network design minimizing cost",
                "Cluster analysis",
                "Game development"
            )
        )
        "prim" -> LearnMoreContent(
            keyConcepts = listOf(
                "Minimum spanning tree (MST)",
                "Prim's greedy algorithm",
                "Grow tree vertex by vertex"
            ),
            pseudocode = listOf(
                "start with any vertex",
                "while vertices remain",
                "  find min edge crossing cut",
                "  add vertex and edge to MST"
            ),
            applications = listOf(
                "Network design optimization",
                "Infrastructure planning",
                "Cluster analysis"
            )
        )
        else -> LearnMoreContent(
            keyConcepts = listOf("Details coming soon."),
            pseudocode = listOf("// Pseudocode coming soon"),
            applications = listOf("Applications coming soon.")
        )
    }
}
