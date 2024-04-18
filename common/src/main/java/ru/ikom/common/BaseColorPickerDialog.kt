package ru.ikom.common

import com.nvt.color.ColorPickerDialog

abstract class BaseColorPickerDialog : ColorPickerDialog.OnColorPickerListener {
    override fun onCancel(dialog: ColorPickerDialog?) {}

    override fun onOk(dialog: ColorPickerDialog?, color: Int) {}
}