package ru.ikom.permissions

import androidx.lifecycle.ViewModel

class PermissionsViewModel(
    private val router: PermissionsRouter
) : ViewModel() {

    fun openEditor() = router.openEditor()

    fun coup() = router.coup()
}