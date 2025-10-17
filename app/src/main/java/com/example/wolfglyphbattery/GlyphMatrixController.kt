package com.example.wolfglyphbattery

import android.content.Context
import android.util.Log

/**
 * Kontroler, który:
 * 1) Sprawdza czy w AAR jest Nothingowe API (GlyphMatrixManager itp.)
 * 2) Buduje ramkę tekstową przez builder i wysyła ją — wszystko przez reflection,
 *    żeby działało z zaciemnionymi nazwami metod.
 */
object GlyphMatrixController {
    private const val TAG = "GlyphMatrixController"

    // Pełne nazwy klas, które naprawdę są w Twoim AAR:
    private const val CLASS_MANAGER = "com.nothing.ketchum.GlyphMatrixManager"
    private const val CLASS_GLYPH_FRAME_BUILDER = "com.nothing.ketchum.GlyphFrame\$Builder"
    private const val CLASS_GLYPH_MATRIX_FRAME = "com.nothing.ketchum.GlyphMatrixFrame"

    fun isApiAvailable(): Boolean {
        return try {
            Class.forName(CLASS_MANAGER)
            Class.forName(CLASS_GLYPH_FRAME_BUILDER)
            Class.forName(CLASS_GLYPH_MATRIX_FRAME)
            true
        } catch (t: Throwable) {
            Log.w(TAG, "API not available: ${t.message}")
            false
        }
    }

    /**
     * Wysyła prosty tekst na Glyphy (po stronie telefonu Nothing),
     * korzystając z klas z AAR (reflection).
     */
    fun sendText(context: Context, text: String): Boolean {
        return try {
            // 1) Manager
            val mgrClass = Class.forName(CLASS_MANAGER)

            // Szukamy statycznego getInstance(Context) lub innej metody,
            // która zwraca GlyphMatrixManager i przyjmuje Context.
            val getInstance = mgrClass.methods.firstOrNull { m ->
                m.name.contains("getInstance", ignoreCase = true) &&
                        m.parameterTypes.size == 1 &&
                        m.parameterTypes[0].name == Context::class.java.name &&
                        m.returnType.name == CLASS_MANAGER
            } ?: mgrClass.methods.firstOrNull { m ->
                m.parameterTypes.size == 1 &&
                        m.parameterTypes[0].name == Context::class.java.name &&
                        m.returnType.name == CLASS_MANAGER
            } ?: throw IllegalStateException("Nie znaleziono metody getInstance(Context) w GlyphMatrixManager")

            val manager = getInstance.invoke(null, context)

            // 2) Zdobądź builder klatki (bez parametrów)
            val builderAny = run {
                // preferowana nazwa:
                val m = mgrClass.methods.firstOrNull { it.name.contains("getGlyphFrameBuilder", true) && it.parameterTypes.isEmpty() }
                if (m != null) m.invoke(manager)
                else {
                    // fallback: dowolna publiczna metoda bez parametrów zwracająca …GlyphFrame$Builder
                    val m2 = mgrClass.methods.firstOrNull {
                        it.parameterTypes.isEmpty() && it.returnType.name == CLASS_GLYPH_FRAME_BUILDER
                    } ?: throw IllegalStateException("Nie znaleziono buildera GlyphFrame\$Builder")
                    m2.invoke(manager)
                }
            }

            val builderClass = Class.forName(CLASS_GLYPH_FRAME_BUILDER)

            // 3) Ustaw tekst (różne wersje SDK mają różne nazwy setterów — próbujemy kilka)
            val setTextMethod = listOf(
                "setGlyphMatrixText", // spotykana w SDK
                "setText",            // fallback
            ).firstNotNullOfOrNull { name ->
                builderClass.methods.firstOrNull { m ->
                    m.name == name && m.parameterTypes.size == 1 && m.parameterTypes[0] == String::class.java
                }
            } ?: builderClass.methods.firstOrNull { m ->
                // ostateczny fallback: dowolna metoda przyjmująca String i zwracająca Builder
                m.parameterTypes.size == 1 &&
                        m.parameterTypes[0] == String::class.java &&
                        m.returnType.name == CLASS_GLYPH_FRAME_BUILDER
            } ?: throw IllegalStateException("Nie znaleziono settera tekstu w GlyphFrame.Builder")

            setTextMethod.invoke(builderAny, text)

            // 4) Zbuduj finalny obiekt ramki (build())
            val buildMethod = builderClass.methods.firstOrNull { m ->
                m.name == "build" && m.parameterTypes.isEmpty()
            } ?: throw IllegalStateException("Nie znaleziono metody build() w GlyphFrame.Builder")

            val frameAny = buildMethod.invoke(builderAny)

            // 5) Wyślij na Glyphy — znajdź metodę managera, która przyjmuje GlyphMatrixFrame
            val matrixFrameClass = Class.forName(CLASS_GLYPH_MATRIX_FRAME)
            val sendMethod = mgrClass.methods.firstOrNull { m ->
                m.parameterTypes.size == 1 && m.parameterTypes[0].name == CLASS_GLYPH_MATRIX_FRAME
            } ?: throw IllegalStateException("Nie znaleziono metody wysyłającej GlyphMatrixFrame w GlyphMatrixManager")

            val result = sendMethod.invoke(manager, frameAny)
            // czasem metoda zwraca void, czasem boolean — traktujemy oba jako sukces
            (result as? Boolean) ?: true
        } catch (t: Throwable) {
            Log.e(TAG, "sendText() failed", t)
            false
        }
    }
}
