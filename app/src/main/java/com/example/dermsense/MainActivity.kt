package com.example.dermsense

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Location
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestCameraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else Toast.makeText(this, "Kamera izni gerekli!", Toast.LENGTH_SHORT).show()
    }

    private val requestLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) fetchUVIndex()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish(); return
        }

        setContentView(R.layout.activity_main)

        val tvGreeting   = findViewById<TextView>(R.id.tvGreeting)
        val btnScan      = findViewById<Button>(R.id.btnScan)
        val bottomNav    = findViewById<BottomNavigationView>(R.id.bottomNav)
        val scansList    = findViewById<LinearLayout>(R.id.scansList)
        val tvNoScans    = findViewById<TextView>(R.id.tvNoScans)
        val headerLayout = findViewById<LinearLayout>(R.id.headerLayout)
        val scanCard     = findViewById<CardView>(R.id.scanCard)

        bottomNav.selectedItemId = R.id.nav_home

        val fadeIn  = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        headerLayout.startAnimation(fadeIn)
        scanCard.postDelayed({ scanCard.startAnimation(slideIn) }, 150)

        val uid = auth.currentUser!!.uid

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "Kullanici"
                val firstName = name.split(" ").firstOrNull() ?: name
                tvGreeting.text = "Merhaba, $firstName"

                // Profil avatarını güncelle
                val emoji = doc.getString("avatarEmoji") ?: "😊"
                findViewById<TextView>(R.id.tvProfileAvatar)?.text = emoji
            }

        db.collection("users").document(uid).collection("scans")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { docs ->
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
                            card.animate().alpha(1f).translationYBy(-20f).setDuration(300).start()
                        }, (index * 100 + 300).toLong())
                    }
                }
            }

        btnScan.setOnClickListener { checkCameraPermission() }
        checkLocationAndFetchUV()

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                R.id.nav_home -> true
                R.id.nav_scan -> { checkCameraPermission(); true }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    true
                }
                else -> false
            }
        }
    }

    private fun checkLocationAndFetchUV() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> fetchUVIndex()
            else -> requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchUVIndex() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val lat = location?.latitude ?: 41.0082
            val lon = location?.longitude ?: 28.9784
            fetchUVFromAPI(lat, lon)
        }
    }

    private fun fetchUVFromAPI(lat: Double, lon: Double) {
        Executors.newSingleThreadExecutor().execute {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&hourly=uv_index&forecast_days=1&timezone=auto"
                val response = URL(url).readText()
                val json = JSONObject(response)
                val uvArray = json.getJSONObject("hourly").getJSONArray("uv_index")
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val uvIndex = uvArray.getDouble(hour.coerceIn(0, 23))
                runOnUiThread { updateUVCard(uvIndex) }
            } catch (e: Exception) {
                runOnUiThread { updateUVCard(-1.0) }
            }
        }
    }

    private fun updateUVCard(uvIndex: Double) {
        val tvUV     = findViewById<TextView>(R.id.tvUVIndex)  ?: return
        val tvUVMsg  = findViewById<TextView>(R.id.tvUVMessage) ?: return
        val tvUVIcon = findViewById<TextView>(R.id.tvUVIcon)   ?: return
        val uvCard   = findViewById<CardView>(R.id.uvCard)     ?: return

        if (uvIndex < 0) {
            tvUV.text     = "UV: —"
            tvUVMsg.text  = "Konum alinamadi"
            tvUVIcon.text = "🌤️"
            return
        }

        val (icon, msg, color) = when {
            uvIndex <= 2  -> Triple("🟢", "UV dusuk — Normal aktivite guvenli",    "#5D8A5E")
            uvIndex <= 5  -> Triple("🟡", "UV orta — SPF 30+ kullan",              "#C4963A")
            uvIndex <= 7  -> Triple("🟠", "UV yuksek — SPF 50+ kullan, sapka tak", "#D4763A")
            uvIndex <= 10 -> Triple("🔴", "UV cok yuksek — Gunesten kacin!",       "#C05050")
            else          -> Triple("☢️", "UV asiri — Disari cikmayın!",           "#9E3A3A")
        }

        tvUVIcon.text = icon
        tvUV.text     = "UV Indeksi: ${String.format("%.1f", uvIndex)}"
        tvUVMsg.text  = msg
        tvUV.setTextColor(Color.parseColor(color))
        uvCard.animate().alpha(1f).setDuration(500).start()
    }

    private fun createScanCard(
        diagnosis: String, risk: String,
        confidence: Int, region: String, date: String
    ): View {
        val dp = resources.displayMetrics.density

        // Palete uygun renkler
        val riskColor = when (risk) {
            "YUKSEK", "YÜKSEK" -> "#C05050"   // yumusak kirmizi
            "ORTA"              -> "#C4963A"   // karamel sari
            else                -> "#5D8A5E"   // yumusak yesil
        }
        val riskBg = when (risk) {
            "YUKSEK", "YÜKSEK" -> "#FFF0F0"
            "ORTA"              -> "#FFF8EC"
            else                -> "#F0F7F0"
        }
        val riskEmoji = when (risk) {
            "YUKSEK", "YÜKSEK" -> "🔴"
            "ORTA"              -> "⚠️"
            else                -> "✅"
        }

        val card = CardView(this).apply {
            radius = 16f * dp
            cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (10 * dp).toInt()
            layoutParams = lp
        }

        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding((16*dp).toInt(), (14*dp).toInt(), (16*dp).toInt(), (14*dp).toInt())
        }

        val stripe = View(this).apply {
            val lp = LinearLayout.LayoutParams((3*dp).toInt(), (44*dp).toInt())
            lp.marginEnd = (14*dp).toInt()
            layoutParams = lp
            background = GradientDrawable().apply {
                cornerRadius = 4f * dp
                setColor(Color.parseColor(riskColor))
            }
        }

        val middle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        middle.addView(TextView(this).apply {
            text = diagnosis; textSize = 14f
            setTextColor(Color.parseColor("#2C2318"))
            setTypeface(null, Typeface.BOLD)
        })
        middle.addView(TextView(this).apply {
            text = "$date · $region"; textSize = 12f
            setTextColor(Color.parseColor("#B5A898"))
            setPadding(0, (3*dp).toInt(), 0, 0)
        })
        middle.addView(TextView(this).apply {
            text = "Guven: %$confidence"; textSize = 11f
            setTextColor(Color.parseColor("#C8BDB1"))
            setPadding(0, (2*dp).toInt(), 0, 0)
        })

        val tvRisk = TextView(this).apply {
            text = "$riskEmoji\n$risk"; textSize = 10f
            setTextColor(Color.parseColor(riskColor))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding((10*dp).toInt(), (8*dp).toInt(), (10*dp).toInt(), (8*dp).toInt())
            background = GradientDrawable().apply {
                cornerRadius = 10f * dp
                setColor(Color.parseColor(riskBg))
                setStroke((1*dp).toInt(), Color.parseColor(riskColor))
            }
        }

        inner.addView(stripe)
        inner.addView(middle)
        inner.addView(tvRisk)
        card.addView(inner)

        card.setOnClickListener {
            val intent = Intent(this, ScanDetailActivity::class.java).apply {
                putExtra("diagnosis", diagnosis)
                putExtra("risk", risk)
                putExtra("confidence", confidence)
                putExtra("region", region)
                putExtra("date", date)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        return card
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> requestCameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        startActivity(Intent(this, CameraActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}