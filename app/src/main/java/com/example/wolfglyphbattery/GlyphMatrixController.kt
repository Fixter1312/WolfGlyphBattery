package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import java.lang.StringBuilder
import kotlin.math.min

/**
 * Logika: budowa ramki 25x25 (0/255) na bazie przekazanej intensywności
 * + podgląd bitmapy + wysyłka do API Nothing metodą refleksji (żeby kompilacja
 * nie wymagała twardych importów z AAR).
 */
object GlyphMatrixController {

    // 25x25 wilk z Twojej wiadomości (1=białe, 0=czarne)
    private val WOLF_25x25: Array<IntArray> = arrayOf(
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

    /** Zwraca surową ramkę 25x25 (0..255) spłaszczoną do 625 bajtów. */
    fun frameBytes(intensityWhite: Int): ByteArray {
        val value = intensityWhite.coerceIn(0, 255)
        val out = ByteArray(25 * 25)
        var i = 0
        for (r in 0 until 25) {
            for (c in 0 until 25) {
                out[i++] = if (WOLF_25x25[r][c] == 1) value.toByte() else 0.toByte()
            }
        }
        return out
    }

    /** Bitmapa poglądowa na ekran (nie wpływa na Nothing Glyph). */
    fun previewBitmap(scale: Int = 12): Bitmap {
        val s = maxOf(1, scale)
        val bmp = Bitmap.createBitmap(25 * s, 25 * s, Bitmap.Config.ARGB_8888)
        for (r in 0 until 25) {
            for (c in 0 until 25) {
                val color = if (WOLF_25x25[r][c] == 1) Color.WHITE else Color.BLACK
                for (y in 0 until s) for (x in 0 until s) {
                    bmp.setPixel(c * s + x, r * s + y, color)
                }
            }
        }
        return bmp
    }

    data class SendResult(val ok: Boolean, val report: String)

    /**
     * Wysyłka przez refleksję – szukamy klas Nothing w runtime,
     * więc jeśli AAR nie zadziała w danym środowisku, nie wywali kompilacji.
     */
    fun sendToGlyphWithReport(context: Context, frame: ByteArray): SendResult {
        val report = StringBuilder()
        fun log(line: String) { report.append(line).append('\n') }

        try {
            // Próbujemy różnych popularnych nazw klas/metod w SDK Nothing
            val candidates = listOf(
                // nazwy przykładowe – jedna z nich powinna istnieć w Twoim AAR
                "com.nothing.ketchum.GlyphMatrixApi" to "sendFrame",
                "com.nothing.ketchum.GlyphMatrix" to "sendFrame",
                "com.nothing.api.glyph.GlyphMatrixApi" to "sendFrame"
            )

            for ((clsName, methodName) in candidates) {
                val cls = runCatching { Class.forName(clsName) }.getOrNull()
                if (cls == null) { log("⛔ Klasa nie znaleziona: $clsName"); continue }

                log("✅
