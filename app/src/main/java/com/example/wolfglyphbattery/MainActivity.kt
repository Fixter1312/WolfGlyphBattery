package com.example.wolfglyphbattery

import android.os.BatteryManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvBattery = findViewById<TextView>(R.id.tvBattery)
        val ivWolf = findViewById<ImageView>(R.id.ivWolf)
        val btn = findViewById<Button>(R.id.btnSendGlyph)

        val percent = readBatteryPercent()
        tvBattery.text = "Bateria: $percent%"

        // Podgląd wilka w UI
        ivWolf.setImageBitmap(GlyphMatrixController.previewBitmap(scale = 10))

        // Wysyłka na Glyph – jasność = % baterii
        btn.setOnClickListener {
            val intensity = ((percent / 100f) * 255).toInt().coerceIn(0, 255)
            val frame = GlyphMatrixController.frameBytes(intensity)
            val ok = GlyphMatrixController.sendToGlyph(this, frame, 25, 25)
            btn.text = if (ok) "Wysłano ✅" else "Nie znaleziono API ❗"
        }
    }

    private fun readBatteryPercent(): Int {
        val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
        val v = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (v in 0..100) v else 0
    }
}
