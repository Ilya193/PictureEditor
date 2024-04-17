package ru.ikom.editor

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featureEditorModule = module {
    viewModel<EditorViewModel> {
        EditorViewModel(get())
    }
}