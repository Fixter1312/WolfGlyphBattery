package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

class GlyphMatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val whitePaint = Paint().apply { isAntiAlias = false }
    private val dimPaint = Paint().apply { isAntiAlias = false }
    private val gridPaint = Paint().apply { isAntiAlias = false; style = Paint.Style.STROKE; strokeWidth = 1f }
    private var percent: Int = 100

    // 25x25 wolf matrix; 1 = white pixel, 0 = black pixel
    private val wolfMatrix: Array<IntArray> = arrayOf(
        intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,0,1,0,1,1,1,0,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,1,0,1,1,1,1,1,0,1,1,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,1,1,0,1,1,0,0,1,1,1,1,1,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,1,1,0,1,1,0,0,0,1,1,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,1,1,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,1,1,1,0,1,1,1,0,0,0,0,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,1,1,1,0,0,0,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,1,1,0,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,1,1,0,0,0,1,0,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,1,0,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,1,1,0,1,1,0,0,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,1,0,0,0,0,1,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,1,0,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,1,1,0,0,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,1,1,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0),
        intArrayOf(0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0),
    )

    private var cellSize: Float = 0f
    private var gridSizePx: Float = 0f
    private val clipRect = Rect()

    init {
        whitePaint.color = 0xFFFFFFFF.toInt()
        dimPaint.color = 0xFF202020.toInt()
        gridPaint.color = 0xFF101010.toInt()
    }

    fun setPercent(p: Int) {
        percent = p.coerceIn(0, 100)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h)
        gridSizePx = size * 0.9f // margin
        cellSize = gridSizePx / 25f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val left = cx - gridSizePx / 2f
        val top = cy - gridSizePx / 2f

        // draw full wolf dimmed first (background silhouette)
        drawWolf(canvas, left, top, dimPaint, visibleRows = 25)

        // Clip visible band from bottom based on percent
        val rowsVisible = floor(25f * (percent / 100f)).toInt()
        if (rowsVisible > 0) {
            drawWolf(canvas, left, top, whitePaint, visibleRows = rowsVisible)
        }

        // Optional subtle grid
        // drawGrid(canvas, left, top)
    }

    private fun drawWolf(canvas: Canvas, left: Float, top: Float, paint: Paint, visibleRows: Int) {
        val bottomRowIndexExclusive = 25
        val startRow = 25 - visibleRows
        for (row in startRow until bottomRowIndexExclusive) {
            val y = top + row * cellSize
            for (col in 0 until 25) {
                if (wolfMatrix[row][col] == 1) {
                    val x = left + col * cellSize
                    canvas.drawRect(x, y, x + cellSize, y + cellSize, paint)
                }
            }
        }
    }

    @Suppress("unused")
    private fun drawGrid(canvas: Canvas, left: Float, top: Float) {
        for (i in 0..25) {
            val x = left + i * cellSize
            val y = top + i * cellSize
            canvas.drawLine(left, top + i * cellSize, left + 25 * cellSize, top + i * cellSize, gridPaint)
            canvas.drawLine(left + i * cellSize, top, left + i * cellSize, top + 25 * cellSize, gridPaint)
        }
    }
}