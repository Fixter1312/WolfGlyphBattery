package com.example.wolfglyphbattery

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.util.Log

object GlyphMatrixController {

    private const val TAG = "GlyphSender"

    // -------- WILK 25x25 --------
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

    fun frameBytes(intensity: Int): ByteArray {
        val clamped = intensity.coerceIn(0, 255)
        val out = ByteArray(W * H)
        var i = 0
        for (y in 0 until H) for (x in 0 until W) {
            out[i++] = if (wolf[y][x] == 1) clamped.toByte() else 0
        }
        return out
    }

    fun previewBitmap(scale: Int = 10): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        for (y in 0 until H) for (x in 0 until W) {
            bmp.setPixel(x, y, if (wolf[y][x] == 1) Color.WHITE else Color.BLACK)
        }
        return Bitmap.createScaledBitmap(bmp, W * scale, H * scale, false)
    }

    // -------- WYSYŁKA DO GLYPH --------
    fun sendToGlyph(context: Context, bytes25x25: ByteArray, width: Int = W, height: Int = H): Boolean {
        // 1) Najbardziej prawdopodobne nazwy (różne wersje SDK)
        val classCandidates = listOf(
            // Ketchum (częste w Nothing OS 2/3)
            "com.nothing.ketchum.GlyphMatrixManager",
            "com.nothing.ketchum.KetchumManager",
            "com.nothing.ketchum.GlyphManager",
            // Glyph Matrix (inne paczki)
            "com.nothing.glyph.matrix.GlyphMatrix",
            "com.nothing.glyph.matrix.GlyphMatrixManager",
            // Client/SDK aliasy
            "com.nothing.ketchum.client.GlyphMatrixClient",
            "com.nothing.ketchum.sdk.GlyphMatrix"
        )

        val methodCandidates = listOf(
            "setAppMatrixFrame",
            "setMatrixFrame",
            "setMatrix",
            "updateMatrixFrame",
            "displayMatrix"
        )

        // 1a) Szukamy klasy/metody i wołamy refleksją
        for (clsName in classCandidates) {
            try {
                val cls = Class.forName(clsName)
                val obj = try { cls.getConstructor(Context::class.java).newInstance(context) }
                          catch (_: Throwable) { null } // czasem są tylko metody statyczne

                for (m in cls.methods) {
                    if (m.name !in methodCandidates) continue
                    val p = m.parameterTypes
                    val ok = try {
                        when (p.size) {
                            3 -> { // (byte[], int, int)
                                m.invoke(obj, bytes25x25, width, height)
                                true
                            }
                            4 -> { // (byte[], int, int, int brightness)
                                m.invoke(obj, bytes25x25, width, height, 255)
                                true
                            }
                            else -> false
                        }
                    } catch (_: Throwable) { false }
                    if (ok) {
                        Log.i(TAG, "Used $clsName.${m.name}(${p.size} params)")
                        return true
                    }
                }
            } catch (_: Throwable) {
                // brak klasy – próbujemy dalej
            }
        }

        // 2) Fallback: Intent/broadcast (niektóre buildy Nothing wystawiają akcję systemową)
        // Uwaga: jeśli nie istnieje, po prostu nic się nie stanie – bez crasha.
        try {
            val actionCandidates = listOf(
                "com.nothing.ketchum.action.SET_MATRIX",
                "com.nothing.ketchum.ACTION_SET_MATRIX",
                "com.nothing.glyph.matrix.ACTION_SET_MATRIX"
            )
            for (act in actionCandidates) {
                val i = Intent(act).apply {
                    putExtra("width", width)
                    putExtra("height", height)
                    putExtra("frame", bytes25x25)
                    putExtra("brightness", 255)
                    addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                }
                context.sendBroadcast(i)
                Log.i(TAG, "Sent broadcast: $act")
                // Nie wiemy, czy ktoś obsłuży – ale to bezpieczny fallback
            }
        } catch (_: Throwable) { /* ignore */ }

        Log.w(TAG, "No matching API found.")
        return false
    }
}
