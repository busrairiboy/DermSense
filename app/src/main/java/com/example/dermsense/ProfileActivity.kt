package com.example.dermsense

import android.app.AlertDialog
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

    private val avatarEmojis = listOf(
        "😊", "😎", "🧑‍⚕️", "👩‍⚕️", "🦁", "🐺", "🦊", "🐻",
        "🌸", "🌿", "⭐", "🔬", "🩺", "💊", "🌙", "☀️"
    )

    private val avatarColors = listOf(
        "#E8823A", "#C4963A", "#5D8A5E", "#7A9EC4",
        "#C05050", "#8A7AC4", "#C4837A", "#6A9E9E"
    )

    private var selectedEmoji = "😊"
    private var selectedColor = "#E8823A"

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
        val btnCustomize   = findViewById<TextView>(R.id.btnChangeColor)

        val skinOptions = listOf("Tip I-II (Acik)", "Tip III-IV (Orta)", "Tip V-VI (Koyu)")
        spinnerSkin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, skinOptions).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        val scaleIn   = AnimationUtils.loadAnimation(this, R.anim.scale_in)
        avatarCard.startAnimation(slideDown)
        infoCard.alpha = 0f
        infoCard.postDelayed({ infoCard.animate().alpha(1f).translationYBy(-30f).setDuration(400).start() }, 200)
        pieCard.alpha = 0f
        pieCard.postDelayed({ pieCard.animate().alpha(1f).translationYBy(-20f).setDuration(400).start() }, 350)

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home     -> { startActivity(Intent(this, MainActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_scan     -> { startActivity(Intent(this, CameraActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_profile  -> true
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                else -> false
            }
        }

        val uid = auth.currentUser?.uid ?: return

        // Özelleştir butonu
        btnCustomize.setOnClickListener {
            showAvatarCustomizeDialog(tvInitials, avatarCard, uid)
        }

        // Avatar çift tıklama da aynı dialog
        tvInitials.setOnClickListener {
            showAvatarCustomizeDialog(tvInitials, avatarCard, uid)
        }

        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val name     = doc.getString("name") ?: "Isimsiz"
                val email    = doc.getString("email") ?: ""
                val age      = doc.getString("age") ?: "-"
                val skinType = doc.getString("skinType") ?: "Belirtilmedi"
                val allergy  = doc.getString("allergyNotes") ?: ""
                val reminder = doc.getString("reminder") ?: "Kapali"
                val emoji    = doc.getString("avatarEmoji") ?: "😊"
                val color    = doc.getString("avatarColor") ?: "#E8823A"

                selectedEmoji = emoji
                selectedColor = color

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

                applyAvatar(tvInitials, avatarCard, emoji, color)
                tvInitials.startAnimation(scaleIn)
            }
        }

        btnEditProfile.setOnClickListener {
            viewMode.visibility = View.GONE
            editMode.visibility = View.VISIBLE
            btnEditProfile.visibility = View.GONE
        }
        btnCancelEdit.setOnClickListener {
            editMode.visibility = View.GONE
            viewMode.visibility = View.VISIBLE
            btnEditProfile.visibility = View.VISIBLE
        }
        btnSaveEdit.setOnClickListener {
            val name    = etNameEdit.text.toString().trim()
            val age     = etAgeEdit.text.toString().trim()
            val allergy = etAllergyEdit.text.toString().trim()
            val skin    = spinnerSkin.selectedItem.toString()
            if (name.isEmpty()) { Toast.makeText(this, "Ad bos olamaz", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

            tvName.text         = name
            tvAge.text          = if (age.isEmpty()) "-" else age
            tvSkinType.text     = skin
            tvAllergyNotes.text = if (allergy.isEmpty()) "Belirtilmedi" else allergy

            editMode.visibility       = View.GONE
            viewMode.visibility       = View.VISIBLE
            btnEditProfile.visibility = View.VISIBLE

            db.collection("users").document(uid).update(
                mapOf("name" to name, "age" to age, "skinType" to skin, "allergyNotes" to allergy)
            ).addOnSuccessListener {
                Toast.makeText(this, "Profil guncellendi", Toast.LENGTH_SHORT).show()
            }
        }

        db.collection("users").document(uid).collection("scans")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20).get()
            .addOnSuccessListener { docs ->
                val high = docs.count { it.getString("risk")?.contains("YÜKSEK") == true || it.getString("risk")?.contains("YUKSEK") == true }
                val mid  = docs.count { it.getString("risk") == "ORTA" }
                val low  = docs.count { it.getString("risk")?.contains("DÜŞÜK") == true || it.getString("risk")?.contains("DUSUK") == true }

                pieChart.setData(low, mid, high)
                tvLegendHigh.text = "Yuksek: $high"
                tvLegendMid.text  = "Orta: $mid"
                tvLegendLow.text  = "Dusuk: $low"

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

    private fun showAvatarCustomizeDialog(
        tvInitials: TextView,
        avatarCard: CardView,
        uid: String
    ) {
        val dp = resources.displayMetrics.density

        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20*dp).toInt(), (24*dp).toInt(), (20*dp).toInt(), (16*dp).toInt())
        }

        // Başlık
        dialogView.addView(TextView(this).apply {
            text = "Avatarini Ozellestir"
            textSize = 18f
            setTextColor(Color.parseColor("#2C2318"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, (4*dp).toInt())
        })
        dialogView.addView(TextView(this).apply {
            text = "Bir emoji ve renk sec"
            textSize = 13f
            setTextColor(Color.parseColor("#B5A898"))
            setPadding(0, 0, 0, (20*dp).toInt())
        })

        // Önizleme
        val previewEmoji = TextView(this).apply {
            text = selectedEmoji
            textSize = 36f
            gravity = Gravity.CENTER
            val size = (80*dp).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
                it.bottomMargin = (20*dp).toInt()
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(selectedColor))
            }
        }
        dialogView.addView(previewEmoji)

        // Emoji seçimi başlığı
        dialogView.addView(TextView(this).apply {
            text = "EMOJI"
            textSize = 11f
            setTextColor(Color.parseColor("#B5A898"))
            letterSpacing = 0.15f
            setPadding(0, 0, 0, (10*dp).toInt())
        })

        // Emoji grid - 4 sütun
        val emojiGrid = GridLayout(this).apply {
            columnCount = 4
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (20*dp).toInt() }
        }

        avatarEmojis.forEach { emoji ->
            val btn = TextView(this).apply {
                text = emoji
                textSize = 28f
                gravity = Gravity.CENTER
                val size = (60*dp).toInt()
                val params = GridLayout.LayoutParams().apply {
                    width = size; height = size
                    setMargins((4*dp).toInt(), (4*dp).toInt(), (4*dp).toInt(), (4*dp).toInt())
                }
                layoutParams = params
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(if (emoji == selectedEmoji) Color.parseColor(selectedColor) else Color.parseColor("#F0EAE1"))
                }
                setOnClickListener {
                    selectedEmoji = emoji
                    previewEmoji.text = emoji
                    // Grid'i yenile
                    for (i in 0 until emojiGrid.childCount) {
                        val child = emojiGrid.getChildAt(i) as TextView
                        child.background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(if (child.text == selectedEmoji) Color.parseColor(selectedColor) else Color.parseColor("#F0EAE1"))
                        }
                    }
                }
            }
            emojiGrid.addView(btn)
        }
        dialogView.addView(emojiGrid)

        // Renk seçimi başlığı
        dialogView.addView(TextView(this).apply {
            text = "ARKA PLAN RENGI"
            textSize = 11f
            setTextColor(Color.parseColor("#B5A898"))
            letterSpacing = 0.15f
            setPadding(0, 0, 0, (10*dp).toInt())
        })

        // Renk satırı
        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (8*dp).toInt() }
        }

        avatarColors.forEach { color ->
            val circle = View(this).apply {
                val size = (40*dp).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.setMargins((6*dp).toInt(), 0, (6*dp).toInt(), 0)
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.parseColor(color))
                    if (color == selectedColor) setStroke((3*dp).toInt(), Color.parseColor("#2C2318"))
                }
                setOnClickListener {
                    selectedColor = color
                    previewEmoji.background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor(color))
                    }
                    // Renk seçimini güncelle
                    for (i in 0 until colorRow.childCount) {
                        val child = colorRow.getChildAt(i)
                        val c = avatarColors[i]
                        (child.background as GradientDrawable).apply {
                            if (c == selectedColor) setStroke((3*dp).toInt(), Color.parseColor("#2C2318"))
                            else setStroke(0, Color.TRANSPARENT)
                        }
                    }
                    // Emoji grid arka planlarını güncelle
                    for (i in 0 until emojiGrid.childCount) {
                        val child = emojiGrid.getChildAt(i) as TextView
                        child.background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(if (child.text == selectedEmoji) Color.parseColor(selectedColor) else Color.parseColor("#F0EAE1"))
                        }
                    }
                }
            }
            colorRow.addView(circle)
        }
        dialogView.addView(colorRow)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Kaydet") { _, _ ->
                applyAvatar(tvInitials, avatarCard, selectedEmoji, selectedColor)
                db.collection("users").document(uid).update(
                    mapOf("avatarEmoji" to selectedEmoji, "avatarColor" to selectedColor)
                )
                Toast.makeText(this, "Avatar guncellendi", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Iptal", null)
            .show()
    }

    private fun applyAvatar(tvInitials: TextView, avatarCard: CardView, emoji: String, color: String) {
        tvInitials.text = emoji
        tvInitials.textSize = 32f
        tvInitials.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(color))
        }

        val parsedColor = Color.parseColor(color)
        val lightColor = Color.argb(
            255,
            (Color.red(parsedColor) + (255 - Color.red(parsedColor)) * 0.75).toInt().coerceIn(0, 255),
            (Color.green(parsedColor) + (255 - Color.green(parsedColor)) * 0.75).toInt().coerceIn(0, 255),
            (Color.blue(parsedColor) + (255 - Color.blue(parsedColor)) * 0.75).toInt().coerceIn(0, 255)
        )
        avatarCard.setCardBackgroundColor(lightColor)
    }

    private fun createScanCard(
        diagnosis: String, risk: String,
        confidence: Int, region: String, date: String
    ): View {
        val dp = resources.displayMetrics.density
        val riskColor = when {
            risk.contains("YÜKSEK") || risk.contains("YUKSEK") -> "#C05050"
            risk == "ORTA" -> "#C4963A"
            else -> "#5D8A5E"
        }
        val riskBg = when {
            risk.contains("YÜKSEK") || risk.contains("YUKSEK") -> "#FFF0F0"
            risk == "ORTA" -> "#FFF8EC"
            else -> "#F0F7F0"
        }
        val riskEmoji = when {
            risk.contains("YÜKSEK") || risk.contains("YUKSEK") -> "🔴"
            risk == "ORTA" -> "⚠️"
            else -> "✅"
        }
        val riskLabel = when {
            risk.contains("YÜKSEK") || risk.contains("YUKSEK") -> "YUKSEK"
            risk == "ORTA" -> "ORTA"
            else -> "DUSUK"
        }
        val card = CardView(this).apply {
            radius = 16f * dp; cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
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
        middle.addView(TextView(this).apply { text = diagnosis; textSize = 14f; setTextColor(Color.parseColor("#2C2318")); setTypeface(null, Typeface.BOLD) })
        middle.addView(TextView(this).apply { text = "$date · $region"; textSize = 12f; setTextColor(Color.parseColor("#B5A898")); setPadding(0, (3*dp).toInt(), 0, 0) })
        middle.addView(TextView(this).apply { text = "Guven: %$confidence"; textSize = 11f; setTextColor(Color.parseColor("#C8BDB1")); setPadding(0, (2*dp).toInt(), 0, 0) })
        val tvRisk = TextView(this).apply {
            text = "$riskEmoji\n$riskLabel"; textSize = 10f
            setTextColor(Color.parseColor(riskColor)); setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
            setPadding((10*dp).toInt(), (8*dp).toInt(), (10*dp).toInt(), (8*dp).toInt())
            background = GradientDrawable().apply {
                cornerRadius = 10f*dp; setColor(Color.parseColor(riskBg))
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
            startActivity(intent); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        return card
    }
}