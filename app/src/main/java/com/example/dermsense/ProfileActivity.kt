package com.example.dermsense

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val tvName         = findViewById<TextView>(R.id.tvName)
        val tvEmail        = findViewById<TextView>(R.id.tvEmail)
        val tvAge          = findViewById<TextView>(R.id.tvAge)
        val tvSkinType     = findViewById<TextView>(R.id.tvSkinType)
        val tvInitials     = findViewById<TextView>(R.id.tvInitials)
        val tvAllergyNotes = findViewById<TextView>(R.id.tvAllergyNotes)
        val tvReminder     = findViewById<TextView>(R.id.tvReminder)
        val btnLogout      = findViewById<Button>(R.id.btnLogout)
        val scansList      = findViewById<LinearLayout>(R.id.scansList)
        val tvNoScans      = findViewById<TextView>(R.id.tvNoScans)
        val pieChart       = findViewById<PieChartView>(R.id.pieChart)
        val tvLegendHigh   = findViewById<TextView>(R.id.tvLegendHigh)
        val tvLegendMid    = findViewById<TextView>(R.id.tvLegendMid)
        val tvLegendLow    = findViewById<TextView>(R.id.tvLegendLow)
        val avatarCard     = findViewById<CardView>(R.id.avatarCard)
        val infoCard       = findViewById<CardView>(R.id.infoCard)
        val pieCard        = findViewById<CardView>(R.id.pieCard)
        val btnEditProfile = findViewById<TextView>(R.id.btnEditProfile)
        val viewMode       = findViewById<LinearLayout>(R.id.viewMode)
        val editMode       = findViewById<LinearLayout>(R.id.editMode)
        val etNameEdit     = findViewById<EditText>(R.id.etNameEdit)
        val etAgeEdit      = findViewById<EditText>(R.id.etAgeEdit)
        val etAllergyEdit  = findViewById<EditText>(R.id.etAllergyEdit)
        val spinnerSkin    = findViewById<Spinner>(R.id.spinnerSkinEdit)
        val btnSaveEdit    = findViewById<Button>(R.id.btnSaveEdit)
        val btnCancelEdit  = findViewById<Button>(R.id.btnCancelEdit)

        val skinOptions = listOf("Tip I-II (Açık)", "Tip III-IV (Orta)", "Tip V-VI (Koyu)")
        spinnerSkin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, skinOptions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Animasyonlar
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        val scaleIn   = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        avatarCard.startAnimation(slideDown)
        tvInitials.postDelayed({ tvInitials.startAnimation(scaleIn) }, 300)
        infoCard.alpha = 0f
        infoCard.postDelayed({ infoCard.animate().alpha(1f).translationYBy(-30f).setDuration(400).start() }, 200)
        pieCard.alpha = 0f
        pieCard.postDelayed({ pieCard.animate().alpha(1f).translationYBy(-20f).setDuration(400).start() }, 350)

        // Bottom Nav
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_scan -> { startActivity(Intent(this, CameraActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_profile -> true
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                else -> false
            }
        }

        val uid = auth.currentUser?.uid ?: return

        // Kullanıcı bilgilerini yükle
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name     = doc.getString("name") ?: "İsimsiz"
                    val email    = doc.getString("email") ?: ""
                    val age      = doc.getString("age") ?: "-"
                    val skinType = doc.getString("skinType") ?: "Belirtilmedi"
                    val allergy  = doc.getString("allergyNotes") ?: ""
                    val reminder = doc.getString("reminder") ?: "Kapalı"

                    tvName.text         = name
                    tvEmail.text        = email
                    tvAge.text          = age
                    tvSkinType.text     = skinType
                    tvAllergyNotes.text = if (allergy.isEmpty()) "Belirtilmedi" else allergy
                    tvReminder.text     = reminder

                    etNameEdit.setText(name)
                    etAgeEdit.setText(age)
                    etAllergyEdit.setText(allergy)
                    spinnerSkin.setSelection(skinOptions.indexOf(skinType).coerceAtLeast(0))

                    val initials = name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2).joinToString("").uppercase()
                    tvInitials.text = initials
                    tvInitials.startAnimation(scaleIn)
                }
            }

        // Düzenle butonu
        btnEditProfile.setOnClickListener {
            viewMode.visibility = View.GONE
            editMode.visibility = View.VISIBLE
            btnEditProfile.visibility = View.GONE
        }

        // İptal
        btnCancelEdit.setOnClickListener {
            editMode.visibility = View.GONE
            viewMode.visibility = View.VISIBLE
            btnEditProfile.visibility = View.VISIBLE
        }

        // Kaydet — anında Firestore'a yaz ve görünümü güncelle
        btnSaveEdit.setOnClickListener {
            val name    = etNameEdit.text.toString().trim()
            val age     = etAgeEdit.text.toString().trim()
            val allergy = etAllergyEdit.text.toString().trim()
            val skin    = spinnerSkin.selectedItem.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Ad boş olamaz", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Önce UI'yı güncelle — hızlı his verir
            tvName.text         = name
            tvAge.text          = if (age.isEmpty()) "-" else age
            tvSkinType.text     = skin
            tvAllergyNotes.text = if (allergy.isEmpty()) "Belirtilmedi" else allergy
            val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
            tvInitials.text = initials

            // Edit modunu kapat
            editMode.visibility    = View.GONE
            viewMode.visibility    = View.VISIBLE
            btnEditProfile.visibility = View.VISIBLE

            // Arka planda Firestore'a kaydet
            db.collection("users").document(uid).update(
                mapOf("name" to name, "age" to age, "skinType" to skin, "allergyNotes" to allergy)
            ).addOnSuccessListener {
                Toast.makeText(this, "Profil güncellendi ✓", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Kayıt hatası: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Taramalar
        db.collection("users").document(uid).collection("scans")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { docs ->
                val high = docs.count { it.getString("risk") == "YÜKSEK" }
                val mid  = docs.count { it.getString("risk") == "ORTA" }
                val low  = docs.count { it.getString("risk") == "DÜŞÜK" }

                pieChart.setData(low, mid, high)
                tvLegendHigh.text = "🔴 Yüksek: $high"
                tvLegendMid.text  = "⚠️ Orta: $mid"
                tvLegendLow.text  = "✅ Düşük: $low"

                if (docs.isEmpty) {
                    tvNoScans.visibility = View.VISIBLE
                } else {
                    tvNoScans.visibility = View.GONE
                    docs.documents.forEachIndexed { index, doc ->
                        val card = createScanCard(
                            doc.getString("diagnosis") ?: "",
                            doc.getString("risk") ?: "",
                            doc.getLong("confidence")?.toInt() ?: 0,
                            doc.getString("region") ?: "",
                            doc.getString("date") ?: ""
                        )
                        card.alpha = 0f
                        scansList.addView(card)
                        card.postDelayed({
                            card.animate().alpha(1f).translationYBy(-15f).setDuration(250).start()
                        }, (index * 80 + 500).toLong())
                    }
                }
            }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun createScanCard(
        diagnosis: String, risk: String,
        confidence: Int, region: String, date: String
    ): View {
        val dp = resources.displayMetrics.density
        val riskColor = when (risk) { "YÜKSEK" -> "#FF4444"; "ORTA" -> "#FFA500"; else -> "#44BB44" }
        val riskEmoji = when (risk) { "YÜKSEK" -> "🔴"; "ORTA" -> "⚠️"; else -> "✅" }

        val card = CardView(this).apply {
            radius = 16f * dp; cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#1A1A32"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = (10 * dp).toInt(); layoutParams = lp
        }
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding((16*dp).toInt(), (14*dp).toInt(), (16*dp).toInt(), (14*dp).toInt())
        }
        val stripe = View(this).apply {
            val lp = LinearLayout.LayoutParams((3*dp).toInt(), (44*dp).toInt())
            lp.marginEnd = (14*dp).toInt(); layoutParams = lp
            background = GradientDrawable().apply { cornerRadius = 4f*dp; setColor(Color.parseColor(riskColor)) }
        }
        val middle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        middle.addView(TextView(this).apply { text = diagnosis; textSize = 14f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD) })
        middle.addView(TextView(this).apply { text = "$date · $region"; textSize = 12f; setTextColor(Color.parseColor("#7B7B9A")); setPadding(0, (3*dp).toInt(), 0, 0) })
        middle.addView(TextView(this).apply { text = "Güven: %$confidence"; textSize = 11f; setTextColor(Color.parseColor("#555577")); setPadding(0, (2*dp).toInt(), 0, 0) })

        val tvRisk = TextView(this).apply {
            text = "$riskEmoji\n$risk"; textSize = 10f
            setTextColor(Color.parseColor(riskColor)); setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
            setPadding((10*dp).toInt(), (8*dp).toInt(), (10*dp).toInt(), (8*dp).toInt())
            background = GradientDrawable().apply {
                cornerRadius = 10f*dp
                setColor(Color.argb(30, Color.red(Color.parseColor(riskColor)), Color.green(Color.parseColor(riskColor)), Color.blue(Color.parseColor(riskColor))))
                setStroke((1*dp).toInt(), Color.parseColor(riskColor))
            }
        }
        inner.addView(stripe); inner.addView(middle); inner.addView(tvRisk)
        card.addView(inner)
        card.setOnClickListener {
            val intent = Intent(this, ScanDetailActivity::class.java).apply {
                putExtra("diagnosis", diagnosis); putExtra("risk", risk)
                putExtra("confidence", confidence); putExtra("region", region); putExtra("date", date)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        return card
    }
}