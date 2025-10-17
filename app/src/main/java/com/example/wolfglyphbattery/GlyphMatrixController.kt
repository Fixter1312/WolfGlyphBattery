package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import java.lang.StringBuilder

object GlyphMatrixController {

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

    fun frameBytes(intensityWhite: Int): ByteArray {
        val value = intensityWhite.coerceIn(0, 255)
        val out = ByteArray(25 * 25)
        var i = 0
        for (r in 0 until 25) for (c in 0 until 25) {
            out[i++] = if (WOLF_25x25[r][c] == 1) value.toByte() else 0.toByte()
        }
        return out
    }

    fun previewBitmap(scale: Int = 12): Bitmap {
        val s = if (scale < 1) 1 else scale
        val bmp = Bitmap.createBitmap(25 * s, 25 * s, Bitmap.Config.ARGB_8888)
        for (r in 0 until 25) for (c in 0 until 25) {
            val color = if (WOLF_25x25[r][c] == 1) Color.WHITE else Color.BLACK
            for (y in 0 until s) for (x in 0 until s) {
                bmp.setPixel(c * s + x, r * s + y, color)
            }
        }
        return bmp
    }

    data class SendResult(val ok: Boolean, val report: String)

    fun sendToGlyphWithReport(context: Context, frame: ByteArray): SendResult {
        val report = StringBuilder()
        fun log(m: String) { report.append(m).append('\n') }

        val candidates = listOf(
            "com.nothing.ketchum.GlyphMatrixApi" to "sendFrame",
            "com.nothing.ketchum.GlyphMatrix" to "sendFrame",
            "com.nothing.api.glyph.GlyphMatrixApi" to "sendFrame"
        )

        try {
            for ((clsName, methodName) in candidates) {
                val cls = runCatching { Class.forName(clsName) }.getOrNull()
                if (cls == null) { log("⛔ Klasa nie znaleziona: $clsName"); continue }

                log("✅ Znaleziono klasę: $clsName")

                val methodCtx = cls.methods.firstOrNull {
                    it.name == methodName &&
                            it.parameterTypes.size == 2 &&
                            Context::class.java.isAssignableFrom(it.parameterTypes[0]) &&
                            it.parameterTypes[1] == ByteArray::class.java
                }
                val methodPlain = cls.methods.firstOrNull {
                    it.name == methodName &&
                            it.parameterTypes.size == 1 &&
                            it.parameterTypes[0] == ByteArray::class.java
                }

                if (methodCtx != null) {
                    val inst = cls.getDeclaredConstructor().newInstance()
                    methodCtx.invoke(inst, context, frame)
                    log("🎉 Wysłano przez $clsName.$methodName(Context, ByteArray)")
                    return SendResult(true, report.toString())
                }
                if (methodPlain != null) {
                    val inst = cls.getDeclaredConstructor().newInstance()
                    methodPlain.invoke(inst, frame)
                    log("🎉 Wysłano przez $clsName.$methodName(ByteArray)")
                    return SendResult(true, report.toString())
                }

                log("⚠ Nie znaleziono metody $methodName w $clsName")
            }
            log("❌ Nie znaleziono kompatybilnego API w AAR.")
            return SendResult(false, report.toString())
        } catch (t: Throwable) {
            log("❌ Wyjątek: ${t.javaClass.name}: ${t.message}")
            return SendResult(false, report.toString())
        }
    }
}
