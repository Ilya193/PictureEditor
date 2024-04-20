package ru.ikom.pictureeditor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.ikom.pictureeditor.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        clearCacheDir()
        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW == intent.action || Intent.ACTION_EDIT == intent.action) {
                if (intent.type?.startsWith("image/") == true) {
                    val imageUri = intent.data
                    if (imageUri != null) viewModel.openEditor(imageUri.toString())
                }
            } else viewModel.openEditor()
        }
        lifecycleScope.launch {
            viewModel.screen().flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).collect {
                it.show(supportFragmentManager, R.id.fragmentContainer)
            }
        }
    }

    private fun clearCacheDir() {
        if (cacheDir.isDirectory) {
            val files = cacheDir.listFiles()
            files?.let {
                for (file in it) {
                    file.delete()
                }
            }
        }
    }
}