package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

/**
 * W tym pliku masz wszystko „techniczne”:
 *  - macierz wilka 25x25 (1=biały, 0=czarny),
 *  - generowanie bitmapy do podglądu w UI,
 *  - wysyłkę ramki 25x25 do Nothing Glyph SDK (przez refleksję).
 */
object GlyphMatrixController {

    // --- 1) Wilk 25x25 ---
    private const val W = 25
    private const val H = 25

    private val wolf: Array<IntArray> = arrayOf(
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
        intArrayOf(0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0)
    )

    /** Z macierzy 25×25 robimy jednowymiarową tablicę bajtów (0…255) */
    fun frameBytes(intensity: Int): ByteArray {
        val clamped = intensity.coerceIn(0, 255)
        val out = ByteArray(W * H)
        var i = 0
        for (y in 0 until H) for (x in 0 until W) {
            out[i++] = if (wolf[y][x] == 1) clamped.toByte() else 0
        }
        return out
    }

    /** Tworzy bitmapę podglądu wilka, powiększoną x[scale]. */
    fun previewBitmap(scale: Int = 10): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        for (y in 0 until H) {
            for (x in 0 until W) {
                bmp.setPixel(x, y, if (wolf[y][x] == 1) Color.WHITE else Color.BLACK)
            }
        }
        return Bitmap.createScaledBitmap(bmp, W * scale, H * scale, false)
    }

    // --- 2) Wysyłka do Nothing Glyph SDK (refleksja, żeby się zawsze kompilowało) ---

    private const val TAG = "GlyphSender"

    fun sendToGlyph(context: Context, bytes25x25: ByteArray, width: Int = W, height: Int = H): Boolean {
        val attempts: List<() -> Boolean> = listOf(
            { tryKetchumManager(context, bytes25x25, width, height, appScoped = true) },
            { tryKetchumManager(context, bytes25x25, width, height, appScoped = false) },
            { tryGlyphMatrix(context, bytes25x25, width, height) }
        )
        for (a in attempts) {
            try { if (a()) return true } catch (t: Throwable) { Log.w(TAG, t.message ?: "attempt fail") }
        }
        Log.w(TAG, "No SDK API matched. App continues.")
        return false
    }

    private fun tryKetchumManager(ctx: Context, frame: ByteArray, w: Int, h: Int, appScoped: Boolean): Boolean {
        val cls = Class.forName("com.nothing.ketchum.GlyphMatrixManager")
        val obj = cls.getConstructor(Context::class.java).newInstance(ctx)
        val methodName = if (appScoped) "setAppMatrixFrame" else "setMatrixFrame"
        val m = cls.methods.firstOrNull { it.name == methodName && it.parameterTypes.size in 3..4 }
            ?: return false
        return try {
            when (m.parameterTypes.size) {
                3 -> { m.invoke(obj, frame, w, h); true }
                4 -> { m.invoke(obj, frame, w, h, 255); true }
                else -> false
            }
        } catch (_: Throwable) { false }
    }

    private fun tryGlyphMatrix(ctx: Context, frame: ByteArray, w: Int, h: Int): Boolean {
        val cls = Class.forName("com.nothing.glyph.matrix.GlyphMatrix")
        val obj = cls.getConstructor(Context::class.java).newInstance(ctx)
        val m = cls.methods.firstOrNull { it.name == "setMatrixFrame" && it.parameterTypes.size in 3..4 }
            ?: return false
        return try {
            when (m.parameterTypes.size) {
                3 -> { m.invoke(obj, frame, w, h); true }
                4 -> { m.invoke(obj, frame, w, h, 255); true }
                else -> false
            }
        } catch (_: Throwable) { false }
    }
}
