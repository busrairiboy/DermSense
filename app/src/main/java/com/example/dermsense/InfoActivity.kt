package com.example.dermsense

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class InfoActivity : AppCompatActivity() {

    data class LesionInfo(
        val emoji: String,
        val description: String,
        val causes: String,
        val warning: String,
        val treatment: String,
        val prevention: String,
        val urgency: String,
        val urgencyColor: String
    )

    data class RegionInfo(
        val tip: String,
        val sunAdvice: String
    )

    private val lesionData = mapOf(
        "Melanom" to LesionInfo(
            emoji = "🔴",
            description = "Melanom, deri hücrelerinin kontrolsüz çoğalmasıyla oluşan en ciddi cilt kanseri türüdür. Erken teşhis hayat kurtarır.",
            causes = "Uzun süreli UV maruziyeti, genetik yatkınlık, açık ten ve çok sayıda ben risk faktörleridir. Yapay bronzlaşma cihazları da riski artırır.",
            warning = "Lezyonun şekli, rengi veya boyutu değişiyorsa; kaşınma, kanama veya kabuklanma varsa DERHAL dermatologa başvurun.",
            treatment = "Erken evrede cerrahi çok etkilidir. İleri evrelerde immunoterapi, hedefe yönelik tedavi veya radyoterapi uygulanır. Tedavi planı mutlaka uzman hekim tarafından belirlenir.",
            prevention = "SPF 50+ güneş koruyucu kullanın. 10:00-16:00 arası doğrudan güneşten kaçının. Yılda bir kez tüm vücut deri kontrolü yaptırın.",
            urgency = "ACİL — En kısa sürede dermatologa gidin",
            urgencyColor = "#FF4444"
        ),
        "Bazal Hücreli Karsinom" to LesionInfo(
            emoji = "🔴",
            description = "En yaygın cilt kanseri türüdür. Yavaş büyür ve nadiren yayılır, ancak erken tedavi önemlidir.",
            causes = "Uzun yıllar boyunca güneşe maruz kalmak temel nedendir. Açık tenli kişilerde, özellikle yüz, boyun ve ellerde sık görülür.",
            warning = "İnci gibi parlak, şeffaf veya pembe renkte kabarcıklar; kenarları silikleşen yaralar; iyileşmeyen yara varsa doktora gidin.",
            treatment = "Cerrahi çıkarma en yaygın yöntemdir. Kriyoterapi, fotodinamik terapi veya topikal ilaçlar da kullanılabilir. Tedavi seçimi konuma ve büyüklüğe göre belirlenir.",
            prevention = "Günlük SPF 30+ kullanımı, koruyucu giysi ve şapka. Yüz bölgesine özellikle dikkat edin.",
            urgency = "YÜKSEK — 2-4 hafta içinde dermatologa gidin",
            urgencyColor = "#FF6B35"
        ),
        "Aktinik Keratoz" to LesionInfo(
            emoji = "⚠️",
            description = "Güneş hasarından kaynaklanan, derinin üst katmanındaki anormal hücre değişimidir. Tedavi edilmezse %5-10 ihtimalle skuamöz hücre kanserine dönüşebilir.",
            causes = "Yıllarca süren güneş maruziyeti, açık ten rengi, yaşlılık ve zayıf bağışıklık sistemi risk faktörleridir.",
            warning = "Kaba, pullu, kırmızımsı lekeler; hafif kaşıntı veya yanma; zımpara kağıdı gibi dokunuş hissi varsa dikkat edin.",
            treatment = "Kriyoterapi (dondurma), lazer tedavisi, fotodinamik terapi veya topikal kremler kullanılır. Erken müdahale çok etkilidir.",
            prevention = "Düzenli güneş koruyucu kullanımı. Güneş hasarlı bölgeleri yıllık olarak kontrol ettirin.",
            urgency = "ORTA — 1-3 ay içinde dermatologa gidin",
            urgencyColor = "#FFA500"
        ),
        "Benign Keratoz" to LesionInfo(
            emoji = "✅",
            description = "Seboreik keratoz olarak da bilinen bu lezyon tamamen iyi huyludur. Yaşlanmayla birlikte ortaya çıkan renk değişiklikleridir.",
            causes = "Genetik yatkınlık ve yaşlanma temel nedendir. Güneş maruziyeti hızlandırabilir ama direkt neden değildir.",
            warning = "Ani büyüme, renk değişimi, kanama veya kaşıntı başlarsa dermatologa gidin. Aksi halde kontrol gerektirmez.",
            treatment = "Tedavi genellikle gerekli değildir. Kozmetik kaygı varsa kriyoterapi veya lazer ile kolayca çıkarılabilir.",
            prevention = "Özel bir önlem gerekmez. Genel cilt sağlığı için düzenli nemlendirme ve güneş koruyucu yeterlidir.",
            urgency = "DÜŞÜK — Rutin dermatoloji kontrolünde belirtebilirsiniz",
            urgencyColor = "#44BB44"
        ),
        "Dermatofibrom" to LesionInfo(
            emoji = "✅",
            description = "Derinin alt katmanında oluşan küçük, sert, iyi huylu bir tümördür. Genellikle bacaklarda görülür ve zararsızdır.",
            causes = "Böcek ısırığı, hafif yaralanma veya tıraş kesikleri tetikleyebilir. Kesin nedeni bilinmemektedir.",
            warning = "Hızlı büyüme, ani ağrı veya renk değişimi olursa kontrol ettirin. Bunlar olmadıkça takip gerektirmez.",
            treatment = "Tedavi genellikle gerekmez. Rahatsızlık veriyorsa cerrahi çıkarma veya kriyoterapi uygulanabilir.",
            prevention = "Özel önlem gerekmez. Cildi tahriş eden ısırık ve kesiklerden korunun.",
            urgency = "DÜŞÜK — Rutin kontrolde belirtebilirsiniz",
            urgencyColor = "#44BB44"
        ),
        "Melanositik Nevüs" to LesionInfo(
            emoji = "✅",
            description = "Yaygın adıyla 'ben' olarak bilinen bu lezyon, melanosit hücrelerinin bir arada birikmesiyle oluşur. Büyük çoğunluğu tamamen iyi huyludur.",
            causes = "Genetik faktörler ve güneş maruziyeti etkilidir. Çoğu kişide doğuştan veya erken çocuklukta oluşur.",
            warning = "ABCDE kuralını uygulayın: Asimetri, düzensiz Sınır, çoklu Renk, 6mm'den büyük Çap, Evrim (değişim). Bunlardan biri varsa dermatologa gidin.",
            treatment = "Sağlıklı benler tedavi gerektirmez. Şüpheli görünümlüler dermatoskopi ile incelenir; gerekirse cerrahi çıkarılır ve patolojiye gönderilir.",
            prevention = "Benleri düzenli olarak kendiniz takip edin. Yılda bir kez dermatoloji kontrolü önerilir. Güneşten koruyun.",
            urgency = "DÜŞÜK — Yıllık rutin kontrol yeterlidir",
            urgencyColor = "#44BB44"
        ),
        "Vasküler Lezyon" to LesionInfo(
            emoji = "⚠️",
            description = "Kan damarlarının anormal büyümesi veya genişlemesiyle oluşan lezyonlardır. Çoğu iyi huyludur ancak türüne göre değişir.",
            causes = "Doğumsal, hormonal değişiklikler, güneş hasarı veya yaşlanma nedeniyle oluşabilir. Bazı türler genetik geçişlidir.",
            warning = "Hızlı büyüme, kanama, renk koyulaşması veya ağrı başlarsa dermatologa gidin. Yüzdeki lezyonlar için estetik kaygı da geçerli bir neden.",
            treatment = "Lazer tedavisi en yaygın yöntemdir. Skleroterapi, kriyoterapi veya cerrahi de kullanılabilir. Tür ve konuma göre seçilir.",
            prevention = "Güneşten korunma ve SPF kullanımı yeni oluşumları azaltır. Sıcak duş ve sauna gibi kan damarlarını genişleten durumlardan kaçının.",
            urgency = "ORTA — 1-3 ay içinde dermatologa gidin",
            urgencyColor = "#FFA500"
        )
    )

    private val regionData = mapOf(
        "Baş / Yüz" to RegionInfo(
            tip = "Yüz bölgesi en çok güneşe maruz kalan bölgedir. Günlük SPF 50+ güneş koruyucu kullanımı zorunludur. Şapka ve güneş gözlüğü de koruma sağlar.",
            sunAdvice = "Yüz için mineral bazlı (çinko oksit içeren) güneş koruyucular önerilir. Nemlendiricili SPF ürünler günlük kullanım için idealdir."
        ),
        "Boyun" to RegionInfo(
            tip = "Boyun, genellikle güneş koruyucu uygulamada atlanan bir bölgedir. Yüzle birlikte mutlaka koruyun.",
            sunAdvice = "Yaz aylarında boyunluklu giysiler veya fular kullanmak UV koruması sağlar. Yüz SPF'ini boyuna da uzatın."
        ),
        "Göğüs" to RegionInfo(
            tip = "Göğüs bölgesi ince deriye sahiptir ve yaşlanma belirtileri erken görünür. Düzenli nemlendirme ve UV koruması önemlidir.",
            sunAdvice = "Dekolteli kıyafet giyildiğinde göğüs bölgesine mutlaka SPF uygulayın. Su geçirmez formüller havuz ve deniz aktiviteleri için tercih edilmeli."
        ),
        "Kol / Ön kol" to RegionInfo(
            tip = "Kollar yoğun UV maruziyetine açıktır. Araba sürerken camdan gelen UV için de koruma gerekir.",
            sunAdvice = "Araba kullanırken güneş tutan taraftaki kola özellikle dikkat edin. Uzun kollu UV koruyucu giysiler pratik bir alternatif."
        ),
        "El / Bilek" to RegionInfo(
            tip = "El arkası çok yüksek UV maruziyeti alır ve cilt yaşlanması burada belirgin olur. El yıkamadan sonra güneş koruyucu yenilemeyi unutmayın.",
            sunAdvice = "El için SPF 30+ kullanın ve her el yıkamadan sonra yenileyin. Eldiven de etkili bir koruma yöntemidir."
        ),
        "Sırt" to RegionInfo(
            tip = "Sırt bölgesi kendiliğinden kontrol etmesi en zor alan olduğundan, düzenli partner kontrolü veya dermatoloji muayenesi önemlidir.",
            sunAdvice = "Sırta güneş koruyucu uygulamak için yardım isteyin veya sırt spreyi kullanın. Çift katlı sörf mayoları pratik koruma sağlar."
        ),
        "Karın" to RegionInfo(
            tip = "Karın bölgesi genellikle giysi ile örtülü olduğundan daha az risk altındadır. Bununla birlikte bikini ve mayo tatillerinde dikkat gerekir.",
            sunAdvice = "Plaj tatillerinde su geçirmez SPF 50+ kullanın ve her 2 saatte bir yenileyin. Güneş altında uzandığınızda bu bölge doğrudan ışınıma maruz kalır."
        ),
        "Bacak" to RegionInfo(
            tip = "Kadınlarda bacak lezyonları sık görülür. Tıraş ve epilasyon cildi hassaslaştırabileceğinden sonrasında nemlendirin.",
            sunAdvice = "Etek ve şort giyildiğinde bacaklara SPF uygulamayı unutmayın. Güneşten korumalı tayt ve çoraplar da tercih edilebilir."
        ),
        "Ayak" to RegionInfo(
            tip = "Ayak tabanı ve parmak aralarındaki lezyonlar gözden kaçabilir. Düzenli muayenede ayaklara özel dikkat gösterin.",
            sunAdvice = "Plajda yürürken bile ayaklara SPF uygulayın. Kum üzerindeki UV yansıması beklenenden fazla olabilir."
        )
    )

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openMapsWithLocation()
        else openMapsGeneral()
    }

    private var mapQuery = "dermatoloji kliniği"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val diagnosis  = intent.getStringExtra("diagnosis") ?: "Melanositik Nevüs"
        val risk       = intent.getStringExtra("risk") ?: "DÜŞÜK"
        val region     = intent.getStringExtra("region") ?: "Belirtilmedi"
        val btnBack    = findViewById<android.widget.Button>(R.id.btnBack)
        val btnDerma   = findViewById<android.widget.Button>(R.id.btnFindDerma)
        val btnHospital = findViewById<android.widget.Button>(R.id.btnFindHospital)
        val container  = findViewById<LinearLayout>(R.id.infoContainer)

        // Animasyon
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        container.startAnimation(fadeIn)

        // Başlık
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = diagnosis

        val tvRisk = findViewById<TextView>(R.id.tvRisk)
        val info = lesionData[diagnosis] ?: lesionData["Melanositik Nevüs"]!!
        val regionInfo = regionData[region]

        val riskColor = when (risk) { "YÜKSEK" -> "#FF4444"; "ORTA" -> "#FFA500"; else -> "#44BB44" }
        tvRisk.text = "${info.emoji} ${info.urgency}"
        tvRisk.setTextColor(Color.parseColor(info.urgencyColor))

        // Kartları ekle
        addInfoCard(container, "📋 Bu Lezyon Nedir?", info.description, "#C9A84C")
        addInfoCard(container, "🔬 Olası Nedenler", info.causes, "#7B9AC9")
        addInfoCard(container, "⚠️ Dikkat Edilmesi Gerekenler", info.warning, "#FFA500")
        addInfoCard(container, "💊 Olası Tedavi Yöntemleri", info.treatment, "#44BB44")
        addInfoCard(container, "🛡️ Koruyucu Önlemler", info.prevention, "#AA88FF")

        // Bölgeye özel öneri
        if (regionInfo != null) {
            addInfoCard(container, "📍 $region Bölgesi İçin Özel Öneri", regionInfo.tip, "#FF8C8C")
            addInfoCard(container, "☀️ Güneş Koruma Önerisi", regionInfo.sunAdvice, "#FFD700")
        }

        // Genel uyarı
        addWarningCard(container)

        // Butonlar
        btnDerma.setOnClickListener {
            mapQuery = "dermatoloji kliniği"
            checkLocationAndOpenMaps()
        }
        btnHospital.setOnClickListener {
            mapQuery = "hastane dermatoloji"
            checkLocationAndOpenMaps()
        }
        btnBack.setOnClickListener { finish() }
    }

    private fun addInfoCard(container: LinearLayout, title: String, content: String, accentColor: String) {
        val dp = resources.displayMetrics.density
        val card = CardView(this).apply {
            radius = 16f * dp; cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#1A1A32"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = (12 * dp).toInt(); layoutParams = lp
        }
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((16*dp).toInt(), (16*dp).toInt(), (16*dp).toInt(), (16*dp).toInt())
        }

        // Renkli üst çizgi
        val stripe = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (3*dp).toInt())
            val bg = android.graphics.drawable.GradientDrawable().apply {
                cornerRadius = 4f * dp
                setColor(Color.parseColor(accentColor))
            }
            background = bg
        }

        val tvTitle = TextView(this).apply {
            text = title; textSize = 13f
            setTextColor(Color.parseColor(accentColor))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, (10*dp).toInt(), 0, (8*dp).toInt())
        }
        val tvContent = TextView(this).apply {
            text = content; textSize = 13f
            setTextColor(Color.parseColor("#CCCCDD"))
            setLineSpacing(0f, 1.4f)
        }
        inner.addView(stripe); inner.addView(tvTitle); inner.addView(tvContent)
        card.addView(inner)

        // Kart belirerek gelsin
        card.alpha = 0f
        container.addView(card)
        card.postDelayed({
            card.animate().alpha(1f).translationYBy(-10f).setDuration(300).start()
        }, (container.childCount * 60).toLong())
    }

    private fun addWarningCard(container: LinearLayout) {
        val dp = resources.displayMetrics.density
        val card = CardView(this).apply {
            radius = 16f * dp; cardElevation = 0f
            setCardBackgroundColor(Color.parseColor("#1A1225"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = (12 * dp).toInt(); layoutParams = lp
        }
        val tv = TextView(this).apply {
            text = "⚕️  Bu ekrandaki bilgiler genel sağlık bilgisi niteliğindedir. Kesin tanı ve tedavi için mutlaka bir dermatologa başvurunuz. Bu uygulama tıbbi teşhis veya tedavi aracı değildir."
            textSize = 12f; setTextColor(Color.parseColor("#7B7B9A"))
            setPadding((16*dp).toInt(), (14*dp).toInt(), (16*dp).toInt(), (14*dp).toInt())
            setLineSpacing(0f, 1.4f)
        }
        card.addView(tv); container.addView(card)
    }

    private fun checkLocationAndOpenMaps() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> openMapsWithLocation()
            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun openMapsWithLocation() {
        val uri = Uri.parse("geo:0,0?q=$mapQuery")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Maps yüklü değilse tarayıcıda aç
            val webUri = Uri.parse("https://www.google.com/maps/search/$mapQuery")
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun openMapsGeneral() {
        val webUri = Uri.parse("https://www.google.com/maps/search/$mapQuery")
        startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}