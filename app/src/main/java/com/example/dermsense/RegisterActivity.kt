package com.example.dermsense

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
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

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName  = findViewById<EditText>(R.id.etLastName)
        val etEmail     = findViewById<EditText>(R.id.etEmail)
        val etPassword  = findViewById<EditText>(R.id.etPassword)
        val etAge       = findViewById<EditText>(R.id.etAge)
        val btnReg      = findViewById<Button>(R.id.btnRegister)
        val btnLogin    = findViewById<TextView>(R.id.btnLogin)
        val tabLogin    = findViewById<TextView>(R.id.tabLogin)
        val progress    = findViewById<ProgressBar>(R.id.progressBar)
        val tvError     = findViewById<TextView>(R.id.tvError)

        // Şifre gücü barları
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

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                updateStrengthBars(pass, bars)
                updateCriteria(pass, criteriaLength, criteriaUpper, criteriaNumber, criteriaSpecial)
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        // Tab — Giriş Yap'a geç
        tabLogin.setOnClickListener { finish() }
        btnLogin.setOnClickListener { finish() }

        // Kayıt Ol
        btnReg.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName  = etLastName.text.toString().trim()
            val name      = "$firstName $lastName".trim()
            val email     = etEmail.text.toString().trim()
            val pass      = etPassword.text.toString().trim()
            val age       = etAge.text.toString().trim()

            if (firstName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                showError(tvError, "Ad, e-posta ve şifre zorunludur"); return@setOnClickListener
            }
            if (pass.length < 6) {
                showError(tvError, "Şifre en az 6 karakter olmalı"); return@setOnClickListener
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
                                "skinType"  to "Tip III-IV (Orta)",
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
            tv.setTextColor(
                if (ok) android.graphics.Color.parseColor("#44BB44")
                else android.graphics.Color.parseColor("#444466")
            )
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
        val colors = listOf("#FF4444", "#FFA500", "#C9A84C", "#44BB44")
        bars.forEachIndexed { i, bar ->
            bar.setBackgroundColor(
                if (i < score) Color.parseColor(colors[score - 1])
                else Color.parseColor("#1E1E3A")
            )
        }
    }

    private fun showError(tv: TextView, msg: String) {
        tv.text = "⚠️  $msg"
        tv.visibility = View.VISIBLE
    }
}