package com.algoviz.plus.ui.learn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.core.datastore.PreferencesManager
import com.algoviz.plus.ui.learn.data.CheatSheetCatalog
import com.algoviz.plus.ui.learn.model.LearnSheet
import com.algoviz.plus.ui.learn.model.LearnTopicTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeLearnProgress(
    val sorting: Float,
    val graph: Float,
    val dataStructures: Float
)

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val sheets: List<LearnSheet> = CheatSheetCatalog.sheets

    val completionMap: StateFlow<Map<String, Boolean>> =
        preferencesManager.learnItemCompletion.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    val sheetProgress: StateFlow<Map<String, Float>> =
        completionMap
            .combine(kotlinx.coroutines.flow.flowOf(sheets)) { completion, allSheets ->
                allSheets.associate { sheet ->
                    val completed = sheet.allItems.count { completion[it.id] == true }
                    val total = sheet.allItems.size.coerceAtLeast(1)
                    sheet.id to (completed.toFloat() / total.toFloat())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    val homeProgress: StateFlow<HomeLearnProgress> =
        completionMap
            .combine(kotlinx.coroutines.flow.flowOf(sheets)) { completion, allSheets ->
                val allItems = allSheets.flatMap { it.allItems }
                fun progressFor(tag: LearnTopicTag): Float {
                    val items = allItems.filter { tag in it.tags }
                    if (items.isEmpty()) return 0f
                    val done = items.count { completion[it.id] == true }
                    return done.toFloat() / items.size.toFloat()
                }

                val dataStructureTags = setOf(LearnTopicTag.DATA_STRUCTURES, LearnTopicTag.RECURSION)
                val dataStructureItems = allItems.filter { item -> item.tags.any { it in dataStructureTags } }
                val dataStructureProgress = if (dataStructureItems.isEmpty()) {
                    0f
                } else {
                    dataStructureItems.count { completion[it.id] == true }.toFloat() / dataStructureItems.size.toFloat()
                }

                HomeLearnProgress(
                    sorting = progressFor(LearnTopicTag.SORTING),
                    graph = progressFor(LearnTopicTag.GRAPH),
                    dataStructures = dataStructureProgress
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeLearnProgress(0f, 0f, 0f)
            )

    fun findSheet(sheetId: String): LearnSheet? = CheatSheetCatalog.findSheet(sheetId)

    fun isCompleted(itemId: String): Boolean = completionMap.value[itemId] == true

    fun setItemCompleted(itemId: String, completed: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLearnItemCompleted(itemId, completed)
        }
    }
}
