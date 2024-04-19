package ru.ikom.editor

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nvt.color.ColorPickerDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.ikom.common.BaseColorPickerDialog
import ru.ikom.common.BaseSeekBarChangeListener
import ru.ikom.editor.databinding.FragmentEditorBinding
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


class EditorFragment : Fragment(), PermissionsListener, CanvasSettingsListener {
    private var _binding: FragmentEditorBinding? = null
    private val binding: FragmentEditorBinding get() = _binding!!

    private val viewModel: EditorViewModel by viewModel()

    private var imageUri = ""

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                showImage(uri, 100)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUri = arguments?.getString(IMAGE_URI_KEY, "") ?: ""
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
        settingListeners()
        if (imageUri.isNotEmpty()) {
            showImage(Uri.parse(imageUri), 1000)
            imageUri = ""
        }
    }

    private fun showImage(uri: Uri, delay: Long) {
        binding.info.setTextColor(Color.GREEN)
        binding.info.text = getString(R.string.load_picture)
        binding.info.alpha = 1f
        binding.drawingView.clear()
        defaultLayoutParams()
        viewLifecycleOwner.lifecycleScope.launch {
            delay(delay)
            val file = getFileFromUri(uri)
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            if (bitmap.height < bitmap.width) {
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
            } else binding.drawingView.setImageBitmap(bitmap)
            binding.drawingView.drawable?.let { dr ->
                val imageWidth = dr.intrinsicWidth
                val imageHeight = dr.intrinsicHeight

                val imageViewWidth = binding.drawingView.width
                val imageViewHeight = binding.drawingView.height

                val displayedImageWidth: Int
                val displayedImageHeight: Int

                if (imageWidth * imageViewHeight > imageViewWidth * imageHeight) {
                    displayedImageWidth = imageViewWidth
                    displayedImageHeight = imageHeight * imageViewWidth / imageWidth
                } else {
                    displayedImageWidth = imageWidth * imageViewHeight / imageHeight
                    displayedImageHeight = imageViewHeight
                }

                binding.drawingView.layoutParams.width = displayedImageWidth
                binding.drawingView.layoutParams.height = displayedImageHeight
            }
            binding.info.alpha = 0f
        }
    }

    override fun onResume() {
        super.onResume()
        val windowInsetsController =
            WindowCompat.getInsetsController(
                requireActivity().window,
                requireActivity().window.decorView
            )
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun settingListeners() {
        binding.icSave.setOnClickListener {
            if (!checkPermissions()) openPermissionsDialog(PermissionsDialogFragment.PERMISSIONS_MODE_SAVE)
            else savePicture()
        }

        binding.icPalette.setOnClickListener {
            val dialog = CanvasSettingsDialogFragment.newInstance(
                binding.drawingView.strokeWidth,
                binding.drawingView.color,
                binding.drawingView.opacity
            )
            dialog.setCanvasSettingsListener(this)
            dialog.show(parentFragmentManager, null)
        }

        binding.icUndo.setOnClickListener {
            binding.drawingView.undo()
        }

        binding.icDelete.setOnClickListener {
            binding.drawingView.clear()
            defaultLayoutParams()
        }

        binding.icShare.setOnClickListener {
            if (!checkPermissions()) openPermissionsDialog(PermissionsDialogFragment.PERMISSIONS_MODE_SHARE)
            else savePictureWithShare()
        }

        binding.icFolder.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun savePicture() {
        binding.drawingView.save(completed = {
            showInfo(getString(R.string.picture_save), Color.GREEN)
        }, error = {
            showInfo(getString(R.string.draw_something), Color.RED)
        })
    }

    private fun savePictureWithShare() {
        binding.drawingView.save(completed = {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, it)
                type = "image/jpeg"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }, error = {
            showInfo(getString(R.string.draw_something), Color.RED)
        })
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        else true
    }

    private fun showInfo(msg: String, color: Int) {
        binding.info.setTextColor(color)
        binding.info.text = msg
        binding.info.animate().alpha(1f).setDuration(250).withEndAction {
            binding.info.animate().alpha(0f).setDuration(1500)
        }.start()
    }

    private fun defaultLayoutParams() {
        val layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0)
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.bottomToTop = binding.containerActions.id
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        binding.drawingView.layoutParams = layoutParams
    }

    override fun successModeSave() {
        savePicture()
    }

    override fun successModeShare() {
        savePictureWithShare()
    }

    override fun successCanvasSettings(stroke: Float, color: Int, opacity: Int) {
        binding.drawingView.strokeWidth = stroke
        binding.drawingView.color = color
        binding.drawingView.opacity = opacity
    }

    override fun cancelPermissions() {
        showInfo(getString(R.string.permission_not_granted), Color.RED)
    }

    private fun openPermissionsDialog(mode: String) {
        val dialog = PermissionsDialogFragment.newInstance(mode)
        dialog.permissionsListener(this)
        dialog.show(parentFragmentManager, null)
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
        val file = File(requireContext().cacheDir, "PictureEditor-${UUID.randomUUID()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    companion object {
        private const val IMAGE_URI_KEY = "IMAGE_URI_KEY"
        fun newInstance(imageUri: String? = null) =
            EditorFragment().apply {
                arguments = Bundle().apply {
                    putString(IMAGE_URI_KEY, imageUri)
                }
            }
    }
}