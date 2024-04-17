package ru.ikom.editor

import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditorViewModel(
    private val router: EditorRouter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<Drawable?>(null)
    val uiState: StateFlow<Drawable?> get() = _uiState.asStateFlow()

    fun cache(path: Drawable? = null) = viewModelScope.launch(dispatcher) {
        _uiState.value = path
    }

    fun coup() = router.coup()
}