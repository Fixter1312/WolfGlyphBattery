package com.example.wolfglyphbattery

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.lang.reflect.Method

/**
 * Prosty wrapper do AAR przez refleksję + diagnostyka.
 *
 * Oczekiwane klasy:
 *  - com.nothing.ketchum.GlyphMatrixApi   (nowsze API)
 *  - com.nothing.ketchum.GlyphMatrix
 *  - com.nothing.api.glyph.GlyphMatrixApi (starsze nazwy – fallback)
 *  - com.nothing.api.glyph.GlyphMatrix
 */
class GlyphMatrixController(private val context: Context) {

    data class Diag(
        val ok: Boolean,
        val lines: List<String>,
        val throwable: Throwable? = null
    )

    /** Szybkie sprawdzenie dostępności API – ZAMIENIA isAppAvalible/isAppAvailable */
    fun isApiAvailable(): Diag {
        val lines = mutableListOf<String>()

        fun add(ok: Boolean, msg: String) {
            lines += (if (ok) "✓ " else "✗ ") + msg
        }

        return try {
            // Spróbuj klas API (nowa -> stara nazwa)
            val apiClass = runCatching {
                Class.forName("com.nothing.ketchum.GlyphMatrixApi")
            }.getOrElse {
                runCatching {
                    Class.forName("com.nothing.api.glyph.GlyphMatrixApi")
                }.getOrNull()
            } ?: run {
                add(false, "Brak klasy GlyphMatrixApi (ketchum/api.glyph)")
                return Diag(false, lines)
            }
            add(true, "Znaleziono ${apiClass.name}")

            // Spróbuj klas modelu
            val matrixClass = runCatching {
                Class.forName("com.nothing.ketchum.GlyphMatrix")
            }.getOrElse {
                runCatching {
                    Class.forName("com.nothing.api.glyph.GlyphMatrix")
                }.getOrNull()
            } ?: run {
                add(false, "Brak klasy GlyphMatrix")
                return Diag(false, lines)
            }
            add(true, "Znaleziono ${matrixClass.name}")

            // Sprawdź czy API ma konstruktor (Context)
            val apiCtor = apiClass.constructors.firstOrNull { ctor ->
                val p = ctor.parameterTypes
                p.size == 1 && Context::class.java.isAssignableFrom(p[0])
            } ?: run {
                add(false, "Brak konstruktora ${apiClass.simpleName}(Context)")
                return Diag(false, lines)
            }
            add(true, "Konstruktor ${apiClass.simpleName}(Context) OK")

            // Sprawdź czy jest metoda show/send/display(GlyphMatrix)
            val send = listOf("show", "send", "display")
                .firstNotNullOfOrNull { m ->
                    runCatching { apiClass.getMethod(m, matrixClass) }.getOrNull()
                } ?: run {
                    add(false, "Brak metody show/send/display(GlyphMatrix)")
                    return Diag(false, lines)
                }
            add(true, "Znaleziono metodę ${send.name}(GlyphMatrix)")

            Diag(true, lines)
        } catch (t: Throwable) {
            lines += "✗ Wyjątek: ${t::class.java.simpleName}: ${t.message}"
            Diag(false, lines, t)
        }
    }

    /** Wysyła ramkę na Glyph – używa tych samych odbić co powyżej. */
    fun send(bytes: ByteArray): Diag {
        val tag = "WolfGlyph/Send"
        val lines = mutableListOf<String>()

        fun add(ok: Boolean, msg: String) {
            lines += (if (ok) "✓ " else "✗ ") + msg
        }

        try {
            // 1) znajdź klasę API (nowa lub stara nazwa)
            val apiClass = runCatching {
                Class.forName("com.nothing.ketchum.GlyphMatrixApi")
            }.getOrElse {
                runCatching {
                    Class.forName("com.nothing.api.glyph.GlyphMatrixApi")
                }.getOrNull()
            } ?: run {
                add(false, "Nie znaleziono klasy GlyphMatrixApi (ani com.nothing.ketchum.*, ani com.nothing.api.glyph.*)")
                return Diag(false, lines)
            }
            add(true, "Znaleziono ${apiClass.name}")

            // 2) znajdź klasę modelu
            val matrixClass = runCatching {
                Class.forName("com.nothing.ketchum.GlyphMatrix")
            }.getOrElse {
                runCatching {
                    Class.forName("com.nothing.api.glyph.GlyphMatrix")
                }.getOrNull()
            } ?: run {
                add(false, "Nie znaleziono klasy GlyphMatrix")
                return Diag(false, lines)
            }
            add(true, "Znaleziono ${matrixClass.name}")

            // 3) utwórz instancję API (konstruktor z Context)
            val apiCtor = apiClass.constructors.firstOrNull { ctor ->
                val p = ctor.parameterTypes
                p.size == 1 && Context::class.java.isAssignableFrom(p[0])
            } ?: run {
                add(false, "Brak konstruktora ${apiClass.simpleName}(Context)")
                return Diag(false, lines)
            }
            val api = apiCtor.newInstance(context)
            add(true, "Utworzono ${apiClass.simpleName}")

            // 4) utwórz obiekt GlyphMatrix z tablicy bajtów
            val fromMethod: Method? = runCatching {
                matrixClass.getMethod("from", ByteArray::class.java)
            }.getOrNull()

            val matrixObj: Any = if (fromMethod != null) {
                fromMethod.invoke(null, bytes).also {
                    add(true, "Zbudowano przez ${matrixClass.simpleName}.from(byte[])")
                }
            } else {
                val ctor = matrixClass.constructors.firstOrNull { ctor ->
                    val p = ctor.parameterTypes
                    p.size == 1 && p[0] == ByteArray::class.java
                } ?: run {
                    add(false, "Brak factory 'from(byte[])' ani konstruktora (byte[]) w ${matrixClass.simpleName}")
                    return Diag(false, lines)
                }
                ctor.newInstance(bytes).also {
                    add(true, "Zbudowano przez konstruktor (byte[])")
                }
            }

            // 5) wyślij
            val apiMethods = listOf("show", "send", "display")
            val sendMethod = apiMethods
                .firstNotNullOfOrNull { m ->
                    runCatching { apiClass.getMethod(m, matrixClass) }.getOrNull()
                } ?: run {
                    add(false, "Nie znaleziono metody show/send/display(GlyphMatrix)")
                    return Diag(false, lines)
                }

            sendMethod.invoke(api, matrixObj)
            add(true, "Wywołano ${apiClass.simpleName}.${sendMethod.name}(GlyphMatrix)")

            Log.i(tag, lines.joinToString("  |  "))
            return Diag(true, lines)
        } catch (t: Throwable) {
            Log.e(tag, "Błąd wysyłania", t)
            add(false, "Wyjątek: ${t::class.java.simpleName}: ${t.message}")
            Toast.makeText(context, "Błąd: ${t::class.java.simpleName}", Toast.LENGTH_SHORT).show()
            return Diag(false, lines, t)
        }
    }
}
