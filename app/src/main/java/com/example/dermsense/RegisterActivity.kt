package com.example.dermsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        val etFirstName   = findViewById<EditText>(R.id.etFirstName)
        val etLastName    = findViewById<EditText>(R.id.etLastName)
        val etEmail       = findViewById<EditText>(R.id.etEmail)
        val etPassword    = findViewById<EditText>(R.id.etPassword)
        val etAge         = findViewById<EditText>(R.id.etAge)
        val spinnerGender   = findViewById<Spinner>(R.id.rgGender)
        val spinnerSkinType = findViewById<Spinner>(R.id.spinnerSkinType)
        val tvSkinTypeHint  = findViewById<TextView>(R.id.tvSkinTypeHint)
        val btnReg          = findViewById<Button>(R.id.btnRegister)
        val btnLogin      = findViewById<TextView>(R.id.btnLogin)
        val tabLogin      = findViewById<TextView>(R.id.tabLogin)
        val progress      = findViewById<ProgressBar>(R.id.progressBar)
        val tvError       = findViewById<TextView>(R.id.tvError)

        val bars = listOf(
            findViewById<View>(R.id.bar1),
            findViewById<View>(R.id.bar2),
            findViewById<View>(R.id.bar3),
            findViewById<View>(R.id.bar4)
        )
        val criteriaLength  = findViewById<TextView>(R.id.criteriaLength)
        val criteriaUpper   = findViewById<TextView>(R.id.criteriaUpper)
        val criteriaNumber  = findViewById<TextView>(R.id.criteriaNumber)
        val criteriaSpecial = findViewById<TextView>(R.id.criteriaSpecial)

        // Cinsiyet spinner — placeholder, seçince metin görünür
        val genderOptions = listOf("Cinsiyet seçin", "Kadın", "Erkek", "Belirtmek istemiyorum")

        val genderAdapter = object : ArrayAdapter<String>(this, 0, genderOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val sel = spinnerGender.selectedItemPosition
                val tv = TextView(context).apply {
                    if (sel <= 0) {
                        text = "👤  Cinsiyet seçin"
                        setTextColor(Color.parseColor("#C8BDB1"))
                    } else {
                        text = "👤  ${genderOptions[sel]}"
                        setTextColor(Color.parseColor("#2C2318"))
                    }
                    textSize = 13f
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 0, 0, 0)
                }
                return tv
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = TextView(context)
                if (position == 0) {
                    tv.height = 0
                    tv.visibility = View.GONE
                } else {
                    tv.text = genderOptions[position]
                    tv.textSize = 14f
                    tv.setTextColor(Color.parseColor("#2C2318"))
                    tv.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    tv.setPadding(48, 40, 48, 40)
                }
                return tv
            }
        }
        spinnerGender.adapter = genderAdapter
        spinnerGender.setSelection(0)

        // Fitzpatrick Cilt Tipi spinner
        val skinTypeOptions = listOf(
            "Cilt tipini seçin",
            "Açık Ten  (Tip I-II)",
            "Orta Ten  (Tip III-IV)",
            "Koyu Ten  (Tip V-VI)"
        )
        val skinTypeHints = listOf(
            "",
            "Çok hassas — yüksek SPF şart, kolayca yanar",
            "Orta hassasiyet — düzenli koruma önerilir",
            "Düşük yanma riski — yine de koruma önemli"
        )

        val skinAdapter = object : ArrayAdapter<String>(this, 0, skinTypeOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val sel = spinnerSkinType.selectedItemPosition
                val tv = TextView(context).apply {
                    if (sel <= 0) {
                        text = "🌡️  Cilt tipini seçin"
                        setTextColor(Color.parseColor("#C8BDB1"))
                    } else {
                        text = "🌡️  ${skinTypeOptions[sel]}"
                        setTextColor(Color.parseColor("#2C2318"))
                    }
                    textSize = 13f
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, 0, 0, 0)
                }
                return tv
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val tv = TextView(context)
                if (position == 0) {
                    tv.height = 0
                    tv.visibility = View.GONE
                } else {
                    tv.text = skinTypeOptions[position]
                    tv.textSize = 13f
                    tv.setTextColor(Color.parseColor("#2C2318"))
                    tv.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    tv.setPadding(48, 36, 48, 36)
                }
                return tv
            }
        }
        spinnerSkinType.adapter = skinAdapter
        spinnerSkinType.setSelection(0) // placeholder

        tvSkinTypeHint.text = ""

        spinnerSkinType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                tvSkinTypeHint.text = skinTypeHints[pos]
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                updateStrengthBars(pass, bars)
                updateCriteria(pass, criteriaLength, criteriaUpper, criteriaNumber, criteriaSpecial)
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        tabLogin.setOnClickListener { finish() }
        btnLogin.setOnClickListener { finish() }

        btnReg.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName  = etLastName.text.toString().trim()
            val name      = "$firstName $lastName".trim()
            val email     = etEmail.text.toString().trim()
            val pass      = etPassword.text.toString().trim()
            val age       = etAge.text.toString().trim()
            val gender    = if (spinnerGender.selectedItemPosition <= 0) "" else genderOptions[spinnerGender.selectedItemPosition]
            val skinPos   = spinnerSkinType.selectedItemPosition
            val skinType  = if (skinPos <= 0) "" else skinTypeOptions[skinPos].substringBefore("—").trim()

            if (firstName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showError(tvError, "Ad, e-posta ve şifre zorunludur")
                return@setOnClickListener
            }
            if (pass.length < 6) {
                showError(tvError, "Şifre en az 6 karakter olmalı")
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            btnReg.isEnabled    = false
            tvError.visibility  = View.GONE

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser!!.uid
                        db.collection("users").document(uid).set(
                            hashMapOf(
                                "name"      to name,
                                "age"       to age,
                                "email"     to email,
                                "gender"    to gender,
                                "skinType"  to skinType,
                                "createdAt" to System.currentTimeMillis()
                            )
                        )
                        progress.visibility = View.GONE
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        progress.visibility = View.GONE
                        btnReg.isEnabled    = true
                        showError(tvError, "Kayıt başarısız: ${task.exception?.message}")
                    }
                }
        }
    }

    private fun updateCriteria(
        pass: String,
        cLength: TextView, cUpper: TextView,
        cNumber: TextView, cSpecial: TextView
    ) {
        fun check(tv: TextView, ok: Boolean) {
            tv.setTextColor(if (ok) Color.parseColor("#5D8A5E") else Color.parseColor("#C8BDB1"))
        }
        check(cLength,  pass.length >= 6)
        check(cUpper,   pass.any { it.isUpperCase() })
        check(cNumber,  pass.any { it.isDigit() })
        check(cSpecial, pass.any { !it.isLetterOrDigit() })
    }

    private fun updateStrengthBars(password: String, bars: List<View>) {
        val score = when {
            password.length >= 10 && password.any { it.isUpperCase() } &&
                    password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
            password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
            password.length >= 6 -> 2
            password.isNotEmpty() -> 1
            else -> 0
        }
        val colors = listOf("#C05050", "#C4963A", "#C4963A", "#5D8A5E")
        bars.forEachIndexed { i, bar ->
            bar.setBackgroundColor(
                if (i < score) Color.parseColor(colors[score - 1])
                else Color.parseColor("#E8E0D5")
            )
        }
    }

    private fun showError(tv: TextView, msg: String) {
        tv.text = "⚠️  $msg"
        tv.visibility = View.VISIBLE
    }
}