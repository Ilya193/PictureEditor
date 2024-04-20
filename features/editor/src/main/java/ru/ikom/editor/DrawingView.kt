package ru.ikom.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File
import java.util.UUID

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var path = Path()

    private var paint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = DEFAULT_STROKE_WIDTH
        alpha = DEFAULT_ALPHA
    }

    private val paths = mutableListOf<Path>()
    private val colors = mutableListOf<Int>()
    private val strokesWidth = mutableListOf<Float>()
    private val opacities = mutableListOf<Int>()

    var color = Color.GREEN
    var strokeWidth = DEFAULT_STROKE_WIDTH
    var opacity = DEFAULT_ALPHA

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (index in 0 until paths.size) {
            paint.color = colors[index]
            paint.strokeWidth = strokesWidth[index]
            paint.alpha = opacities[index]
            canvas.drawPath(paths[index], paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path = Path()
                colors.add(color)
                paths.add(path)
                strokesWidth.add(strokeWidth)
                opacities.add(opacity)
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> path.lineTo(x, y)
        }

        invalidate()
        return true
    }

    fun save(completed: (Uri) -> Unit, error: () -> Unit) {
        if (paths.isNotEmpty()) {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                Canvas(bitmap).also { canvas ->
                    draw(canvas)
                    val imagesDir = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    else Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val file = File(imagesDir, "PictureEditor-${UUID.randomUUID()}.jpg")
                    file.outputStream().use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    }
                    MediaScannerConnection.scanFile(
                        this@DrawingView.context,
                        arrayOf(file.absolutePath),
                        null,
                        null
                    )
                    completed(
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) file.toUri()
                        else FileProvider.getUriForFile(
                            this@DrawingView.context,
                            this@DrawingView.context.packageName + ".provider",
                            file
                        )
                    )
                }
            }
        } else error()
    }

    fun clear() {
        if (paths.isNotEmpty()) {
            path.reset()
            paths.clear()
            colors.clear()
            strokesWidth.clear()
            opacities.clear()
            invalidate()
        }
        setImageDrawable(null)
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            paths.removeLast()
            colors.removeLast()
            strokesWidth.removeLast()
            opacities.removeLast()
            invalidate()
        }
    }

    companion object {
        const val DEFAULT_STROKE_WIDTH = 12f
        const val MIN_STROKE_WIDTH = 10f
        const val DEFAULT_ALPHA = 255
    }
}