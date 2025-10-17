package com.example.wolfglyphbattery

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.lang.reflect.Method

/**
 * Wysyłka do Nothing Glyphs wykonana w 100% refleksją pod AAR,
 * który zawiera klasy z pakietu com.nothing.ketchum.*:
 *
 *  - GlyphMatrixManager (API menedżer)
 *  - GlyphMatrixObject  (model danych) + Builder
 *
 * Kod sam wykrywa dostępne metody (from(byte[]), konstruktory,
 * Builder#set…(byte[]), build(), a potem w managerze metody
 * typu send/show/display/set/update(*GlyphMatrixObject*)).
 */
class GlyphMatrixController(private val context: Context) {

    data class Diag(
        val ok: Boolean,
        val lines: List<String>,
        val throwable: Throwable? = null
    )

    fun send(bytes: ByteArray): Diag {
        val tag = "WolfGlyph/Send"
        val lines = mutableListOf<String>()
        fun add(ok: Boolean, msg: String) { lines += (if (ok) "✓ " else "✗ ") + msg }

        try {
            // 1) Znajdź klasy w Twoim AAR
            val managerClass = runCatching {
                Class.forName("com.nothing.ketchum.GlyphMatrixManager")
            }.getOrNull() ?: run {
                add(false, "Brak klasy: com.nothing.ketchum.GlyphMatrixManager")
                return Diag(false, lines)
            }
            add(true, "Znaleziono ${managerClass.name}")

            val objClass = runCatching {
                Class.forName("com.nothing.ketchum.GlyphMatrixObject")
            }.getOrNull() ?: run {
                add(false, "Brak klasy: com.nothing.ketchum.GlyphMatrixObject")
                return Diag(false, lines)
            }
            add(true, "Znaleziono ${objClass.name}")

            // 2) Utwórz instancję managera (szukamy konstruktora z Context)
            val managerCtor = managerClass.constructors.firstOrNull { ctor ->
                val p = ctor.parameterTypes
                p.size == 1 && android.content.Context::class.java.isAssignableFrom(p[0])
            } ?: run {
                add(false, "Brak konstruktora ${managerClass.simpleName}(Context)")
                return Diag(false, lines)
            }
            val manager = managerCtor.newInstance(context)
            add(true, "Utworzono ${managerClass.simpleName}")

            // 3) Zbuduj GlyphMatrixObject z byte[]
            val matrixObj: Any = run {
                // a) statyczne factory: from(byte[])
                val fromMethod: Method? = runCatching {
                    objClass.getMethod("from", ByteArray::class.java)
                }.getOrNull()

                if (fromMethod != null) {
                    add(true, "Używam ${objClass.simpleName}.from(byte[])")
                    fromMethod.invoke(null, bytes)
                } else {
                    // b) konstruktor (byte[])
                    val byteCtor = objClass.constructors.firstOrNull { ctor ->
                        val p = ctor.parameterTypes
                        p.size == 1 && p[0] == ByteArray::class.java
                    }
                    if (byteCtor != null) {
                        add(true, "Używam konstruktora ${objClass.simpleName}(byte[])")
                        byteCtor.newInstance(bytes)
                    } else {
                        // c) Builder: GlyphMatrixObject$Builder
                        val builderClass = runCatching {
                            Class.forName("${objClass.name}\$Builder")
                        }.getOrNull() ?: run {
                            add(false, "Brak factory/konstruktora i Buildera dla ${objClass.simpleName}")
                            return@run null
                        }
                        val builder = builderClass.getDeclaredConstructor().newInstance()
                        // Szukamy dowolnego settera, który przyjmuje byte[]
                        val setter = builderClass.methods.firstOrNull { m ->
                            m.parameterTypes.size == 1 && m.parameterTypes[0] == ByteArray::class.java
                        } ?: run {
                            add(false, "Builder nie ma metody przyjmującej byte[]")
                            return@run null
                        }
                        setter.invoke(builder, bytes)
                        val build = runCatching { builderClass.getMethod("build") }.getOrNull()
                            ?: run {
                                add(false, "Builder nie ma metody build()")
                                return@run null
                            }
                        add(true, "Zbudowano przez ${builderClass.simpleName}.${setter.name}(byte[])->build()")
                        build.invoke(builder)
                    }
                } ?: return Diag(false, lines)
            }

            // 4) Wyślij – znajdź metodę w managerze przyjmującą GlyphMatrixObject
            val candidateNames = listOf("send", "show", "display", "set", "update", "play")
            val sendMethod = candidateNames
                .firstNotNullOfOrNull { name ->
                    runCatching { managerClass.getMethod(name, objClass) }.getOrNull()
                } ?: run {
                    // Gdy brak „ładnie nazwanych” metod – bierzemy dowolną publiczną,
                    // która przyjmuje dokładnie jeden argument typu GlyphMatrixObject
                    managerClass.methods.firstOrNull { m ->
                        m.parameterTypes.size == 1 && m.parameterTypes[0] == objClass
                    } ?: run {
                        add(false, "Nie znalazłem metody *(GlyphMatrixObject) w ${managerClass.simpleName}")
                        return Diag(false, lines)
                    }
                }

            sendMethod.invoke(manager, matrixObj)
            add(true, "Wywołano ${managerClass.simpleName}.${sendMethod.name}(${objClass.simpleName})")

            Log.i(tag, lines.joinToString(" | "))
            return Diag(true, lines)
        } catch (t: Throwable) {
            add(false, "Wyjątek: ${t::class.java.simpleName}: ${t.message}")
            Log.e("WolfGlyph/Send", "Błąd wysyłania", t)
            Toast.makeText(context, "Błąd: ${t::class.java.simpleName}", Toast.LENGTH_SHORT).show()
            return Diag(false, lines, t)
        }
    }
}
