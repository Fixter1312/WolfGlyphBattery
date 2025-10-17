package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GlyphMatrixView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintWhite = Paint().apply { color = 0xFFFFFFFF.toInt() }
    private val paintBlack = Paint().apply { color = 0xFF000000.toInt() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cell = (minOf(width, height) / 25f)
        for (r in 0 until 25) {
            for (c in 0 until 25) {
                val p = if (WOLF[r][c] == 1) paintWhite else paintBlack
                canvas.drawRect(
                    c * cell, r * cell,
                    (c + 1) * cell, (r + 1) * cell, p
                )
            }
        }
    }

    // skrócona macierz do rysowania (wykorzystujemy tę samą co w kontrolerze)
    private val WOLF = GlyphMatrixController
        .run { javaClass.getDeclaredField("WOLF_25x25").apply { isAccessible = true }.get(this) as Array<IntArray> }
}
