package com.algoviz.plus.ui.algorithms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.model.AlgorithmStep
import com.algoviz.plus.domain.model.PlaybackSpeed
import com.algoviz.plus.domain.model.VisualizationState
import com.algoviz.plus.domain.repository.AlgorithmRepository
import com.algoviz.plus.domain.usecase.GenerateAlgorithmStepsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class VisualizationViewModel @Inject constructor(
    private val generateStepsUseCase: GenerateAlgorithmStepsUseCase,
    private val algorithmRepository: AlgorithmRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    data class AlgorithmInputSpec(
        val key: String,
        val label: String,
        val placeholder: String,
        val numeric: Boolean
    )
    
    private val algorithmId: String = savedStateHandle["algorithmId"] ?: "bubble_sort"
    
    private val _algorithm = MutableStateFlow<Algorithm?>(null)
    val algorithm: StateFlow<Algorithm?> = _algorithm.asStateFlow()
    
    private val _visualizationState = MutableStateFlow(VisualizationState())
    val visualizationState: StateFlow<VisualizationState> = _visualizationState.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _algorithmParameterInput = MutableStateFlow("")
    val algorithmParameterInput: StateFlow<String> = _algorithmParameterInput.asStateFlow()

    private val _algorithmParameterError = MutableStateFlow<String?>(null)
    val algorithmParameterError: StateFlow<String?> = _algorithmParameterError.asStateFlow()

    private val _customInputError = MutableStateFlow<String?>(null)
    val customInputError: StateFlow<String?> = _customInputError.asStateFlow()
    
    private var allSteps: List<AlgorithmStep> = emptyList()
    private var playbackJob: Job? = null
    
    init {
        loadAlgorithm()
        generateInitialArray()
    }
    
    private fun loadAlgorithm() {
        viewModelScope.launch {
            _algorithm.value = algorithmRepository.getAlgorithmById(algorithmId)
        }
    }
    
    fun generateInitialArray(size: Int = 6) {
        val finalSize = (_algorithm.value?.defaultArraySize ?: size).coerceAtMost(10)
        val randomArray = List(finalSize) { Random.nextInt(10, 100) }
        _visualizationState.value = VisualizationState(
            array = randomArray,
            totalSteps = 0
        )
        allSteps = emptyList()
        _customInputError.value = null
    }

    fun applyCustomInput(rawInput: String) {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            _customInputError.value = "Enter values like: 5, 12, 3, 40"
            return
        }

        val tokens = trimmed.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        if (tokens.size > 10) {
            _customInputError.value = "You can visualize up to 10 numbers only"
            return
        }

        val parsed = mutableListOf<Int>()
        for (token in tokens) {
            val value = token.toIntOrNull()
            if (value == null) {
                _customInputError.value = "Invalid number: $token"
                return
            }
            parsed.add(value)
        }

        if (parsed.isEmpty()) {
            _customInputError.value = "Please enter at least one number"
            return
        }

        pause()
        _visualizationState.value = VisualizationState(
            array = parsed,
            totalSteps = 0
        )
        allSteps = emptyList()
        _customInputError.value = null
        generateSteps()
    }

    fun clearCustomInputError() {
        _customInputError.value = null
    }

    fun getAlgorithmInputSpec(): AlgorithmInputSpec? {
        return when (algorithmId) {
            "linear_search", "binary_search", "jump_search", "interpolation_search", "exponential_search", "ternary_search", "bst_search" ->
                AlgorithmInputSpec(
                    key = "target",
                    label = "Target Element",
                    placeholder = "Enter number to search (optional)",
                    numeric = true
                )

            "quick_select" ->
                AlgorithmInputSpec(
                    key = "kIndex",
                    label = "k-th Index",
                    placeholder = "Enter k index (0-based, optional)",
                    numeric = true
                )

            "trie_operations" ->
                AlgorithmInputSpec(
                    key = "words",
                    label = "Words",
                    placeholder = "Comma separated words (optional)",
                    numeric = false
                )

            else -> null
        }
    }

    fun setAlgorithmParameterInput(value: String) {
        _algorithmParameterInput.value = value
        if (_algorithmParameterError.value != null) {
            _algorithmParameterError.value = null
        }
    }

    fun clearAlgorithmParameterError() {
        _algorithmParameterError.value = null
    }
    
    fun generateSteps() {
        viewModelScope.launch {
            val extraInput = buildExtraInputMap() ?: return@launch
            _isGenerating.value = true
            try {
                val steps = generateStepsUseCase(algorithmId, _visualizationState.value.array, extraInput)
                allSteps = steps
                _visualizationState.value = _visualizationState.value.copy(
                    totalSteps = steps.size,
                    currentStep = 0
                )
                if (steps.isNotEmpty()) {
                    updateStateFromStep(steps[0])
                }
            } catch (e: Exception) {
                Timber.e(e, "Error generating steps")
            } finally {
                _isGenerating.value = false
            }
        }
    }
    
    fun play() {
        if (_visualizationState.value.isPlaying) return
        
        _visualizationState.value = _visualizationState.value.copy(isPlaying = true)
        
        playbackJob = viewModelScope.launch {
            while (_visualizationState.value.currentStep < allSteps.size - 1 && 
                   _visualizationState.value.isPlaying) {
                delay(_visualizationState.value.speed.delayMs)
                stepForward()
            }
            _visualizationState.value = _visualizationState.value.copy(
                isPlaying = false,
                isComplete = _visualizationState.value.currentStep >= allSteps.size - 1
            )
        }
    }
    
    fun pause() {
        playbackJob?.cancel()
        _visualizationState.value = _visualizationState.value.copy(isPlaying = false)
    }
    
    fun stepForward() {
        val currentStep = _visualizationState.value.currentStep
        if (currentStep < allSteps.size - 1) {
            val nextStep = currentStep + 1
            _visualizationState.value = _visualizationState.value.copy(currentStep = nextStep)
            updateStateFromStep(allSteps[nextStep])
        }
    }
    
    fun stepBackward() {
        val currentStep = _visualizationState.value.currentStep
        if (currentStep > 0) {
            val prevStep = currentStep - 1
            _visualizationState.value = _visualizationState.value.copy(currentStep = prevStep)
            updateStateFromStep(allSteps[prevStep])
        }
    }
    
    fun reset() {
        pause()
        if (allSteps.isNotEmpty()) {
            _visualizationState.value = _visualizationState.value.copy(
                currentStep = 0,
                isComplete = false
            )
            updateStateFromStep(allSteps[0])
        }
    }
    
    fun setSpeed(speed: PlaybackSpeed) {
        _visualizationState.value = _visualizationState.value.copy(speed = speed)
    }
    
    private fun updateStateFromStep(step: AlgorithmStep) {
        _visualizationState.value = _visualizationState.value.copy(
            array = step.array,
            comparingIndices = step.comparingIndices,
            swappingIndices = step.swappingIndices,
            sortedIndices = step.sortedIndices,
            currentIndex = step.currentIndex,
            comparisons = step.comparisons,
            swaps = step.swaps
        )
    }
    
    fun getCurrentStepData(): AlgorithmStep? {
        val currentStep = _visualizationState.value.currentStep
        return if (currentStep < allSteps.size) allSteps[currentStep] else null
    }
    
    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }

    private fun buildExtraInputMap(): Map<String, String>? {
        val spec = getAlgorithmInputSpec() ?: return emptyMap()
        val value = _algorithmParameterInput.value.trim()
        if (value.isBlank()) {
            return emptyMap()
        }

        if (spec.numeric && value.toIntOrNull() == null) {
            _algorithmParameterError.value = "${spec.label} must be a valid number"
            return null
        }

        if (spec.key == "kIndex") {
            val k = value.toIntOrNull() ?: return emptyMap()
            if (k < 0 || k >= _visualizationState.value.array.size) {
                _algorithmParameterError.value = "k index must be between 0 and ${_visualizationState.value.array.lastIndex}"
                return null
            }
        }

        return mapOf(spec.key to value)
    }
}
