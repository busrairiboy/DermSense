package com.example.dermsense

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo   = findViewById<ImageView>(R.id.ivSplashLogo)
        val tvName   = findViewById<TextView>(R.id.tvSplashName)
        val tvSlogan = findViewById<TextView>(R.id.tvSplashSlogan)

        // Animasyonlar
        val fadeIn   = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp  = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val scaleIn  = AnimationUtils.loadAnimation(this, R.anim.scale_in)

        ivLogo.startAnimation(scaleIn)
        ivLogo.postDelayed({ tvName.startAnimation(fadeIn) }, 300)
        ivLogo.postDelayed({ tvSlogan.startAnimation(slideUp) }, 500)

        // 2.5 saniye sonra geç
        Handler(Looper.getMainLooper()).postDelayed({
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2500)
    }
}