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
    
    private val algorithmId: String = savedStateHandle["algorithmId"] ?: "bubble_sort"
    
    private val _algorithm = MutableStateFlow<Algorithm?>(null)
    val algorithm: StateFlow<Algorithm?> = _algorithm.asStateFlow()
    
    private val _visualizationState = MutableStateFlow(VisualizationState())
    val visualizationState: StateFlow<VisualizationState> = _visualizationState.asStateFlow()
    
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()
    
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
        val finalSize = _algorithm.value?.defaultArraySize ?: size
        val randomArray = List(finalSize) { Random.nextInt(10, 100) }
        _visualizationState.value = VisualizationState(
            array = randomArray,
            totalSteps = 0
        )
        allSteps = emptyList()
    }
    
    fun generateSteps() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val steps = generateStepsUseCase(algorithmId, _visualizationState.value.array)
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
    
    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
