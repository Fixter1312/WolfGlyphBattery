package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

object GlyphMatrixController {

    private const val TAG = "GlyphSender"

    // ---- Wymiary matrycy ----
    private const val W = 25
    private const val H = 25

    // ---- Wilk 25x25 (1=biały, 0=czarny) ----
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

    /** Z macierzy tworzymy bajty 0..255 (jasność) */
    fun frameBytes(intensity: Int): ByteArray {
        val clamped = intensity.coerceIn(0, 255)
        val out = ByteArray(W * H)
        var i = 0
        for (y in 0 until H) for (x in 0 until W) {
            out[i++] = if (wolf[y][x] == 1) clamped.toByte() else 0
        }
        return out
    }

    /** Podgląd bitmapy wilka w UI (powiększenie scale) */
    fun previewBitmap(scale: Int = 10): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        for (y in 0 until H) for (x in 0 until W) {
            bmp.setPixel(x, y, if (wolf[y][x] == 1) Color.WHITE else Color.BLACK)
        }
        return Bitmap.createScaledBitmap(bmp, W * scale, H * scale, false)
    }

    // ---- Wysyłka do Nothing Glyph SDK (Twoje AAR: int[] bez width/height) ----
    fun sendToGlyph(context: Context, bytes25x25: ByteArray, width: Int = W, height: Int = H): Boolean {
        try {
            // Klasa managera z AAR
            val cls = Class.forName("com.nothing.ketchum.GlyphMatrixManager")

            // Instancja: preferuj getInstance(Context), w razie czego konstruktor(Context)
            val instance = try {
                val getInst = cls.getMethod("getInstance", Context::class.java)
                getInst.invoke(null, context)
            } catch (_: Throwable) {
                val ctor = cls.getConstructor(Context::class.java)
                ctor.newInstance(context)
            }

            // Rejestracja aplikacji (jeśli metoda istnieje)
            try {
                val register = cls.getMethod("register", String::class.java)
                register.invoke(instance, context.packageName)
            } catch (_: Throwable) { /* brak = OK */ }

            // Konwersja do int[] (SDK tego wymaga)
            val ints = IntArray(width * height)
            var i = 0
            for (b in bytes25x25) ints[i++] = (b.toInt() and 0xFF)

            // Próba wywołań: setAppMatrixFrame(int[]) → setMatrixFrame(int[])
            fun tryCall(name: String): Boolean = try {
                val m = cls.getMethod(name, IntArray::class.java)
                m.invoke(instance, ints)
                true
            } catch (_: Throwable) { false }

            if (tryCall("setAppMatrixFrame") || tryCall("setMatrixFrame")) {
                Log.i(TAG, "Glyph OK: int[] via GlyphMatrixManager")
                return true
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Glyph error: ${t.message}")
        }
        Log.w(TAG, "No matching API")
        return false
    }
}
