package ru.ikom.pictureeditor

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.ikom.editor.featureEditorModule
import ru.ikom.permissions.featurePermissionsModule

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule, featurePermissionsModule, featureEditorModule)
        }
    }
}