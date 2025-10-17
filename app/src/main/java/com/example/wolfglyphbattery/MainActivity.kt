package com.example.wolfglyphbattery

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prosty układ programistycznie, żeby nie grzebać w XML.
        val btn = Button(this).apply { text = "Wyślij na Glyph" }
        setContentView(btn)

        btn.setOnClickListener {
            if (!GlyphMatrixController.isApiAvailable()) {
                Toast.makeText(this, "Nie znaleziono kompatybilnego API w AAR", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val ok = GlyphMatrixController.sendText(this, "WOLF")
            Toast.makeText(
                this,
                if (ok) "Wysłane na Glyphy" else "Błąd wysyłania",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
