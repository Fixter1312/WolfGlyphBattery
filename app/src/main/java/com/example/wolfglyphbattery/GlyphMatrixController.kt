package com.example.wolfglyphbattery

import android.os.Build
import android.util.Log
import com.nothing.glyph.matrix.Glyph
import com.nothing.glyph.matrix.GlyphMatrixFrame
import com.nothing.glyph.matrix.GlyphMatrixManager

object GlyphMatrixController {
    private var gm: GlyphMatrixManager? = null
    private var inited = false

    fun init(activity: android.app.Activity) {
        if (gm != null) return
        gm = GlyphMatrixManager(activity)
        gm?.init(object : GlyphMatrixManager.Callback {
            override fun onConnected() {
                inited = true
                // Phone (3) target. Value from SDK docs.
                gm?.register(Glyph.DEVICE_23112)
            }
            override fun onDisconnected() {
                inited = false
            }
        })
    }

    fun shutdown() {
        gm?.unInit()
        gm = null
        inited = false
    }

    fun pushFrame(colors: IntArray) {
        // Prefer setAppMatrixFrame if available (Nothing OS 20250801+), else fallback.
        try {
            val method = gm?.javaClass?.getMethod("setAppMatrixFrame", IntArray::class.java)
            if (method != null) {
                method.invoke(gm, colors)
                return
            }
        } catch (ignored: Throwable) { /* method not available */ }
        gm?.setMatrixFrame(colors)
    }

    fun clear() {
        try {
            val method = gm?.javaClass?.getMethod("closeAppMatrix")
            method?.invoke(gm)
        } catch (ignored: Throwable) { /* not critical */ }
    }
}