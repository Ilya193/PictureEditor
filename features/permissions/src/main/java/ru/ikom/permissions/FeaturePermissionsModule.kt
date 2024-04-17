package ru.ikom.permissions

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val featurePermissionsModule = module {
    viewModel<PermissionsViewModel> {
        PermissionsViewModel(get())
    }
}