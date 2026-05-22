package com.example.dermsense

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ScanDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_detail)

        val diagnosis  = intent.getStringExtra("diagnosis") ?: ""
        val risk       = intent.getStringExtra("risk") ?: ""
        val confidence = intent.getIntExtra("confidence", 0)
        val region     = intent.getStringExtra("region") ?: ""
        val date       = intent.getStringExtra("date") ?: ""

        val tvDiagnosis  = findViewById<TextView>(R.id.tvDiagnosis)
        val tvDate       = findViewById<TextView>(R.id.tvDate)
        val tvRegion     = findViewById<TextView>(R.id.tvRegion)
        val tvRisk       = findViewById<TextView>(R.id.tvRisk)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence)
        val progressBar  = findViewById<ProgressBar>(R.id.progressBar)
        val tvA          = findViewById<TextView>(R.id.tvA)
        val tvB          = findViewById<TextView>(R.id.tvB)
        val tvC          = findViewById<TextView>(R.id.tvC)
        val tvD          = findViewById<TextView>(R.id.tvD)
        val tvAStatus    = findViewById<TextView>(R.id.tvAStatus)
        val tvBStatus    = findViewById<TextView>(R.id.tvBStatus)
        val tvCStatus    = findViewById<TextView>(R.id.tvCStatus)
        val tvDStatus    = findViewById<TextView>(R.id.tvDStatus)

        val abcdeScores = mapOf(
            "Melanom" to mapOf(
                "A" to Pair("Asimetrik yapı", true),
                "B" to Pair("Düzensiz kenar", true),
                "C" to Pair("Çok renkli", true),
                "D" to Pair("~6mm+ tahmin", true)
            ),
            "Melanositik Nevüs" to mapOf(
                "A" to Pair("Simetrik yapı", false),
                "B" to Pair("Düzgün kenar", false),
                "C" to Pair("Tek renkli", false),
                "D" to Pair("~5mm tahmin", false)
            ),
            "Bazal Hücreli Karsinom" to mapOf(
                "A" to Pair("Asimetrik yapı", true),
                "B" to Pair("Düzensiz kenar", true),
                "C" to Pair("İnci renkli", false),
                "D" to Pair("~5mm+ tahmin", true)
            ),
            "Aktinik Keratoz" to mapOf(
                "A" to Pair("Hafif asimetri", true),
                "B" to Pair("Belirsiz kenar", true),
                "C" to Pair("Kırmızımsı ton", false),
                "D" to Pair("~4mm tahmin", false)
            ),
            "Benign Keratoz" to mapOf(
                "A" to Pair("Hafif asimetri", false),
                "B" to Pair("Düzgün kenar", false),
                "C" to Pair("Kahverengi ton", false),
                "D" to Pair("~5mm tahmin", false)
            ),
            "Dermatofibrom" to mapOf(
                "A" to Pair("Simetrik yapı", false),
                "B" to Pair("Düzgün kenar", false),
                "C" to Pair("Tek renkli", false),
                "D" to Pair("~3mm tahmin", false)
            ),
            "Vasküler Lezyon" to mapOf(
                "A" to Pair("Simetrik yapı", false),
                "B" to Pair("Belirgin kenar", false),
                "C" to Pair("Kırmızı ton", false),
                "D" to Pair("~4mm tahmin", false)
            )
        )

        val riskColor = when {
            risk.contains("YÜKSEK") -> "#C05050"
            risk == "ORTA"          -> "#C4963A"
            else                    -> "#5D8A5E"
        }
        val riskEmoji = when {
            risk.contains("YÜKSEK") -> "🔴"
            risk == "ORTA"          -> "⚠️"
            else                    -> "✅"
        }

        tvDiagnosis.text  = diagnosis
        tvDate.text       = "📅 $date"
        tvRegion.text     = "📍 $region"
        tvRisk.text       = "$riskEmoji $risk RİSK"
        tvRisk.setTextColor(Color.parseColor(riskColor))
        tvConfidence.text = "Model Güveni: %$confidence"
        progressBar.progress = confidence

        val abcde = abcdeScores[diagnosis] ?: abcdeScores["Melanositik Nevüs"]!!
        tvA.text = abcde["A"]!!.first
        tvB.text = abcde["B"]!!.first
        tvC.text = abcde["C"]!!.first
        tvD.text = abcde["D"]!!.first

        fun setStatus(tv: TextView, danger: Boolean) {
            if (danger) {
                tv.text = "⚠️ Dikkat"
                tv.setTextColor(Color.parseColor("#C05050"))
            } else {
                tv.text = "✓ Normal"
                tv.setTextColor(Color.parseColor("#5D8A5E"))
            }
        }

        setStatus(tvAStatus, abcde["A"]!!.second)
        setStatus(tvBStatus, abcde["B"]!!.second)
        setStatus(tvCStatus, abcde["C"]!!.second)
        setStatus(tvDStatus, abcde["D"]!!.second)
    }
}