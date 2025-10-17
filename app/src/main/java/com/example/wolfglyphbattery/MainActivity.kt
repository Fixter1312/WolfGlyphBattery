package com.example.wolfglyphbattery

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvBattery = findViewById<TextView>(R.id.tvBattery)
        val ivWolf = findViewById<ImageView>(R.id.ivWolf)
        val btn = findViewById<Button>(R.id.btnSendGlyph)
        val btnDiag = findViewById<Button>(R.id.btnDiag)

        val percent = readBatteryPercent(this)
        tvBattery.text = "Bateria: $percent%"

        ivWolf.setImageBitmap(GlyphMatrixController.previewBitmap(scale = 10))

        btn.setOnClickListener {
            val intensity = ((percent / 100f) * 255).toInt().coerceIn(0, 255)
            val frame = GlyphMatrixController.frameBytes(intensity)
            val result = GlyphMatrixController.sendToGlyphWithReport(this, frame)
            btn.text = if (result.ok) "Wysłano ✅" else "Nie znaleziono API ❗"
            if (!result.ok) showReport(result.report)
        }

        btnDiag.setOnClickListener {
            val frame = GlyphMatrixController.frameBytes(255)
            val result = GlyphMatrixController.sendToGlyphWithReport(this, frame)
            showReport(result.report)
        }
    }

    private fun showReport(reportText: String) {
        val tv = TextView(this).apply {
            setTextIsSelectable(true)
            setPadding(32, 24, 32, 24)
            textSize = 13f
            text = reportText
        }
        val sc = ScrollView(this).apply { addView(tv) }
        AlertDialog.Builder(this)
            .setTitle("Raport diagnozy")
            .setView(sc)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun readBatteryPercent(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val v = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return if (v in 0..100) v else 0
    }
}
