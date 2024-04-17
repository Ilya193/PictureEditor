package ru.ikom.pictureeditor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val navigation: Navigation<Screen>
) : ViewModel() {

    fun screen(): StateFlow<Screen> = navigation.read()

    fun openPermissions() = navigation.openPermissions()
    fun openEditor() = navigation.openEditor()
}