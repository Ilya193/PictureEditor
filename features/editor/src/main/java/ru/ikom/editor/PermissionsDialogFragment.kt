package ru.ikom.editor

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.ikom.editor.databinding.FragmentPermissionsDialogBinding


class PermissionsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPermissionsDialogBinding? = null
    private val binding: FragmentPermissionsDialogBinding get() = _binding!!

    private var permissionsListener: PermissionsListener? = null

    private var mode = ""

    fun permissionsListener(listener: PermissionsListener) {
        this.permissionsListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mode = arguments?.getString(PERMISSIONS_MODE, "") ?: ""
    }

    private val permission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                callback()
                dismiss()
            }
            else {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    requestPermission()
                else cannotBeRequested()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentPermissionsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener {
            permissionsListener?.cancelPermissions()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(true)
            }
            else {
                callback()
                dismiss()
            }
        }
    }

    private fun requestPermission(first: Boolean = false) {
        binding.containerRequest.visibility = View.VISIBLE
        binding.containerBan.visibility = View.GONE
        binding.btnAccess.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                permission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (first) permission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun cannotBeRequested() {
        binding.containerRequest.visibility = View.GONE
        binding.containerBan.visibility = View.VISIBLE
        binding.btnOpenSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            })
        }
    }

    private fun callback() {
        if (mode == PERMISSIONS_MODE_SAVE) permissionsListener?.successModeSave()
        else permissionsListener?.successModeShare()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        permissionsListener = null
    }

    companion object {
        const val PERMISSIONS_MODE = "PERMISSIONS_MODE"
        const val PERMISSIONS_MODE_SAVE = "PERMISSIONS_MODE_SAVE"
        const val PERMISSIONS_MODE_SHARE = "PERMISSIONS_MODE_SHARE"

        fun newInstance(mode: String) =
            PermissionsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(PERMISSIONS_MODE, mode)
                }
            }
    }
}