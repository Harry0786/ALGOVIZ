package com.algoviz.plus.core.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.algoviz.plus.core.common.dispatcher.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>(
    initialState: State,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<Effect>()
    val uiEffect: SharedFlow<Effect> = _uiEffect.asSharedFlow()

    protected val currentState: State
        get() = _uiState.value

    abstract fun handleEvent(event: Event)

    protected fun updateState(update: State.() -> State) {
        _uiState.value = currentState.update()
    }

    protected fun sendEffect(effect: Effect) {
        viewModelScope.launch(dispatchers.main) {
            _uiEffect.emit(effect)
        }
    }

    protected fun launchIO(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(dispatchers.io, block = block)
    }

    protected fun launchDefault(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(dispatchers.default, block = block)
    }
}

interface UiState
interface UiEvent
interface UiEffect
