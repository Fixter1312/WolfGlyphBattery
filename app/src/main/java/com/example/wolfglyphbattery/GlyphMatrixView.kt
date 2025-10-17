package com.example.wolfglyphbattery

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView

/**
 * Prosty widok z info o statusie API i tekstem do wysłania.
 * Jeśli masz już swój layout – możesz ten plik pominąć.
 */
class GlyphMatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val status = TextView(context).apply {
        textSize = 14f
    }

    init {
        addView(status)
    }

    fun updateStatus() {
        status.text = if (GlyphMatrixController.isApiAvailable()) {
            "API Nothing: OK"
        } else {
            "API Nothing: BRAK"
        }
    }
}
