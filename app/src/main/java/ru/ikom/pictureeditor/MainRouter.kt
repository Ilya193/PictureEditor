package ru.ikom.pictureeditor

import ru.ikom.common.Router

interface MainRouter : Router {
    fun openEditor(imageUri: String)
}