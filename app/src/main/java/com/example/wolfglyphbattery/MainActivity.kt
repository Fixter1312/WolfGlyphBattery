package com.example.wolfglyphbattery

import android.os.BatteryManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wolfglyphbattery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var controller: GlyphMatrixController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = GlyphMatrixController(this)

        // prosty odczyt poziomu baterii do TextView (opcjonalnie)
        updateBatteryLabel()

        // ====== WŁAŚCIWY PRZYCISK Z LAYOUTU ======
        binding.btnSendGlyph.setOnClickListener {
            // 1) sprawdzenie dostępności API
            val check = controller.isApiAvailable()
            if (!check.ok) {
                showDiag("Diagnoza API", check.lines)
                return@setOnClickListener
            }

            // 2) zbuduj ramkę do wysłania (placeholder – możesz podmienić)
            val frame = demoFrameBytes()

            // 3) wyślij
            val result = controller.send(frame)
            if (result.ok) {
                Toast.makeText(this, "Wysłano na Glyph", Toast.LENGTH_SHORT).show()
            } else {
                showDiag("Błąd wysyłania", result.lines)
            }
        }

        // ====== DRUGI PRZYCISK Z LAYOUTU ======
        binding.btnDiag.setOnClickListener {
            val check = controller.isApiAvailable()
            showDiag("Diagnoza API", check.lines)
        }
    }

    private fun updateBatteryLabel() {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        binding.tvBattery.text = "Bateria: ${level}%"
    }

    private fun showDiag(title: String, lines: List<String>) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(lines.joinToString("\n"))
            .setPositiveButton("OK", null)
            .show()
    }

    /**
     * Prosta testowa ramka 16x16 (256 bajtów) – na przemian 0/255.
     * Jeśli Twoje AAR wymaga konkretnego rozmiaru, podmień na właściwy.
     */
    private fun demoFrameBytes(): ByteArray =
        ByteArray(256) { i -> if (i % 2 == 0) 0xFF.toByte() else 0x00 }
}
