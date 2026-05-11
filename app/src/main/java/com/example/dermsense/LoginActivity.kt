package com.example.dermsense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                hideProgress()
                showError("Google girişi başarısız: ${e.message}")
            }
        } else {
            hideProgress()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db   = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) { goToMain(); return }

        // Google Sign-In ayarla
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail        = findViewById<EditText>(R.id.etEmail)
        val etPassword     = findViewById<EditText>(R.id.etPassword)
        val btnLogin       = findViewById<Button>(R.id.btnLogin)
        val btnRegister    = findViewById<TextView>(R.id.btnRegister)
        val tvForgot       = findViewById<TextView>(R.id.tvForgot)
        val btnGoogleSignIn= findViewById<LinearLayout>(R.id.btnGoogleSignIn)
        val tabRegister    = findViewById<TextView>(R.id.tabRegister)
        val logoSection    = findViewById<LinearLayout>(R.id.logoSection)
        val fadeIn         = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        logoSection.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_down))

        // Tab — Kayıt Ol'a geç
        tabRegister.setOnClickListener { goToRegister() }
        btnRegister.setOnClickListener { goToRegister() }

        // E-posta ile giriş
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass  = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                showError("E-posta ve şifre boş olamaz"); return@setOnClickListener
            }
            showProgress()
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener {
                    hideProgress()
                    if (it.isSuccessful) goToMain()
                    else { showError("Hatalı e-posta veya şifre"); tvError().startAnimation(fadeIn) }
                }
        }

        // Şifremi unuttum
        tvForgot.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) { showError("Şifre sıfırlamak için e-posta girin"); return@setOnClickListener }
            auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                val tv = tvError()
                if (task.isSuccessful) {
                    tv.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
                    tv.text = "✓  Şifre sıfırlama e-postası gönderildi"
                } else {
                    tv.text = "⚠️  E-posta gönderilemedi"
                }
                tv.visibility = View.VISIBLE
                tv.startAnimation(fadeIn)
            }
        }

        // Google ile giriş
        btnGoogleSignIn.setOnClickListener {
            showProgress()
            googleSignInClient.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                hideProgress()
                if (task.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener
                    val uid  = user.uid
                    // Yeni kullanıcıysa Firestore'a kaydet
                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                val displayName = user.displayName ?: ""
                                db.collection("users").document(uid).set(
                                    hashMapOf(
                                        "name"      to displayName,
                                        "email"     to (user.email ?: ""),
                                        "age"       to "",
                                        "skinType"  to "Tip III-IV (Orta)",
                                        "createdAt" to System.currentTimeMillis()
                                    )
                                )
                            }
                            goToMain()
                        }
                } else {
                    showError("Google girişi başarısız")
                }
            }
    }

    private fun showProgress() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        tvError().visibility = View.GONE
    }

    private fun hideProgress() {
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
    }

    private fun showError(msg: String) {
        val tv = tvError()
        tv.text = "⚠️  $msg"
        tv.visibility = View.VISIBLE
    }

    private fun tvError() = findViewById<TextView>(R.id.tvError)

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_left)
        finish()
    }

    private fun goToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}