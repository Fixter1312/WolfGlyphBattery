package com.example.wolfglyphbattery

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class GlyphMatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        setImageBitmap(GlyphMatrixController.previewBitmap(scale = 10))
        adjustViewBounds = true
    }

    fun refreshPreview(scale: Int = 10) {
        setImageBitmap(GlyphMatrixController.previewBitmap(scale))
    }
}
