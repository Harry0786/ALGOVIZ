package com.algoviz.plus.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.algoviz.plus.ui.learn.model.LearnItem
import com.algoviz.plus.ui.learn.model.LearnPlaylist
import com.algoviz.plus.ui.learn.model.LearnSection
import com.algoviz.plus.ui.learn.model.LearnSheet
import com.algoviz.plus.ui.learn.viewmodel.LearnViewModel

private enum class TopicFilter {
    ALL,
    IN_PROGRESS,
    COMPLETED,
    HAS_VISUALIZER
}

private data class ContinueTarget(
    val sheetId: String,
    val sectionId: String,
    val topicId: String,
    val sheetTitle: String,
    val sectionTitle: String,
    val topicTitle: String
)

@Composable
fun LearnScreen(
    onBackClick: () -> Unit,
    onVisualizeAlgorithm: (String) -> Unit,
    initialSheetId: String? = null,
    viewModel: LearnViewModel = hiltViewModel()
) {
    val completionMap by viewModel.completionMap.collectAsStateWithLifecycle()
    val sheetProgress by viewModel.sheetProgress.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var selectedSheetId by rememberSaveable { mutableStateOf(initialSheetId) }
    var selectedSectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTopicId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPlaylistId by rememberSaveable { mutableStateOf<String?>(null) }
    var showCreatePlaylistDialog by rememberSaveable { mutableStateOf(false) }
    var playlistTargetItemIds by remember { mutableStateOf<List<String>>(emptyList()) }

    val selectedSheet = selectedSheetId?.let { viewModel.findSheet(it) }
    val selectedSection = selectedSheet?.sections?.firstOrNull { it.id == selectedSectionId }
    val selectedTopic = selectedSection?.items?.firstOrNull { it.id == selectedTopicId }
    val selectedPlaylist = playlists.firstOrNull { it.id == selectedPlaylistId }
    val selectedPlaylistItems = remember(selectedPlaylist, viewModel.sheets) {
        selectedPlaylist?.itemIds.orEmpty().mapNotNull { viewModel.findItem(it) }
    }
    val continueTarget = remember(completionMap, viewModel.sheets) {
        findContinueTarget(viewModel.sheets, completionMap)
    }
    val overallProgress = remember(completionMap, viewModel.sheets) {
        val allItems = viewModel.sheets.flatMap { sheet -> sheet.allItems }
        if (allItems.isEmpty()) 0f else allItems.count { completionMap[it.id] == true }.toFloat() / allItems.size.toFloat()
    }
    val currentSectionItems = selectedSection?.items ?: emptyList()
    val currentTopicIndex = selectedTopic?.let { topic ->
        currentSectionItems.indexOfFirst { it.id == topic.id }
    } ?: -1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0B0B0D),
                        Color(0xFF141418),
                        Color(0xFF1A1A1F)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        when {
            selectedPlaylist != null && selectedSheet == null -> {
                LearnPlaylistTopics(
                    playlist = selectedPlaylist,
                    items = selectedPlaylistItems,
                    completionMap = completionMap,
                    onBack = { selectedPlaylistId = null },
                    onOpenTopic = { itemId ->
                        val location = findTopicLocation(viewModel.sheets, itemId)
                        if (location != null) {
                            selectedSheetId = location.sheetId
                            selectedSectionId = location.sectionId
                            selectedTopicId = location.topicId
                            selectedPlaylistId = null
                        }
                    }
                )
            }

            selectedSheet == null -> {
                LearnSheetList(
                    sheets = viewModel.sheets,
                    sheetProgress = sheetProgress,
                    playlists = playlists,
                    continueTarget = continueTarget,
                    overallProgress = overallProgress,
                    onBackClick = onBackClick,
                    onCreatePlaylist = { showCreatePlaylistDialog = true },
                    onOpenPlaylist = { selectedPlaylistId = it },
                    onOpenSheet = {
                        selectedSheetId = it
                        selectedSectionId = null
                        selectedTopicId = null
                    },
                    onContinueLearning = { target ->
                        selectedSheetId = target.sheetId
                        selectedSectionId = target.sectionId
                        selectedTopicId = target.topicId
                    }
                )
            }

            selectedSection == null -> {
                LearnSectionList(
                    sheet = selectedSheet,
                    completionMap = completionMap,
                    progress = sheetProgress[selectedSheet.id] ?: 0f,
                    onBack = {
                        selectedSheetId = null
                        selectedSectionId = null
                        selectedTopicId = null
                    },
                    onOpenSection = {
                        selectedSectionId = it
                        selectedTopicId = null
                    }
                )
            }

            selectedTopic == null -> {
                LearnTopicList(
                    sheetTitle = selectedSheet.title,
                    section = selectedSection,
                    completionMap = completionMap,
                    onBack = {
                        selectedSectionId = null
                        selectedTopicId = null
                    },
                    onOpenTopic = { selectedTopicId = it },
                    onToggleComplete = viewModel::setItemCompleted,
                    onAddTopicToPlaylist = { itemId ->
                        playlistTargetItemIds = listOf(itemId)
                    },
                    onAddSectionToPlaylist = { itemIds ->
                        playlistTargetItemIds = itemIds
                    }
                )
            }

            else -> {
                LearnTopicDetail(
                    sheetTitle = selectedSheet.title,
                    sectionTitle = selectedSection.title,
                    item = selectedTopic,
                    isCompleted = completionMap[selectedTopic.id] == true,
                    onBack = { selectedTopicId = null },
                    onToggleComplete = viewModel::setItemCompleted,
                    onAddToPlaylist = { itemId ->
                        playlistTargetItemIds = listOf(itemId)
                    },
                    onVisualizeAlgorithm = onVisualizeAlgorithm,
                    hasPrevious = currentTopicIndex > 0,
                    hasNext = currentTopicIndex in 0 until currentSectionItems.lastIndex,
                    onPrevious = {
                        if (currentTopicIndex > 0) {
                            selectedTopicId = currentSectionItems[currentTopicIndex - 1].id
                        }
                    },
                    onNext = {
                        if (currentTopicIndex >= 0 && currentTopicIndex < currentSectionItems.lastIndex) {
                            selectedTopicId = currentSectionItems[currentTopicIndex + 1].id
                        }
                    }
                )
            }
        }

        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { showCreatePlaylistDialog = false },
                onCreate = { name ->
                    viewModel.createPlaylist(name)
                    showCreatePlaylistDialog = false
                }
            )
        }

        if (playlistTargetItemIds.isNotEmpty()) {
            AddToPlaylistDialog(
                playlists = playlists,
                topicCount = playlistTargetItemIds.size,
                onDismiss = { playlistTargetItemIds = emptyList() },
                onCreateNew = {
                    playlistTargetItemIds = emptyList()
                    showCreatePlaylistDialog = true
                },
                onAddToPlaylist = { playlistId ->
                    playlistTargetItemIds.forEach { itemId ->
                        viewModel.addItemToPlaylist(playlistId, itemId)
                    }
                    playlistTargetItemIds = emptyList()
                }
            )
        }
    }
}

@Composable
private fun LearnSheetList(
    sheets: List<LearnSheet>,
    sheetProgress: Map<String, Float>,
    playlists: List<LearnPlaylist>,
    continueTarget: ContinueTarget?,
    overallProgress: Float,
    onBackClick: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onOpenPlaylist: (String) -> Unit,
    onOpenSheet: (String) -> Unit,
    onContinueLearning: (ContinueTarget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Column {
                Text(
                    text = "Learn",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Influencer cheat sheet tracks",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCreatePlaylist) {
                Icon(
                    imageVector = Icons.Outlined.AddCircle,
                    contentDescription = "Create Own Sheet",
                    tint = Color(0xFFF3F4F6)
                )
            }
        }

        if (playlists.isNotEmpty()) {
            Text(
                text = "Create Own Sheet",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                playlists.forEach { playlist ->
                    Surface(
                        onClick = { onOpenPlaylist(playlist.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = playlist.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${playlist.itemIds.size} topics",
                                color = Color(0xFFF3F4F6),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pick A Sheet",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Open the roadmap, choose a topic, and study each concept in full-screen detail.",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                LinearProgressIndicator(
                    progress = { overallProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = Color(0xFFF3F4F6),
                    trackColor = Color.White.copy(alpha = 0.12f)
                )
                Text(
                    text = "Overall progress ${(overallProgress * 100).toInt()}%",
                    color = Color(0xFFF3F4F6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (continueTarget != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF3F4F6).copy(alpha = 0.18f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFF3F4F6).copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Continue Learning",
                        color = Color(0xFFF3F4F6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${continueTarget.sheetTitle} • ${continueTarget.sectionTitle}",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                    Text(
                        text = continueTarget.topicTitle,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { onContinueLearning(continueTarget) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF3F4F6),
                            contentColor = Color(0xFF0B0B0D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Resume Topic", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sheets) { sheet ->
                val progress = sheetProgress[sheet.id] ?: 0f
                Card(
                    onClick = { onOpenSheet(sheet.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = sheet.influencer,
                            color = Color(0xFFF3F4F6),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sheet.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.75f)
                            )
                        }
                        Text(
                            text = sheet.structureNote,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = Color(0xFFF3F4F6),
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}% completed",
                            color = Color(0xFFF3F4F6),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LearnSectionList(
    sheet: LearnSheet,
    progress: Float,
    completionMap: Map<String, Boolean>,
    onBack: () -> Unit,
    onOpenSection: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = sheet.influencer,
                        color = Color(0xFFF3F4F6),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = sheet.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% complete",
                        color = Color(0xFFF3F4F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Text(
                    text = sheet.structureNote,
                    modifier = Modifier.padding(14.dp),
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }

        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFF3F4F6),
                trackColor = Color.White.copy(alpha = 0.14f)
            )
        }

        items(sheet.sections) { section ->
            val sectionProgress = sectionProgress(section, completionMap)
            val completedCount = section.items.count { completionMap[it.id] == true }
            Card(
                onClick = { onOpenSection(section.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = section.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = section.summary,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("$completedCount/${section.items.size}") },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = Color.White.copy(alpha = 0.11f),
                                disabledLabelColor = Color(0xFFF3F4F6)
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { sectionProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFFF3F4F6),
                        trackColor = Color.White.copy(alpha = 0.12f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun LearnTopicList(
    sheetTitle: String,
    section: LearnSection,
    completionMap: Map<String, Boolean>,
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onAddTopicToPlaylist: (String) -> Unit,
    onAddSectionToPlaylist: (List<String>) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(TopicFilter.ALL) }
    val filterScroll = rememberScrollState()
    val filteredItems = remember(query, selectedFilter, completionMap, section.items) {
        section.items.filter { item ->
            val queryMatch = query.isBlank() ||
                item.title.contains(query, ignoreCase = true) ||
                item.explanation.contains(query, ignoreCase = true) ||
                item.keyPoints.any { it.contains(query, ignoreCase = true) }

            val filterMatch = when (selectedFilter) {
                TopicFilter.ALL -> true
                TopicFilter.IN_PROGRESS -> completionMap[item.id] != true
                TopicFilter.COMPLETED -> completionMap[item.id] == true
                TopicFilter.HAS_VISUALIZER -> item.algorithmId != null
            }

            queryMatch && filterMatch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = sheetTitle,
                        color = Color(0xFFF3F4F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = section.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap any topic to open full-screen details",
                        color = Color.White.copy(alpha = 0.66f),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onAddSectionToPlaylist(section.items.map { it.id }) }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlaylistPlay,
                        contentDescription = "Add Section To Playlist",
                        tint = Color(0xFFF3F4F6)
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search topics...", color = Color.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFF3F4F6),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    cursorColor = Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopicFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = when (filter) {
                                    TopicFilter.ALL -> "All"
                                    TopicFilter.IN_PROGRESS -> "In Progress"
                                    TopicFilter.COMPLETED -> "Completed"
                                    TopicFilter.HAS_VISUALIZER -> "Has Visualizer"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.09f),
                            labelColor = Color.White.copy(alpha = 0.8f),
                            selectedContainerColor = Color(0xFFF3F4F6),
                            selectedLabelColor = Color(0xFF0B0B0D)
                        )
                    )
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.07f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No topics match the current filters.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White.copy(alpha = 0.74f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        items(filteredItems) { item ->
            LearnTopicCard(
                item = item,
                isCompleted = completionMap[item.id] == true,
                onOpenTopic = { onOpenTopic(item.id) },
                onToggleComplete = { checked -> onToggleComplete(item.id, checked) },
                onAddToPlaylist = { onAddTopicToPlaylist(item.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LearnTopicCard(
    item: LearnItem,
    isCompleted: Boolean,
    onOpenTopic: () -> Unit,
    onToggleComplete: (Boolean) -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Card(
        onClick = onOpenTopic,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141418).copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = onToggleComplete,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFFF3F4F6),
                        checkmarkColor = Color(0xFF0B0B0D),
                        uncheckedColor = Color.White.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = item.title,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFFF3F4F6),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.OpenInFull,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = item.explanation,
                color = Color.White.copy(alpha = 0.78f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 2
            )

            AssistChip(
                onClick = {},
                enabled = false,
                label = {
                    Text(
                        text = if (item.algorithmId != null) "Has Visualizer Link" else "Concept Topic",
                        fontSize = 11.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = Color.White.copy(alpha = 0.10f),
                    disabledLabelColor = Color.White.copy(alpha = 0.76f),
                    disabledLeadingIconContentColor = Color.White.copy(alpha = 0.76f)
                )
            )

            OutlinedButton(
                onClick = onAddToPlaylist,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlaylistPlay,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Add To Create Own Sheet", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LearnTopicDetail(
    sheetTitle: String,
    sectionTitle: String,
    item: LearnItem,
    isCompleted: Boolean,
    onBack: () -> Unit,
    onToggleComplete: (String, Boolean) -> Unit,
    onAddToPlaylist: (String) -> Unit,
    onVisualizeAlgorithm: (String) -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "$sheetTitle • $sectionTitle",
                        color = Color(0xFFF3F4F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Topic Details",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = {
                                Text(
                                    text = if (isCompleted) "Completed" else "In Progress",
                                    fontSize = 11.sp
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = if (isCompleted) Color(0xFFF3F4F6).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.11f),
                                disabledLabelColor = if (isCompleted) Color(0xFFF3F4F6) else Color.White.copy(alpha = 0.78f)
                            )
                        )
                    }

                    Text(
                        text = item.explanation,
                        color = Color.White.copy(alpha = 0.84f),
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )

                    TheoryApproachCard(
                        title = "Brute Force Approach",
                        content = item.bruteForceApproach.ifBlank {
                            defaultBruteForceExplanation(item)
                        }
                    )

                    TheoryApproachCard(
                        title = "Optimal Approach",
                        content = item.optimalApproach.ifBlank {
                            defaultOptimalExplanation(item)
                        }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Key Takeaways",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        item.keyPoints.forEachIndexed { index, point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF3F4F6).copy(alpha = 0.20f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color(0xFFF3F4F6),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Text(
                                    text = point,
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onToggleComplete(item.id, !isCompleted) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color.White.copy(alpha = 0.16f) else Color(0xFFF3F4F6),
                            contentColor = if (isCompleted) Color.White else Color(0xFF0B0B0D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCompleted) "Mark As In Progress" else "Mark As Completed",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = { onAddToPlaylist(item.id) },
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistPlay,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Add To Create Own Sheet", color = Color.White)
                    }

                    if (item.algorithmId != null) {
                        Button(
                            onClick = { onVisualizeAlgorithm(item.algorithmId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6),
                                contentColor = Color(0xFF0B0B0D)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(text = "Open In Visualizer", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPrevious,
                            enabled = hasPrevious,
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Text("Previous Topic", color = Color.White)
                        }
                        Button(
                            onClick = onNext,
                            enabled = hasNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.15f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next Topic", color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TheoryApproachCard(
    title: String,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFFF3F4F6),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = content,
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LearnPlaylistTopics(
    playlist: LearnPlaylist,
    items: List<LearnItem>,
    completionMap: Map<String, Boolean>,
    onBack: () -> Unit,
    onOpenTopic: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Create Own Sheet",
                        color = Color(0xFFF3F4F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = playlist.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${items.size} topics",
                        color = Color.White.copy(alpha = 0.66f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.07f)
                ) {
                    Text(
                        text = "No topics in this custom sheet yet.",
                        modifier = Modifier.padding(14.dp),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }

        items(items) { item ->
            LearnTopicCard(
                item = item,
                isCompleted = completionMap[item.id] == true,
                onOpenTopic = { onOpenTopic(item.id) },
                onToggleComplete = {},
                onAddToPlaylist = {}
            )
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Own Sheet") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Sheet Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddToPlaylistDialog(
    playlists: List<LearnPlaylist>,
    topicCount: Int,
    onDismiss: () -> Unit,
    onCreateNew: () -> Unit,
    onAddToPlaylist: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add To Create Own Sheet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select a custom sheet for ${topicCount} topic(s).")
                if (playlists.isEmpty()) {
                    Text(
                        text = "No custom sheet found. Create one first.",
                        color = Color.Gray
                    )
                } else {
                    playlists.forEach { playlist ->
                        OutlinedButton(
                            onClick = { onAddToPlaylist(playlist.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${playlist.name} (${playlist.itemIds.size})")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCreateNew) {
                Text("Create New")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private data class TopicLocation(
    val sheetId: String,
    val sectionId: String,
    val topicId: String
)

private fun findTopicLocation(
    sheets: List<LearnSheet>,
    itemId: String
): TopicLocation? {
    sheets.forEach { sheet ->
        sheet.sections.forEach { section ->
            if (section.items.any { it.id == itemId }) {
                return TopicLocation(sheet.id, section.id, itemId)
            }
        }
    }
    return null
}

private fun defaultBruteForceExplanation(item: LearnItem): String {
    return "Start by checking all straightforward possibilities for ${item.title}. " +
        "This usually means trying each candidate state, validating the condition, and keeping track of the best valid result. " +
        "The brute force route is easier to reason about and helps you verify correctness before optimization."
}

private fun defaultOptimalExplanation(item: LearnItem): String {
    val hint = when {
        item.tags.contains(com.algoviz.plus.ui.learn.model.LearnTopicTag.GRAPH) ->
            "Use graph traversal or shortest-path strategy with visited/state pruning."
        item.tags.contains(com.algoviz.plus.ui.learn.model.LearnTopicTag.DYNAMIC_PROGRAMMING) ->
            "Define a compact state transition and reuse overlapping subproblems."
        item.tags.contains(com.algoviz.plus.ui.learn.model.LearnTopicTag.SEARCHING) ->
            "Exploit monotonicity or ordering to cut the search space quickly."
        item.tags.contains(com.algoviz.plus.ui.learn.model.LearnTopicTag.SORTING) ->
            "Use ordering to avoid repeated comparisons and derive answer in fewer passes."
        else -> "Use the right data structure to reduce repeated work and improve lookups/updates."
    }
    return "$hint Focus on minimizing repeated computation while preserving correctness for edge cases."
}

private fun sectionProgress(section: LearnSection, completionMap: Map<String, Boolean>): Float {
    if (section.items.isEmpty()) return 0f
    val done = section.items.count { completionMap[it.id] == true }
    return done.toFloat() / section.items.size.toFloat()
    }

private fun findContinueTarget(
    sheets: List<LearnSheet>,
    completionMap: Map<String, Boolean>
): ContinueTarget? {
    for (sheet in sheets) {
        for (section in sheet.sections) {
            val nextIncomplete = section.items.firstOrNull { completionMap[it.id] != true }
            if (nextIncomplete != null) {
                return ContinueTarget(
                    sheetId = sheet.id,
                    sectionId = section.id,
                    topicId = nextIncomplete.id,
                    sheetTitle = sheet.title,
                    sectionTitle = section.title,
                    topicTitle = nextIncomplete.title
                )
            }
        }
    }
    return null
}


