package ru.ikom.editor

import androidx.lifecycle.ViewModel

class EditorViewModel(
    private val router: EditorRouter
) : ViewModel() {
    fun coup() = router.coup()
}