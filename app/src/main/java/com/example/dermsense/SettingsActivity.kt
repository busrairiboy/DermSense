package com.example.dermsense

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    companion object {
        const val CHANNEL_ID = "dermsense_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth  = FirebaseAuth.getInstance()
        db    = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("dermsense_prefs", Context.MODE_PRIVATE)

        val uid = auth.currentUser?.uid ?: return

        createNotificationChannel()

        // Bottom Nav
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_settings
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home     -> { startActivity(Intent(this, MainActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_scan     -> { startActivity(Intent(this, CameraActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_profile  -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_settings -> true
                else -> false
            }
        }

        // Hesap bilgilerini yükle
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            findViewById<EditText>(R.id.etName).setText(doc.getString("name") ?: "")
            findViewById<EditText>(R.id.etEmail).setText(doc.getString("email") ?: "")
            findViewById<EditText>(R.id.etAge).setText(doc.getString("age") ?: "")
            val skinType    = doc.getString("skinType") ?: "Tip III-IV (Orta)"
            val skinOptions = listOf("Tip I-II (Açık)", "Tip III-IV (Orta)", "Tip V-VI (Koyu)")
            val skinSpinner = findViewById<Spinner>(R.id.spinnerSkinType)
            skinSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, skinOptions).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            skinSpinner.setSelection(skinOptions.indexOf(skinType).coerceAtLeast(0))
        }

        // Profil kaydet
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            val name = findViewById<EditText>(R.id.etName).text.toString().trim()
            val age  = findViewById<EditText>(R.id.etAge).text.toString().trim()
            val skin = findViewById<Spinner>(R.id.spinnerSkinType).selectedItem.toString()
            if (name.isEmpty()) { toast("Ad boş olamaz"); return@setOnClickListener }
            db.collection("users").document(uid).update(mapOf("name" to name, "age" to age, "skinType" to skin))
                .addOnSuccessListener { toast("Profil güncellendi ✓") }
        }

        // E-posta değiştir
        findViewById<TextView>(R.id.btnChangeEmail).setOnClickListener {
            showChangeEmailDialog()
        }

        // Şifre değiştir
        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            showChangePasswordDialog()
        }

        // Tarama hatırlatıcısı
        val reminderSpinner = findViewById<Spinner>(R.id.spinnerReminder)
        val reminderOptions = listOf(
            "Kapalı",
            "Her hafta  (7 günde bir)",
            "İki haftada bir  (14 günde bir)",
            "Her ay  (30 günde bir)"
        )

        val reminderAdapter = object : ArrayAdapter<String>(this, 0, reminderOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return TextView(context).apply {
                    text = reminderOptions[position]
                    textSize = 13f
                    setTextColor(Color.parseColor("#2C2318"))
                    setPadding(0, 0, 0, 0)
                }
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return TextView(context).apply {
                    text = reminderOptions[position]
                    textSize = 14f
                    setTextColor(Color.parseColor("#2C2318"))
                    setBackgroundColor(Color.parseColor("#FFFFFF"))
                    setPadding(48, 44, 48, 44)
                }
            }
        }
        reminderSpinner.adapter = reminderAdapter
        reminderSpinner.setSelection(prefs.getInt("reminder_idx", 0))
        reminderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putInt("reminder_idx", pos).apply()
                db.collection("users").document(uid).update("reminder", reminderOptions[pos])
                if (pos > 0) sendReminderNotification(reminderOptions[pos])
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // UV bildirimi switch
        val switchUV = findViewById<Switch>(R.id.switchUVNotification)
        switchUV.isChecked = prefs.getBoolean("uv_notification", false)
        switchUV.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("uv_notification", checked).apply()
            if (checked) sendUVTestNotification()
        }

        // Dışa aktar
        findViewById<TextView>(R.id.btnExportData).setOnClickListener { exportScansAsJson(uid) }

        // Veri politikası
        findViewById<TextView>(R.id.btnPrivacyPolicy).setOnClickListener {
            showInfoDialog("Veri Politikası",
                "DermSense uygulaması, kullanıcı verilerini yalnızca uygulama işlevleri için kullanır.\n\n" +
                        "• Fotoğraflar cihazınızda işlenir, sunucuya gönderilmez.\n" +
                        "• Tarama sonuçları Firebase'de şifreli olarak saklanır.\n" +
                        "• Kişisel verileriniz üçüncü taraflarla paylaşılmaz.\n" +
                        "• Hesabınızı sildiğinizde tüm verileriniz kalıcı olarak silinir.")
        }

        // Tüm taramaları sil
        findViewById<TextView>(R.id.btnDeleteScans).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Taramaları Sil")
                .setMessage("Tüm tarama geçmişiniz silinecek. Bu işlem geri alınamaz.")
                .setPositiveButton("Sil") { _, _ -> deleteAllScans(uid) }
                .setNegativeButton("İptal", null).show()
        }

        // Hesabı sil
        findViewById<TextView>(R.id.btnDeleteAccount).setOnClickListener {
            showDeleteAccountDialog()
        }

        // Kullanım koşulları
        findViewById<TextView>(R.id.btnTerms).setOnClickListener {
            showInfoDialog("Kullanım Koşulları",
                "Bu uygulama bir araştırma prototipidir ve tıbbi teşhis aracı değildir.\n\n" +
                        "• Sonuçlar kesin tanı niteliği taşımaz.\n" +
                        "• Şüpheli lezyonlar için mutlaka dermatologa başvurun.\n" +
                        "• Uygulama geliştiricileri tıbbi sonuçlardan sorumlu tutulamaz.\n" +
                        "• 18 yaş altı kullanıcılar ebeveyn gözetiminde kullanmalıdır.")
        }

        // Geri bildirim
        findViewById<TextView>(R.id.btnFeedback).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:iriboybusra@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "DermSense Geri Bildirim")
                putExtra(Intent.EXTRA_TEXT, "Uygulama hakkında görüşlerim:\n\n")
            }
            startActivity(Intent.createChooser(intent, "E-posta Gönder"))
        }

        findViewById<TextView>(R.id.tvVersion).text   = "1.0.0"
        findViewById<TextView>(R.id.tvDeveloper).text = "Büşra İriboy"
    }

    // ── Bildirim kanalı ──────────────────────────────────
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "DermSense Bildirimleri",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Tarama hatırlatıcıları ve UV uyarıları" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendReminderNotification(period: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Tarama Hatırlatıcısı Ayarlandı")
            .setContentText("$period cilt taramanızı yapmanız için hatırlatılacaksınız.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$period cilt taramanızı yapmayı unutmayın. Düzenli tarama erken teşhis için önemlidir."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    private fun sendUVTestNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("☀️ UV Bildirimleri Açık")
            .setContentText("UV indeksi yüksek olduğunda bildirim alacaksınız.")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("UV indeksi 6'nın üzerine çıktığında 'Bugün UV yüksek! SPF 50+ kullanmayı ve gölgede kalmayı unutmayın 🌞' bildirimi alacaksınız."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(2, notification)
    }

    // ── Dialoglar ────────────────────────────────────────
    private fun showChangeEmailDialog() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(60, 20, 60, 20) }
        val etNewEmail  = EditText(this).apply { hint = "Yeni e-posta"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS }
        val etPassword  = EditText(this).apply { hint = "Şifrenizi girin"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        layout.addView(etNewEmail); layout.addView(etPassword)
        AlertDialog.Builder(this).setTitle("E-posta Değiştir").setView(layout)
            .setPositiveButton("Değiştir") { _, _ ->
                val newEmail = etNewEmail.text.toString().trim()
                val password = etPassword.text.toString()
                if (newEmail.isEmpty()) { toast("E-posta boş olamaz"); return@setPositiveButton }
                val user = auth.currentUser ?: return@setPositiveButton
                val cred = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(cred).addOnSuccessListener {
                    user.updateEmail(newEmail).addOnSuccessListener {
                        db.collection("users").document(user.uid).update("email", newEmail)
                        findViewById<EditText>(R.id.etEmail).setText(newEmail)
                        toast("E-posta güncellendi ✓")
                    }.addOnFailureListener { toast("E-posta güncellenemedi: ${it.message}") }
                }.addOnFailureListener { toast("Şifre hatalı") }
            }.setNegativeButton("İptal", null).show()
    }

    private fun showChangePasswordDialog() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(60, 20, 60, 20) }
        val etCurrent = EditText(this).apply { hint = "Mevcut şifre"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        val etNew     = EditText(this).apply { hint = "Yeni şifre (min 6)"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        val etConfirm = EditText(this).apply { hint = "Yeni şifre tekrar"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        layout.addView(etCurrent); layout.addView(etNew); layout.addView(etConfirm)
        AlertDialog.Builder(this).setTitle("Şifre Değiştir").setView(layout)
            .setPositiveButton("Değiştir") { _, _ ->
                val current = etCurrent.text.toString(); val new = etNew.text.toString(); val confirm = etConfirm.text.toString()
                if (new != confirm) { toast("Şifreler eşleşmiyor"); return@setPositiveButton }
                if (new.length < 6) { toast("Şifre en az 6 karakter olmalı"); return@setPositiveButton }
                val user = auth.currentUser ?: return@setPositiveButton
                val cred = EmailAuthProvider.getCredential(user.email!!, current)
                user.reauthenticate(cred).addOnSuccessListener {
                    user.updatePassword(new).addOnSuccessListener { toast("Şifre güncellendi ✓") }
                        .addOnFailureListener { toast("Hata: ${it.message}") }
                }.addOnFailureListener { toast("Mevcut şifre hatalı") }
            }.setNegativeButton("İptal", null).show()
    }

    private fun showDeleteAccountDialog() {
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(60, 20, 60, 20) }
        val tvWarn = TextView(this).apply { text = "Bu işlem geri alınamaz. Tüm verileriniz silinecek." }
        val etPassword = EditText(this).apply { hint = "Şifrenizi girin"; inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD }
        layout.addView(tvWarn); layout.addView(etPassword)
        AlertDialog.Builder(this).setTitle("⚠️ Hesabı Sil").setView(layout)
            .setPositiveButton("Hesabı Sil") { _, _ ->
                val password = etPassword.text.toString()
                val user = auth.currentUser ?: return@setPositiveButton
                val cred = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(cred).addOnSuccessListener {
                    db.collection("users").document(user.uid).delete()
                    user.delete().addOnSuccessListener {
                        toast("Hesap silindi")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finishAffinity()
                    }
                }.addOnFailureListener { toast("Şifre hatalı") }
            }.setNegativeButton("İptal", null).show()
    }

    private fun deleteAllScans(uid: String) {
        db.collection("users").document(uid).collection("scans").get().addOnSuccessListener { docs ->
            val batch = db.batch(); docs.forEach { batch.delete(it.reference) }
            batch.commit().addOnSuccessListener { toast("Tüm taramalar silindi ✓") }
        }
    }

    private fun exportScansAsJson(uid: String) {
        db.collection("users").document(uid).collection("scans")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().addOnSuccessListener { docs ->
                val sb = StringBuilder("[\n")
                docs.forEachIndexed { i, doc ->
                    sb.append("  {\n")
                    sb.append("    \"diagnosis\": \"${doc.getString("diagnosis")}\",\n")
                    sb.append("    \"risk\": \"${doc.getString("risk")}\",\n")
                    sb.append("    \"confidence\": ${doc.getLong("confidence")},\n")
                    sb.append("    \"region\": \"${doc.getString("region")}\",\n")
                    sb.append("    \"date\": \"${doc.getString("date")}\"\n")
                    sb.append("  }${if (i < docs.size()-1) "," else ""}\n")
                }
                sb.append("]")
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; putExtra(Intent.EXTRA_TEXT, sb.toString())
                    putExtra(Intent.EXTRA_SUBJECT, "DermSense Tarama Verileri")
                }
                startActivity(Intent.createChooser(intent, "Paylaş"))
            }
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("Tamam", null).show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}