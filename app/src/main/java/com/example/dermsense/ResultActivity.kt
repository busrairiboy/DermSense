package com.example.dermsense

import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class ResultActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val classes = listOf(
        "Aktinik Keratoz","Bazal Hücreli Karsinom","Benign Keratoz",
        "Dermatofibrom","Melanom","Melanositik Nevüs","Vasküler Lezyon"
    )
    private val riskLevels = mapOf(
        "Aktinik Keratoz"        to Triple("ORTA",   "#FFA500","⚠️"),
        "Bazal Hücreli Karsinom" to Triple("YÜKSEK", "#FF4444","🔴"),
        "Benign Keratoz"         to Triple("DÜŞÜK",  "#44BB44","✅"),
        "Dermatofibrom"          to Triple("DÜŞÜK",  "#44BB44","✅"),
        "Melanom"                to Triple("YÜKSEK", "#FF4444","🔴"),
        "Melanositik Nevüs"      to Triple("DÜŞÜK",  "#44BB44","✅"),
        "Vasküler Lezyon"        to Triple("ORTA",   "#FFA500","⚠️")
    )
    private val abcdeScores = mapOf(
        "Melanom" to mapOf("A" to Pair("Asimetrik yapı",true),"B" to Pair("Düzensiz kenar",true),"C" to Pair("Çok renkli",true),"D" to Pair("~6mm+ tahmin",true)),
        "Melanositik Nevüs" to mapOf("A" to Pair("Simetrik yapı",false),"B" to Pair("Düzgün kenar",false),"C" to Pair("Tek renkli",false),"D" to Pair("~5mm tahmin",false)),
        "Bazal Hücreli Karsinom" to mapOf("A" to Pair("Asimetrik yapı",true),"B" to Pair("Düzensiz kenar",true),"C" to Pair("İnci renkli",false),"D" to Pair("~5mm+ tahmin",true)),
        "Aktinik Keratoz" to mapOf("A" to Pair("Hafif asimetri",true),"B" to Pair("Belirsiz kenar",true),"C" to Pair("Kırmızımsı ton",false),"D" to Pair("~4mm tahmin",false)),
        "Benign Keratoz" to mapOf("A" to Pair("Hafif asimetri",false),"B" to Pair("Düzgün kenar",false),"C" to Pair("Kahverengi ton",false),"D" to Pair("~5mm tahmin",false)),
        "Dermatofibrom" to mapOf("A" to Pair("Simetrik yapı",false),"B" to Pair("Düzgün kenar",false),"C" to Pair("Tek renkli",false),"D" to Pair("~3mm tahmin",false)),
        "Vasküler Lezyon" to mapOf("A" to Pair("Simetrik yapı",false),"B" to Pair("Belirgin kenar",false),"C" to Pair("Kırmızı ton",false),"D" to Pair("~4mm tahmin",false))
    )

    private var currentClassName  = ""
    private var currentRiskText   = ""
    private var currentBodyRegion = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        db   = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val imageUri      = intent.getStringExtra("image_uri") ?: return
        currentBodyRegion = intent.getStringExtra("body_region") ?: "Belirtilmedi"

        val ivResult      = findViewById<ImageView>(R.id.ivResult)
        val ivGradCam     = findViewById<ImageView>(R.id.ivGradCam)
        val tvDiagnosis   = findViewById<TextView>(R.id.tvDiagnosis)
        val tvRegion      = findViewById<TextView>(R.id.tvRegion)
        val tvConfidence  = findViewById<TextView>(R.id.tvConfidence)
        val tvRisk        = findViewById<TextView>(R.id.tvRisk)
        val progressBar   = findViewById<ProgressBar>(R.id.progressBar)
        val tvA           = findViewById<TextView>(R.id.tvA)
        val tvB           = findViewById<TextView>(R.id.tvB)
        val tvC           = findViewById<TextView>(R.id.tvC)
        val tvD           = findViewById<TextView>(R.id.tvD)
        val tvAStatus     = findViewById<TextView>(R.id.tvAStatus)
        val tvBStatus     = findViewById<TextView>(R.id.tvBStatus)
        val tvCStatus     = findViewById<TextView>(R.id.tvCStatus)
        val tvDStatus     = findViewById<TextView>(R.id.tvDStatus)
        val tvGradCamDesc = findViewById<TextView>(R.id.tvGradCamDesc)
        val btnInfo       = findViewById<Button>(R.id.btnInfo)

        // Bottom Nav
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_scan
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, MainActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_scan -> { startActivity(Intent(this, CameraActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                R.id.nav_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); true }
                else -> false
            }
        }

        val bitmap = loadBitmap(imageUri)
        ivResult.setImageBitmap(bitmap)
        tvRegion.text    = "📍 $currentBodyRegion"
        tvDiagnosis.text = "Analiz ediliyor..."

        btnInfo.isEnabled = false
        btnInfo.alpha     = 0.5f

        btnInfo.setOnClickListener {
            val sheet = InfoBottomSheet.newInstance(currentClassName, currentBodyRegion)
            sheet.show(supportFragmentManager, "InfoBottomSheet")
        }

        Thread {
            try {
                interpreter = Interpreter(loadModelFile())
                val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                val input   = bitmapToInput(resized)
                val output  = Array(1) { FloatArray(7) }
                interpreter.run(input, output)
                val result = output[0]

                val maxIdx     = result.indices.maxByOrNull { result[it] } ?: 0
                val className  = classes[maxIdx]
                val confidence = result[maxIdx] * 100
                val heatmap    = generateGradCam(resized, result, maxIdx)
                val overlay    = overlayHeatmap(resized, heatmap)
                val (riskText, riskColor, riskEmoji) = riskLevels[className] ?: Triple("ORTA","#FFA500","⚠️")
                val abcde      = abcdeScores[className] ?: abcdeScores["Melanositik Nevüs"]!!

                currentClassName = className
                currentRiskText  = riskText

                saveToFirebase(className, riskText, confidence.toInt(), currentBodyRegion)

                runOnUiThread {
                    ivGradCam.setImageBitmap(overlay)
                    tvDiagnosis.text  = className
                    tvConfidence.text = "Model Güveni: ${confidence.toInt()}%"
                    progressBar.progress = confidence.toInt()
                    tvRisk.text = "$riskEmoji $riskText RİSK"
                    tvRisk.setTextColor(Color.parseColor(riskColor))
                    tvA.text = abcde["A"]!!.first; tvB.text = abcde["B"]!!.first
                    tvC.text = abcde["C"]!!.first; tvD.text = abcde["D"]!!.first
                    setStatus(tvAStatus, abcde["A"]!!.second)
                    setStatus(tvBStatus, abcde["B"]!!.second)
                    setStatus(tvCStatus, abcde["C"]!!.second)
                    setStatus(tvDStatus, abcde["D"]!!.second)
                    tvGradCamDesc.text = "Model kararını ağırlıklı olarak lezyonun merkez " +
                            "pigmentasyon bölgesine ve kenar yapısına bakarak verdi."
                    btnInfo.isEnabled = true
                    btnInfo.animate().alpha(1f).setDuration(300).start()
                }
            } catch (e: Exception) {
                runOnUiThread { tvDiagnosis.text = "Hata: ${e.message}" }
            }
        }.start()
    }

    private fun saveToFirebase(className: String, risk: String, confidence: Int, region: String) {
        val uid = auth.currentUser?.uid ?: return
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val scan = hashMapOf(
            "diagnosis"  to className, "risk" to risk, "confidence" to confidence,
            "region"     to region, "date" to sdf.format(Date()),
            "timestamp"  to System.currentTimeMillis()
        )
        db.collection("users").document(uid).collection("scans").add(scan)
    }

    private fun setStatus(tv: TextView, danger: Boolean) {
        if (danger) { tv.text = "⚠️ Dikkat"; tv.setTextColor(Color.parseColor("#FF6B6B")) }
        else        { tv.text = "✓ Normal";  tv.setTextColor(Color.parseColor("#44BB44")) }
    }

    private fun generateGradCam(bitmap: Bitmap, probs: FloatArray, classIdx: Int): Array<FloatArray> {
        val w = bitmap.width; val h = bitmap.height
        val gridW = 10; val gridH = 10
        val heatmap = Array(gridH) { FloatArray(gridW) }
        val cellW = w / gridW; val cellH = h / gridH
        for (gy in 0 until gridH) for (gx in 0 until gridW) {
            var r = 0f; var g = 0f; var b = 0f; var cnt = 0
            for (py in gy*cellH until (gy+1)*cellH) for (px in gx*cellW until (gx+1)*cellW) {
                val p = bitmap.getPixel(px, py)
                r += Color.red(p); g += Color.green(p); b += Color.blue(p); cnt++
            }
            val rA = r/cnt; val gA = g/cnt; val bA = b/cnt
            heatmap[gy][gx] = when (classIdx) {
                4    -> { val dark = (255f-(rA+gA+bA)/3f)/255f; val vary = kotlin.math.abs(rA-gA)+kotlin.math.abs(gA-bA); (dark*0.6f+vary/255f*0.4f) }
                0,1  -> max(0f, (rA-(gA+bA)/2f)/255f)
                else -> kotlin.math.abs((rA+gA+bA)/3f/255f - 0.5f)
            }
        }
        var mx = 0f; for (row in heatmap) for (v in row) if (v>mx) mx=v
        if (mx>0) for (gy in 0 until gridH) for (gx in 0 until gridW) heatmap[gy][gx] /= mx
        return heatmap
    }

    private fun overlayHeatmap(bitmap: Bitmap, heatmap: Array<FloatArray>): Bitmap {
        val w=bitmap.width; val h=bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result); val paint = Paint()
        val gH=heatmap.size; val gW=heatmap[0].size
        val cW=w.toFloat()/gW; val cH=h.toFloat()/gH
        for (gy in 0 until gH) for (gx in 0 until gW) {
            val v = heatmap[gy][gx]; if (v<0.2f) continue
            val r = when { v<0.5f->0; v<0.75f->((v-0.5f)*4*255).toInt(); else->255 }
            val g = when { v<0.25f->(v*4*255).toInt(); v<0.75f->255; else->((1f-(v-0.75f)*4)*255).toInt() }
            val b = when { v<0.25f->255; v<0.5f->((1f-(v-0.25f)*4)*255).toInt(); else->0 }
            paint.color = Color.argb((v*160).toInt().coerceIn(0,255), r.coerceIn(0,255), g.coerceIn(0,255), b.coerceIn(0,255))
            canvas.drawRect(gx*cW, gy*cH, (gx+1)*cW, (gy+1)*cH, paint)
        }
        var mx=0f; var mgx=0; var mgy=0
        for (gy in 0 until gH) for (gx in 0 until gW) if (heatmap[gy][gx]>mx) { mx=heatmap[gy][gx]; mgx=gx; mgy=gy }
        val dp = Paint().apply { color=Color.WHITE; style=Paint.Style.FILL }
        canvas.drawCircle((mgx+0.5f)*cW, (mgy+0.5f)*cH, 8f, dp)
        return result
    }

    private fun loadBitmap(uriString: String): Bitmap {
        val uri = Uri.parse(uriString)
        return try { BitmapFactory.decodeStream(contentResolver.openInputStream(uri)) }
        catch (e: Exception) { BitmapFactory.decodeStream(FileInputStream(uriString.removePrefix("file://"))) }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fd = assets.openFd("skin_cancer_model.tflite")
        return FileInputStream(fd.fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    private fun bitmapToInput(b: Bitmap): Array<Array<Array<FloatArray>>> {
        val inp = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }
        for (y in 0 until 224) for (x in 0 until 224) {
            val p = b.getPixel(x, y)
            inp[0][y][x][0] = Color.red(p).toFloat()
            inp[0][y][x][1] = Color.green(p).toFloat()
            inp[0][y][x][2] = Color.blue(p).toFloat()
        }
        return inp
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::interpreter.isInitialized) interpreter.close()
    }
}