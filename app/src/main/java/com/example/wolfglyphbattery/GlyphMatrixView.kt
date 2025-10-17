package com.example.wolfglyphbattery

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Warstwa pośrednia do AAR poprzez refleksję + diagnostyka.
 * Szukamy klas zarówno w nowej przestrzeni nazw, jak i w starej:
 *  - com.nothing.ketchum.GlyphMatrixApi / GlyphMatrix
 *  - com.nothing.api.glyph.GlyphMatrixApi / GlyphMatrix
 */
class GlyphMatrixView(private val context: Context) {

    data class Diag(
        val ok: Boolean,
        val lines: List<String>,
        val throwable: Throwable? = null
    )

    /** Sprawdza czy w ogóle mamy w AAR którąkolwiek z oczekiwanych klas. */
    fun isApiAvailable(): Diag {
        val lines = mutableListOf<String>()

        fun add(ok: Boolean, msg: String) {
            lines += (if (ok) "✓ " else "✗ ") + msg
        }

        val api = findClass(
            "com.nothing.ketchum.GlyphMatrixApi",
            "com.nothing.api.glyph.GlyphMatrixApi"
        )
        if (api == null) add(false, "Klasa nie znaleziona: GlyphMatrixApi")
        else add(true, "Znaleziono: ${api.name}")

        val matrix = findClass(
            "com.nothing.ketchum.GlyphMatrix",
            "com.nothing.api.glyph.GlyphMatrix"
        )
        if (matrix == null) add(false, "Klasa nie znaleziona: GlyphMatrix")
        else add(true, "Znaleziono: ${matrix.name}")

        val ok = api != null && matrix != null
        if (!ok) add(false, "Nie znaleziono kompatybilnego API w AAR.")
        return Diag(ok, lines)
    }

    /** Wysyła ramkę na Glyph – korzysta z metod znalezionych refleksją. */
    fun send(bytes: ByteArray): Diag {
        val tag = "WolfGlyph/Send"
        val lines = mutableListOf<String>()
        fun add(ok: Boolean, msg: String) {
            lines += (if (ok) "✓ " else "✗ ") + msg
        }

        try {
            val apiClass = findClass(
                "com.nothing.ketchum.GlyphMatrixApi",
                "com.nothing.api.glyph.GlyphMatrixApi"
            ) ?: run {
                add(false, "Brak klasy GlyphMatrixApi")
                return Diag(false, lines)
            }
            add(true, "API: ${apiClass.name}")

            val matrixClass = findClass(
                "com.nothing.ketchum.GlyphMatrix",
                "com.nothing.api.glyph.GlyphMatrix"
            ) ?: run {
                add(false, "Brak klasy GlyphMatrix")
                return Diag(false, lines)
            }
            add(true, "Model: ${matrixClass.name}")

            // konstruktor API z Context
            val apiCtor = apiClass.constructors.firstOrNull { c ->
                val p = c.parameterTypes
                p.size == 1 && Context::class.java.isAssignableFrom(p[0])
            } ?: run {
                add(false, "Brak konstruktora ${apiClass.simpleName}(Context)")
                return Diag(false, lines)
            }
            val api = apiCtor.newInstance(context)
            add(true, "Utworzono instancję API")

            // utworzenie GlyphMatrix z byte[]
            val fromMethod = runCatching {
                matrixClass.getMethod("from", ByteArray::class.java)
            }.getOrNull()
            val matrixObj: Any =
                if (fromMethod != null) {
                    fromMethod.invoke(null, bytes).also {
                        add(true, "Zbudowano GlyphMatrix via from(byte[])")
                    }
                } else {
                    val ctor = matrixClass.constructors.firstOrNull { c ->
                        val p = c.parameterTypes
                        p.size == 1 && p[0] == ByteArray::class.java
                    } ?: run {
                        add(false, "Brak factory from(byte[]) ani konstruktora (byte[])")
                        return Diag(false, lines)
                    }
                    ctor.newInstance(bytes).also {
                        add(true, "Zbudowano GlyphMatrix via konstruktor(byte[])")
                    }
                }

            // wywołanie: show/send/display(GlyphMatrix)
            val method = listOf("show", "send", "display")
                .firstNotNullOfOrNull { m ->
                    runCatching { apiClass.getMethod(m, matrixClass) }.getOrNull()
                } ?: run {
                    add(false, "Nie znaleziono show/send/display(GlyphMatrix) w ${apiClass.simpleName}")
                    return Diag(false, lines)
                }

            method.invoke(api, matrixObj)
            add(true, "Wywołano ${apiClass.simpleName}.${method.name}(GlyphMatrix)")

            Log.i(tag, lines.joinToString(" | "))
            return Diag(true, lines)
        } catch (t: Throwable) {
            Log.e(tag, "Błąd wysyłania", t)
            add(false, "Wyjątek: ${t::class.java.simpleName}: ${t.message}")
            Toast.makeText(context, "Błąd: ${t::class.java.simpleName}", Toast.LENGTH_SHORT).show()
            return Diag(false, lines, t)
        }
    }

    // --- utils ---
    private fun findClass(vararg names: String): Class<*>? =
        names.firstNotNullOfOrNull { n -> runCatching { Class.forName(n) }.getOrNull() }
}
