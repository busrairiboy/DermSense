package com.example.dermsense

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InfoBottomSheet : BottomSheetDialogFragment() {

    data class RegionDetail(
        val icon: String,
        val sunProtection: String,
        val dailyCare: String,
        val selfExam: String,
        val lifestyle: String,
        val whenToSeeDoctor: String
    )

    private val regionDetails = mapOf(
        "Bas / Yuz / Boyun" to RegionDetail(
            icon = "🙂",
            sunProtection = "Yuz icin SPF 50+ mineral bazli guneş koruyucu kullanin. Her sabah nemlendiricinizin uzerine uygulayin. Gun icinde 4-6 saatte bir yenileyin. Genis kenarlı sapka ve UV koruyucu gunes gozlugu kullanin.",
            dailyCare = "Yuzunuzu sabah aksam nazikce temizleyin. Retinol veya niasinamid iceren urunler cilt yenilenmesine yardimci olur. Gece nemlendirici kullanmak onemlidir. Dudaklari da SPF iceren balzam ile koruyun.",
            selfExam = "Her ay aynada yuzunuzu detaylica inceleyin. Alın, sakak, burun, yanak, cene ve kulak arkasini kontrol edin. Boyun ve kulak arkasini da unutmayin.",
            lifestyle = "Sigara icmek yuz cildini ciddi olcude yaslandırır. Yeterli uyku cilt yenilenmesini destekler. Bol su icmek cilt nemini korur.",
            whenToSeeDoctor = "Yuzde herhangi bir lezyon 6 haftadan uzun sure devam ediyorsa, yeni olusan asimetrik lekeler, kabuklanma ve iyilesmeyen yaralar varsa dermatologa basvurun."
        ),
        "Gogus" to RegionDetail(
            icon = "❤️",
            sunProtection = "Dekolteli kiyafet giyildiginde gogus bolgesine mutlaka SPF 30+ uygulanmalidir. Su gecirmez formuller havuz ve deniz aktiviteleri icin idealdir.",
            dailyCare = "Gogus derisi ince ve hassastir. Kollajen uretimini destekleyen C vitamini iceren kremler kullanin. Nemlendirmeye ek olarak bir yag uygulayabilirsiniz.",
            selfExam = "Her ay dus sonrasi gogus bolgesini inceleyin. Meme basi veya areola cevresindeki lezyon degisimlerini not alin.",
            lifestyle = "Sigara cilt elastikiyetini azaltir. Saglikli kiloda kalmak cilt sagligini destekler.",
            whenToSeeDoctor = "Goguste yeni cikan veya degisen ben, iyilesmeyen yara veya meme basi akintisi varsa mutlaka doktora gidin."
        ),
        "Kol / On kol" to RegionDetail(
            icon = "💪",
            sunProtection = "Kollar yogun UV maruziyetine aciktir. Araba surerken de SPF uygulayın. Uzun kollu UV koruyucu (UPF 50+) giysiler iyi bir alternatiftir.",
            dailyCare = "Kollarinizi duzenli olarak nemlendirin. Hafif eksfoliyasyon eski hucreleri uzaklastirir.",
            selfExam = "Her iki kolu da ic ve dis yuzden kontrol edin. Dirsek kivrimlarini ve ic kolu inceleyin.",
            lifestyle = "Kisa kollu spor yaparken kolları guneş koruyucu ile koruyun. Su sporlarinda su gecirmez SPF kullanin.",
            whenToSeeDoctor = "Kolda hizla buyuyen, renk degistiren veya kasınan lezyon varsa dermatologa basvurun."
        ),
        "El / Bilek" to RegionDetail(
            icon = "🤚",
            sunProtection = "El arkası cok yuksek UV maruziyeti alır. El yikamadan sonra guneş koruyucuyu yenileyin. SPF iceren el kremi hem nemlendirici hem koruyucu olarak kullanilabilir.",
            dailyCare = "Elleri sik sik yikamak cildi kurutur — her yikamadan sonra nemlendirici kullanin. Bulaşik yikarken eldiven giyin.",
            selfExam = "El sirtini, avucu ve her parmagi dikkatlice kontrol edin. Tirnak altini da inceleyin — melanom bazen tirnak altinda gorulur.",
            lifestyle = "Bahce isi ve el isi icin koruyucu eldiven kullanin. Kimyasal temizlik urunlerine elleri maruz birakmayin.",
            whenToSeeDoctor = "Tirnak altinda koyu cizgi veya renk degisimi, el sirtinda buyuyen asimetrik lezyon varsa dermatologa gidin."
        ),
        "Sirt" to RegionDetail(
            icon = "🫙",
            sunProtection = "Sirt bolgesi kendinizin ulasamadiği yerdir. Sirt icin ozel puskurtmeli SPF urunler pratik bir cozumdur.",
            dailyCare = "Sirtiniza losyon veya nemlendirici uygulamak icin uzun sapli fırca kullanin.",
            selfExam = "Sirtı kendi basiniza kontrol etmek zordur. Bir partnerin sirtinizi duzenli olarak incelemesini isteyin veya iki ayna kullanin.",
            lifestyle = "Spor yaparken ter tutan kiyafetler sirtta mantar enfeksiyonuna yol acabilir.",
            whenToSeeDoctor = "Sirtı duzenli olarak kendinizin kontrol etmesi zor oldugundan yilda bir kez dermatolog tarafindan incelenmesi onerilir."
        ),
        "Bacak" to RegionDetail(
            icon = "🦵",
            sunProtection = "Etek, sort veya mayo giyildiginde bacaklar direkt UV alir. SPF 30+ kullanin ve ozellikle diz alti ve baldir bolgesine dikkat edin.",
            dailyCare = "Tirash veya agda sonrasi cilt hassaslasir — nazik nemlendirici kullanin. Hafif eksfoliyasyon pürüzsüz cilt icin onerilir.",
            selfExam = "Her iki bacagin on, arka ve yan yuzlerini kontrol edin. Diz arkasi ve kasik bolgesini de inceleyin.",
            lifestyle = "Uzun sure ayakta durma veya oturma bacak dolasimini olumsuz etkiler. Egzersiz kan dolasimini iyilestirir.",
            whenToSeeDoctor = "Bacakta yeni cikan asimetrik leke, hizla buyuyen ben varsa dermatologa gidin."
        ),
        "Ayak" to RegionDetail(
            icon = "🦶",
            sunProtection = "Plajda yururken bile ayaklara SPF uygulayin. Kum uzerindeki UV yansimasi beklenenden cok daha fazladir.",
            dailyCare = "Ayaklari her gun sicak suyla yikayin ve ozellikle parmak aralarin iyice kurulayın. Tirnaklari duzgun kesin.",
            selfExam = "Ayak tabanını duzenli olarak kontrol edin. Tirnak altı ve tirnak yataginı inceleyin. Koyu kahverengi veya siyah cizgiler ciddı uyarı isaretidir.",
            lifestyle = "Kamuya acik alanlarda mantar enfeksiyonunu onlemek icin terlik kullanin.",
            whenToSeeDoctor = "Tirnak altinda koyu cizgi, ayak tabaninda buyuyen asimetrik lezyon varsa mutlaka dermatologa gidin."
        )
    )

    private val regionList = listOf(
        "Bas / Yuz / Boyun", "Gogus", "Kol / On kol",
        "El / Bilek", "Sirt", "Bacak", "Ayak"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_sheet_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerRegion = view.findViewById<Spinner>(R.id.spinnerRegion)
        val btnGetInfo    = view.findViewById<Button>(R.id.btnGetInfo)
        val btnDerma      = view.findViewById<Button>(R.id.btnDerma)
        val btnHospital   = view.findViewById<Button>(R.id.btnHospital)
        val infoContainer = view.findViewById<LinearLayout>(R.id.infoContainer)
        val tvUrgency     = view.findViewById<TextView>(R.id.tvUrgency)
        val diagnosis     = arguments?.getString("diagnosis") ?: ""
        val defaultRegion = arguments?.getString("region") ?: regionList[0]

        tvUrgency.text = "Tarama Sonucu: $diagnosis"
        tvUrgency.setTextColor(Color.parseColor("#E8823A"))

        val regionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, regionList)
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRegion.adapter = regionAdapter

        val matchedRegion = regionList.firstOrNull { it.contains(defaultRegion.take(4)) } ?: regionList[0]
        spinnerRegion.setSelection(regionList.indexOf(matchedRegion).coerceAtLeast(0))

        showRegionInfo(matchedRegion, infoContainer, btnDerma, btnHospital)

        btnGetInfo.setOnClickListener {
            val region = spinnerRegion.selectedItem.toString()
            showRegionInfo(region, infoContainer, btnDerma, btnHospital)
        }
    }

    private fun showRegionInfo(
        region: String,
        container: LinearLayout,
        btnDerma: Button,
        btnHospital: Button
    ) {
        container.removeAllViews()
        val detail = regionDetails[region] ?: return

        addCard(container, "Gunes Korumasi",          detail.sunProtection,    "#E8823A")
        addCard(container, "Gunluk Bakim",             detail.dailyCare,        "#C4963A")
        addCard(container, "Nasil Kontrol Edilir?",   detail.selfExam,         "#7A9EC4")
        addCard(container, "Yasam Tarzi Onerileri",   detail.lifestyle,        "#8A7AC4")
        addCard(container, "Ne Zaman Doktora Gidilmeli?", detail.whenToSeeDoctor, "#C05050")

        val dp = resources.displayMetrics.density
        val warnCard = androidx.cardview.widget.CardView(requireContext()).apply {
            radius = 12f * dp; cardElevation = 0f
            setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = (8*dp).toInt(); layoutParams = lp
        }
        val warnTv = TextView(requireContext()).apply {
            text = "Bu bilgiler genel saglik rehberi niteligindedir. Tani ve tedavi icin mutlaka bir dermatologa basvurun."
            textSize = 11f; setTextColor(Color.parseColor("#7A6E65"))
            setPadding((12*dp).toInt(), (10*dp).toInt(), (12*dp).toInt(), (10*dp).toInt())
            setLineSpacing(0f, 1.3f)
        }
        warnCard.addView(warnTv); container.addView(warnCard)

        btnDerma.setOnClickListener { openMaps("dermatoloji klinigi") }
        btnHospital.setOnClickListener { openMaps("hastane dermatoloji") }
    }

    private fun addCard(container: LinearLayout, title: String, content: String, color: String) {
        val dp = resources.displayMetrics.density
        val card = androidx.cardview.widget.CardView(requireContext()).apply {
            radius = 14f * dp; cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = (10*dp).toInt(); layoutParams = lp
        }
        val inner = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((14*dp).toInt(), (14*dp).toInt(), (14*dp).toInt(), (14*dp).toInt())
        }
        val stripe = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (3*dp).toInt())
            background = GradientDrawable().apply { cornerRadius = 3f*dp; setColor(Color.parseColor(color)) }
        }
        val tvTitle = TextView(requireContext()).apply {
            text = title; textSize = 12f
            setTextColor(Color.parseColor(color))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, (8*dp).toInt(), 0, (6*dp).toInt())
        }
        val tvContent = TextView(requireContext()).apply {
            text = content; textSize = 13f
            setTextColor(Color.parseColor("#2C2318"))
            setLineSpacing(0f, 1.4f)
        }
        inner.addView(stripe); inner.addView(tvTitle); inner.addView(tvContent)
        card.addView(inner)
        card.alpha = 0f
        container.addView(card)
        card.animate().alpha(1f).translationYBy(-8f).setDuration(200)
            .setStartDelay((container.childCount * 50).toLong()).start()
    }

    private fun openMaps(query: String) {
        val uri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireActivity().packageManager) != null) startActivity(intent)
        else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/$query")))
    }

    companion object {
        fun newInstance(diagnosis: String, region: String): InfoBottomSheet {
            return InfoBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("diagnosis", diagnosis)
                    putString("region", region)
                }
            }
        }
    }
}