package com.example.wolfglyphbattery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import java.lang.StringBuilder

object GlyphMatrixController {

    private const val W = 25
    private const val H = 25

    // ---- WILK 25x25 (1=biały, 0=czarny) ----
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

    /**
     * Próbuje wysłać ramkę do Glyph przez różne wersje Nothing SDK.
     * Zwraca parę (sukces, szczegółowyRaportDoUI)
     */
    fun sendToGlyphWithReport(context: Context, bytes25x25: ByteArray): Pair<Boolean, String> {
        val report = StringBuilder()
        report.appendLine("Diagnoza Glyph / ${Build.MANUFACTURER} ${Build.MODEL} (SDK ${Build.VERSION.SDK_INT})")
        report.appendLine("Próby klas/metod:")

        // Kandydaci klas (różne wydania SDK)
        val classes = listOf(
            "com.nothing.ketchum.GlyphMatrixManager",
            "com.nothing.ketchum.KetchumManager",
            "com.nothing.ketchum.GlyphManager",
            "com.nothing.glyph.matrix.GlyphMatrix",
            "com.nothing.glyph.matrix.GlyphMatrixManager",
            "com.nothing.ketchum.client.GlyphMatrixClient",
            "com.nothing.ketchum.sdk.GlyphMatrix"
        )

        // Kandydaci metod (różne nazwy w SDK)
        val methodsInt = listOf(
            "setAppMatrixFrame",
            "setMatrixFrame",
            "setMatrix",
            "updateMatrixFrame",
            "displayMatrix"
        )
        val methodsByte = listOf(
            "setAppMatrixFrame",
            "setMatrixFrame",
            "setMatrix",
            "updateMatrixFrame",
            "displayMatrix"
        )

        // int[] i byte[] do prób
        val ints = IntArray(bytes25x25.size) { i -> bytes25x25[i].toInt() and 0xFF }

        fun tryOnce(clsName: String): Boolean {
            return try {
                val cls = Class.forName(clsName)
                report.appendLine(" • Klasa OK: $clsName")

                // instancja: getInstance(Context) -> ctor(Context) -> static
                val instance: Any? = try {
                    val m = cls.getMethod("getInstance", Context::class.java)
                    m.invoke(null, context)
                } catch (_: Throwable) {
                    try {
                        val ctor = cls.getConstructor(Context::class.java)
                        ctor.newInstance(context)
                    } catch (_: Throwable) { null }
                }

                // register(package) jeśli istnieje
                try {
                    val reg = cls.getMethod("register", String::class.java)
                    reg.invoke(instance, context.packageName)
                    report.appendLine("   - register(package) OK")
                } catch (_: Throwable) { /* ignore */ }

                // czasem trzeba otworzyć sesję
                for (openName in listOf("openAppMatrix", "open", "openSession")) {
                    try {
                        val m = cls.getMethod(openName)
                        m.invoke(instance)
                        report.appendLine("   - $openName() OK")
                        break
                    } catch (_: Throwable) { /* ignore */ }
                }

                // PRÓBA: int[]
                for (name in methodsInt) {
                    try {
                        val m = cls.getMethod(name, IntArray::class.java)
                        m.invoke(instance, ints)
                        report.appendLine("   ✓ $name(IntArray) — DZIAŁA")
                        // close* (opcjonalnie)
                        for (closeName in listOf("closeAppMatrix", "close", "closeSession", "turnOff")) {
                            try { cls.getMethod(closeName).invoke(instance) } catch (_: Throwable) {}
                        }
                        return true
                    } catch (t: Throwable) {
                        report.appendLine("   × $name(IntArray): ${t.javaClass.simpleName}")
                    }
                }

                // PRÓBA: byte[] (niektóre buildy)
                for (name in methodsByte) {
                    try {
                        val m = cls.getMethod(name, ByteArray::class.java, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                        m.invoke(instance, bytes25x25, W, H)
                        report.appendLine("   ✓ $name(byte[],w,h) — DZIAŁA")
                        for (closeName in listOf("closeAppMatrix", "close", "closeSession", "turnOff")) {
                            try { cls.getMethod(closeName).invoke(instance) } catch (_: Throwable) {}
                        }
                        return true
                    } catch (t: Throwable) {
                        report.appendLine("   × $name(byte[],w,h): ${t.javaClass.simpleName}")
                    }
                }

                // jeżeli nic nie weszło
                false
            } catch (cnf: ClassNotFoundException) {
                report.appendLine(" • Brak klasy: $clsName")
                false
            }
        }

        for (c in classes) {
            if (tryOnce(c)) {
                return true to report.toString()
            }
        }

        // Fallback: broadcast (jeśli system coś wystawia, ale zwykle nie)
        try {
            val act = "com.nothing.ketchum.action.SET_MATRIX"
            val i = android.content.Intent(act).apply {
                putExtra("width", W)
                putExtra("height", H)
                putExtra("frame", bytes25x25)
                putExtra("brightness", 255)
                addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(i)
            report.appendLine(" • Wysłano broadcast: $act (brak potwierdzenia)")
        } catch (_: Throwable) { }

        return false to report.toString()
    }
}
