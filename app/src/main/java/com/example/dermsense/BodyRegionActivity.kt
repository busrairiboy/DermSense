package com.example.dermsense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class BodyRegionActivity : AppCompatActivity() {

    private var selectedRegion = ""
    private var selectedCard: CardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_region)

        val imageUri    = intent.getStringExtra("image_uri") ?: return
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val tvSelected  = findViewById<TextView>(R.id.tvSelected)

        val regions = listOf(
            R.id.cardHead  to "Kafa / Yüz / Boyun",
            R.id.cardChest to "Göğüs",
            R.id.cardArm   to "Kol / Ön kol",
            R.id.cardHand  to "El / Bilek",
            R.id.cardBack  to "Sırt",
            R.id.cardLeg   to "Bacak",
            R.id.cardFoot  to "Ayak"
        )

        regions.forEach { (cardId, regionName) ->
            val card = findViewById<CardView>(cardId)
            card.setOnClickListener {
                selectedCard?.setCardBackgroundColor(0x1A1A32.or(-0x1000000))
                card.setCardBackgroundColor(0x2A2A5E.or(-0x1000000))
                selectedCard   = card
                selectedRegion = regionName
                tvSelected.text = "Seçilen bölge: $regionName"
                btnContinue.isEnabled = true
                btnContinue.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xC9A84C.or(-0x1000000))
            }
        }

        btnContinue.setOnClickListener {
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("image_uri", imageUri)
                putExtra("body_region", selectedRegion)
            }
            startActivity(intent)
        }
    }
}