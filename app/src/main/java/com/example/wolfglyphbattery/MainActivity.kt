package com.example.wolfglyphbattery

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var controller: GlyphMatrixController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        controller = GlyphMatrixController(this)

        findViewById<Button>(R.id.sendButton).setOnClickListener {
            // tu wstaw swój obraz jako 20x11 pikseli (220 bajtów) – poniżej przykładowy wzór
            val bytes = DemoFrames.wolf220 // jeśli masz własne – podmień
            val diag = controller.send(bytes)
            showDiag(diag)
        }
    }

    private fun showDiag(diag: GlyphMatrixController.Diag) {
        val title = if (diag.ok) "Diagnoza: OK" else "Diagnoza: BŁĄD"
        val msg = buildString {
            diag.lines.forEach { appendLine(it) }
            diag.throwable?.let {
                appendLine()
                appendLine("Exception: ${it::class.java.name}")
                appendLine(it.message ?: "")
            }
        }
        Log.i("WolfGlyph/Diag", msg)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(msg.trim())
            .setPositiveButton("OK", null)
            .show()
    }
}

/**
 * Przykładowe dane 20x11 (Nothing Glyph 2.0 typowa macierz 20 kolumn x 11 wierszy).
 * Zamień na swoje bajty jeśli masz generator.
 */
object DemoFrames {
    // 220 bajtów – prosta szachownica jako placeholder
    val wolf220: ByteArray = ByteArray(220) { i ->
        val x = i % 20
        val y = i / 20
        if ((x + y) % 2 == 0) 1 else 0
    }
}
