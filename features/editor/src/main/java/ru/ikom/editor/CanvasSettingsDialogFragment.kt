package ru.ikom.editor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ru.ikom.common.BaseSeekBarChangeListener
import ru.ikom.editor.databinding.FragmentCanvasSettingsDialogBinding

class CanvasSettingsDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentCanvasSettingsDialogBinding? = null
    private val binding: FragmentCanvasSettingsDialogBinding get() = _binding!!

    private var listener: CanvasSettingsListener? = null

    private var stroke = DrawingView.DEFAULT_STROKE_WIDTH
    private var color = Color.GREEN
    private var opacity = DrawingView.DEFAULT_ALPHA

    private val colors = mutableListOf<ColorUi>().apply {
        addAll(
            listOf(
                ColorUi(0, Color.GREEN),
                ColorUi(1, Color.BLACK),
                ColorUi(2, Color.WHITE),
                ColorUi(3, Color.BLUE),
                ColorUi(4, Color.CYAN),
                ColorUi(5, Color.DKGRAY),
                ColorUi(6, Color.GRAY),
                ColorUi(7, Color.LTGRAY),
                ColorUi(8, Color.MAGENTA),
                ColorUi(9, Color.RED),
                ColorUi(10, Color.YELLOW)
            )
        )
    }

    private val adapter = ColorsAdapter {
        val item = colors[it]
        if (item.selected) colors[it] = item.copy(selected = false)
        else {
            for (i in colors.indices) {
                if (colors[i] == item) colors[i] = colors[i].copy(selected = true)
                else colors[i] = colors[i].copy(selected = false)
            }
        }
        submitList()
    }

    private fun submitList() {
        adapter.submitList(colors.toList())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stroke = arguments?.getFloat(STROKE_KEY, DrawingView.DEFAULT_STROKE_WIDTH) ?: DrawingView.DEFAULT_STROKE_WIDTH
        color = arguments?.getInt(COLOR_KEY, Color.GREEN) ?: Color.GREEN
        opacity = arguments?.getInt(OPACITY_KEY, DrawingView.DEFAULT_ALPHA) ?: DrawingView.DEFAULT_ALPHA

        for (i in colors.indices) {
            val item = colors[i]
            if (item.color == color) {
                colors[i] = item.copy(selected = true)
                break
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentCanvasSettingsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.seekbarStroke.progress = (stroke - DrawingView.MIN_STROKE_WIDTH).toInt()
        binding.strokeCanvasValue.text = stroke.toInt().toString()
        binding.seekbarStroke.setOnSeekBarChangeListener(object : BaseSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                stroke = progress + DrawingView.MIN_STROKE_WIDTH
                binding.strokeCanvasValue.text = stroke.toInt().toString()
            }
        })

        binding.seekbarOpacity.progress = opacity
        binding.opacityCanvasValue.text = opacity.toString()
        binding.seekbarOpacity.setOnSeekBarChangeListener(object : BaseSeekBarChangeListener() {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                opacity = progress
                binding.opacityCanvasValue.text = opacity.toString()
            }
        })

        binding.rvColors.adapter = adapter
        adapter.submitList(colors.toList())

        binding.btnApply.setOnClickListener {
            listener?.successCanvasSettings(stroke, colors.first { it.selected }.color, opacity)
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        listener = null
    }

    fun setCanvasSettingsListener(listener: CanvasSettingsListener) {
        this.listener = listener
    }

    companion object {
        private const val STROKE_KEY = "STROKE_KEY"
        private const val COLOR_KEY = "COLOR_KEY"
        private const val OPACITY_KEY = "OPACITY_KEY"

        fun newInstance(stroke: Float, color: Int, opacity: Int) =
            CanvasSettingsDialogFragment().apply {
                arguments = Bundle().apply {
                    putFloat(STROKE_KEY, stroke)
                    putInt(COLOR_KEY, color)
                    putInt(OPACITY_KEY, opacity)
                }
            }
    }
}