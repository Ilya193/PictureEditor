package ru.ikom.editor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class EditorViewModel(
    private val router: EditorRouter,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {
    fun coup() = router.coup()
}