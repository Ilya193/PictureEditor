package ru.ikom.editor

interface PermissionsListener {
    fun successModeSave()
    fun successModeShare()
    fun cancelPermissions()
}