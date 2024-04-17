package ru.ikom.pictureeditor

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.ikom.editor.EditorFragment
import ru.ikom.editor.EditorRouter
import ru.ikom.permissions.PermissionsFragment
import ru.ikom.permissions.PermissionsRouter

interface Navigation<T> : MainRouter {
    fun read(): StateFlow<T>
    fun update(value: T)

    class Base : Navigation<Screen>, PermissionsRouter, EditorRouter {
        private val screen = MutableStateFlow<Screen>(Screen.Empty)

        override fun read(): StateFlow<Screen> = screen.asStateFlow()

        override fun update(value: Screen) {
            screen.value = value
        }

        override fun coup() {
            update(Screen.Coup)
        }

        override fun openPermissions() {
            update(PermissionsScreen())
        }

        override fun openEditor() {
            update(EditorScreen())
        }
    }
}

interface Screen {
    fun show(supportFragmentManager: FragmentManager, container: Int) = Unit

    abstract class Replace(
        private val fragment: Fragment
    ) : Screen {
        override fun show(supportFragmentManager: FragmentManager, container: Int) {
            supportFragmentManager.beginTransaction()
                .replace(container, fragment)
                .commit()
        }
    }

    data object Coup : Screen

    data object Empty : Screen
}

class PermissionsScreen : Screen.Replace(PermissionsFragment.newInstance())
class EditorScreen : Screen.Replace(EditorFragment.newInstance())