package com.algoviz.plus.ui.algorithms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.model.AlgorithmCategory
import com.algoviz.plus.domain.usecase.GetAllAlgorithmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlgorithmListViewModel @Inject constructor(
    private val getAllAlgorithmsUseCase: GetAllAlgorithmsUseCase
) : ViewModel() {
    
    private val _algorithms = MutableStateFlow<List<Algorithm>>(emptyList())
    val algorithms: StateFlow<List<Algorithm>> = _algorithms.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<AlgorithmCategory?>(null)
    val selectedCategory: StateFlow<AlgorithmCategory?> = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        loadAlgorithms()
    }
    
    private fun loadAlgorithms() {
        viewModelScope.launch {
            getAllAlgorithmsUseCase().collect { allAlgorithms ->
                _algorithms.value = filterAlgorithms(allAlgorithms)
            }
        }
    }
    
    fun onCategorySelected(category: AlgorithmCategory?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            getAllAlgorithmsUseCase().collect { allAlgorithms ->
                _algorithms.value = filterAlgorithms(allAlgorithms)
            }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            getAllAlgorithmsUseCase().collect { allAlgorithms ->
                _algorithms.value = filterAlgorithms(allAlgorithms)
            }
        }
    }
    
    private fun filterAlgorithms(allAlgorithms: List<Algorithm>): List<Algorithm> {
        var filtered = allAlgorithms
        
        // Filter by category
        _selectedCategory.value?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        
        // Filter by search query
        if (_searchQuery.value.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(_searchQuery.value, ignoreCase = true) ||
                it.description.contains(_searchQuery.value, ignoreCase = true)
            }
        }
        
        return filtered
    }
}
