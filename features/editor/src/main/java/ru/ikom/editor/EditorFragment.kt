package ru.ikom.editor

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.nvt.color.ColorPickerDialog
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.ikom.editor.databinding.FragmentEditorBinding
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding: FragmentEditorBinding get() = _binding!!

    private val viewModel: EditorViewModel by viewModel()

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val file = getFileFromUri(uri)
                val drawable = Drawable.createFromPath(file.absolutePath)
                drawable?.let {
                    //viewModel.cache(it)
                    val bitmap = Bitmap.createBitmap(
                        it.intrinsicWidth,
                        it.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(bitmap)
                    it.setBounds(0, 0, canvas.width, canvas.height)
                    it.draw(canvas)

                    if (it.minimumHeight < it.minimumWidth) {
                        val originalBitmap = BitmapDrawable(resources, bitmap).bitmap
                        val matrix = Matrix()
                        matrix.postRotate(90f)

                        val rotatedBitmap = Bitmap.createBitmap(
                            originalBitmap,
                            0,
                            0,
                            originalBitmap.getWidth(),
                            originalBitmap.getHeight(),
                            matrix,
                            true
                        )
                        binding.drawingView.setImageBitmap(rotatedBitmap)
                        //requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        binding.drawingView.setImageBitmap(bitmap)
                        //requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icSave.setOnClickListener {
            binding.drawingView.save { }
        }

        binding.icPalette.setOnClickListener {
            binding.drawingView.let { view ->
                ColorPickerDialog(
                    context,
                    view.color,
                    true,
                    object : ColorPickerDialog.OnColorPickerListener {
                        override fun onCancel(dialog: ColorPickerDialog?) {}
                        override fun onOk(dialog: ColorPickerDialog?, color: Int) {
                            binding.drawingView.color = color
                        }
                    }).show()
            }

        }

        binding.icUndo.setOnClickListener {
            binding.drawingView.undo()
        }

        binding.icDelete.setOnClickListener {
            binding.drawingView.clear()
            viewModel.cache()
        }

        binding.icShare.setOnClickListener {
            binding.drawingView.save {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, it)
                    type = "image/jpeg"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }
        }

        binding.icFolder.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).collect {
                it?.let { drawable ->
                    /*val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                    drawable.draw(canvas)

                    val originalBitmap = BitmapDrawable(resources, bitmap).bitmap
                    val matrix = Matrix()
                    matrix.postRotate(90f)

                    val rotatedBitmap = Bitmap.createBitmap(
                        originalBitmap,
                        0,
                        0,
                        originalBitmap.getWidth(),
                        originalBitmap.getHeight(),
                        matrix,
                        true
                    )*/
                    binding.drawingView.setImageDrawable(it)
                    //clearCacheDir()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.coup()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun clearCacheDir() {
        val cacheDir = requireContext().cacheDir
        if (cacheDir.isDirectory) {
            val files = cacheDir.listFiles()
            if (files != null) {
                for (file in files) {
                    file.delete()
                }
            }
        }
    }

    companion object {
        fun newInstance() =
            EditorFragment()
    }
}