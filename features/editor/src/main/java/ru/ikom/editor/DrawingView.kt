package ru.ikom.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ImageView(context, attrs, defStyleAttr) {

    private var path = Path()

    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    private var paint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    private val paths = mutableListOf<Path>()
    private val colors = mutableListOf<Int>()
    var color = Color.GREEN

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (index in 0 until paths.size) {
            paint.color = colors[index]
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
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            MotionEvent.ACTION_UP -> path.lineTo(x, y)
        }

        invalidate()

        return true
    }

    fun save(completed: (Uri) -> Unit) {
        if (paths.isNotEmpty()) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap?.let {
                canvas = Canvas(it)
                canvas?.let {
                    draw(it)
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val file = File(imagesDir, "PictureEditor-${UUID.randomUUID()}.jpg")
                    val stream = FileOutputStream(file)
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                    MediaScannerConnection.scanFile(
                        this@DrawingView.context,
                        arrayOf(file.absolutePath),
                        null,
                        null
                    )
                    completed(
                        FileProvider.getUriForFile(
                            this@DrawingView.context,
                            this@DrawingView.context.packageName + ".provider",
                            file
                        )
                    )
                }
            }
        }
    }

    fun clear() {
        if (paths.isNotEmpty()) {
            path.reset()
            paths.clear()
            colors.clear()
            invalidate()
        }
        setImageDrawable(null)
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            paths.removeLast()
            colors.removeLast()
            invalidate()
        }
    }
}