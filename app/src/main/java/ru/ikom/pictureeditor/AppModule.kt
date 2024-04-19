package ru.ikom.pictureeditor

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.ikom.editor.EditorRouter
import ru.ikom.permissions.PermissionsRouter

val appModule = module {
    val navigation = Navigation.Base()

    single<MainRouter> {
        navigation
    }

    single<Navigation<Screen>> {
        navigation
    }

    single<EditorRouter> {
        navigation
    }

    viewModel<MainViewModel> {
        MainViewModel(get())
    }
}