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

        "Baş / Yüz / Boyun" to RegionDetail(
            icon = "🙂",
            sunProtection = "Yüz bölgesi UV ışınlarına en fazla maruz kalan alandır. Her sabah nemlendiricinizin üzerine SPF 50+ mineral bazlı güneş koruyucu uygulayın. Öğle saatlerinde güneşe çıkılacaksa geniş kenarlı şapka ve UV filtreli güneş gözlüğü kullanmak ek koruma sağlar. Güneş koruyucuyu her 4-6 saatte bir yenilemeyi ihmal etmeyin.",
            dailyCare = "Sabah ve akşam nazik bir temizleyiciyle yüzünüzü yıkayın. Niasinamid veya C vitamini içeren serumlar cilt tonu eşitsizliklerine karşı etkilidir. Gece nemlendirici kullanmak cilt bariyerini güçlendirir; özellikle boyun ve dekolte bölgesini de nemlendirme rutininize dahil edin. Dudakları SPF içeren bir balmumu ile koruyun.",
            selfExam = "Her ay aynada yüzünüzü aydınlık bir ortamda dikkatlice inceleyin. Alın, şakak, burun kanatları, yanaklar, çene, kulak kepçesi ve kulak arkası mutlaka kontrol edilmesi gereken noktalardır. Boyun ve çene altını da göz ardı etmeyin. Çift ayna kullanarak arka boynu da görmeye çalışın.",
            lifestyle = "Sigara kullanımı yüz cildini erken yaşlandıran en önemli etkenlerden biridir. Kaliteli uyku cilt yenilenmesini destekler; günde 7-8 saat uyumak cildin dinlenmesine yardımcı olur. Yeterli su içmek (günde en az 2 litre) cilt nemini korur. Stres yönetimi de cilt sağlığını doğrudan etkiler.",
            whenToSeeDoctor = "Yüzde 6 haftadan uzun süre iyileşmeyen bir yara veya lezyon varsa, yeni oluşan asimetrik ya da düzensiz kenarlı bir ben fark ettiyseniz, kabuklanma veya kanama gibi belirtiler eşlik ediyorsa zaman kaybetmeden bir dermatologa başvurun."
        ),

        "Göğüs" to RegionDetail(
            icon = "❤️",
            sunProtection = "Açık ya da dekolteli kıyafet giyildiğinde göğüs ve dekolte bölgesi doğrudan UV ışınlarına maruz kalır. Bu bölgeye SPF 30 veya daha yüksek koruma faktörlü bir güneş koruyucu uygulayın. Deniz ve havuz aktivitelerinde su geçirmez formülleri tercih edin ve her 2 saatte bir yenileyin.",
            dailyCare = "Göğüs derisi ince ve hassas bir yapıya sahiptir. Kollajen üretimini destekleyen C vitamini içeren kremler bu bölgeye düzenli olarak uygulanabilir. Günlük nemlendiriciye ek olarak bir bitkisel yağ (örneğin kuşburnu veya badem yağı) kullanmak cilt elastikiyetini artırabilir.",
            selfExam = "Her ay duş sonrasında göğüs bölgesini bol ışık altında inceleyin. Meme başı ve areola çevresindeki renk, şekil veya doku değişikliklerini not alın. Daha önce fark etmediğiniz yeni bir ben ya da leke varsa onu da kaydedin.",
            lifestyle = "Sigara cilt elastikiyetini azaltır ve iyileşme sürecini yavaşlatır. Sağlıklı kiloda kalmak cilt gerginliğini destekler. Bol sebze ve meyve tüketimi, antioksidanlar aracılığıyla cilt sağlığını olumlu etkiler.",
            whenToSeeDoctor = "Göğüste yeni çıkan ya da değişen bir ben, iyileşmeyen bir yara, meme başından akıntı veya deride içeri çekilme gibi belirtilerden herhangi biri varsa mutlaka doktora gidin. Bu belirtiler geciktirilmeden değerlendirilmelidir."
        ),

        "Kol / Ön kol" to RegionDetail(
            icon = "💪",
            sunProtection = "Kollar günlük yaşamda yoğun UV maruziyetine açık bir bölgedir. Araba kullanırken bile cam camdan geçen UV ışınları kol derisine zarar verebilir; bu nedenle araç içinde de güneş koruyucu uygulamak önemlidir. Uzun süreli açık hava aktivitelerinde UPF 50+ değerine sahip uzun kollu kıyafetler pratik ve etkili bir koruma yöntemidir.",
            dailyCare = "Kollarınızı duş sonrasında düzenli olarak nemlendirin. Haftada bir ya da iki kez hafif bir peelinç eski hücre tabakasını uzaklaştırarak ürünlerin ciltten daha iyi emilmesine yardımcı olur.",
            selfExam = "Her iki kolun iç ve dış yüzünü, dirsek kıvrımını ve kol altını ayrı ayrı kontrol edin. İç kol melanomlar için sık gözden kaçan bir bölgedir; bu nedenle özellikle dikkat gerektirmektedir.",
            lifestyle = "Koşu, bisiklet veya yüzme gibi açık hava sporlarında kolları güneş koruyucu ile koruyun. Su sporlarında su geçirmez SPF formülleri seçin. Kimyasal maddelere veya güneşe uzun süre maruz kalınan işlerde koruyucu giysi kullanın.",
            whenToSeeDoctor = "Kolda hızla büyüyen, renk değiştiren, kaşınan ya da kanayan bir lezyon fark ederseniz beklemeden bir dermatologa başvurun."
        ),

        "El / Bilek" to RegionDetail(
            icon = "🤚",
            sunProtection = "El sırtı, güneş koruyucu uygulamada en çok ihmal edilen bölgelerden biridir. Her el yıkamasından sonra güneş koruyucuyu yenilemeniz gerekir. SPF içeren el kremi hem nemlendirici hem de koruyucu işlev görür; pratik bir çözüm arayanlar için idealdir.",
            dailyCare = "Eller günde birçok kez yıkandığından cilt kuruluğuna oldukça meyillidir. Her yıkama sonrasında nemlendirici kullanmak bu döngüyü kırar. Bulaşık yıkarken veya kimyasal maddelerle temas halindeyken mutlaka eldiven giyin.",
            selfExam = "El sırtını, avucu, her parmağın yan yüzlerini ve tırnak altını dikkatlice inceleyin. Tırnak altında görülen koyu renk çizgi veya renk değişikliği hafife alınmamalıdır; bu bulgu subungual melanom belirtisi olabilir.",
            lifestyle = "Bahçe işleri, ev onarımı veya el sanatları gibi aktivitelerde koruyucu eldiven kullanın. Temizlik ürünleri ve çözücülerle cildin doğrudan temas etmesinden kaçının.",
            whenToSeeDoctor = "Tırnak altında yeni oluşan koyu bir çizgi ya da renk değişikliği, el sırtında büyüyen asimetrik bir lezyon veya uzun süredir geçmeyen bir yara varsa bir dermatologa danışın."
        ),

        "Sırt" to RegionDetail(
            icon = "🫙",
            sunProtection = "Sırt bölgesi vücudun en geniş yüzeylerinden biri olmasına karşın kendi başına ulaşmanın güç olduğu bir alandır. Sırt için tasarlanmış uzun saplı sprey uygulayıcılar ya da yardım alarak bol miktarda SPF uygulamak bu sorunu çözer. Deniz ve havuz aktivitelerinde su geçirmez formüller seçin.",
            dailyCare = "Sırta losyon veya nemlendirici uygulamak için uzun saplı bir fırça ya da uygulaydı kullanabilirsiniz. Duş sonrasında cildi iyice kurulayıp hemen nemlendirici uygulamak etkinliği artırır.",
            selfExam = "Sırtı tek başınıza incelemeniz fiziksel olarak güçtür. Bir partnerin sırtınızı ayda bir düzenli olarak gözden geçirmesini isteyin ya da iki ayna yardımıyla omuz başlarını ve bel bölgesini kendiniz kontrol edin.",
            lifestyle = "Ter tutan, sentetik kumaşlardan yapılmış kıyafetler uzun süre giyildiğinde sırtta mantar enfeksiyonuna zemin hazırlayabilir. Nefes alabilen pamuklu kumaşları tercih etmek bu riski azaltır.",
            whenToSeeDoctor = "Sırt bölgesini kendi kendinize düzenli ve ayrıntılı biçimde kontrol etmek mümkün olmadığından yılda en az bir kez bir dermatoloğun sırtınızı bütünüyle incelemesi önerilmektedir."
        ),

        "Bacak" to RegionDetail(
            icon = "🦵",
            sunProtection = "Etek, şort veya mayo giyildiğinde bacaklar direkt güneş ışınlarına maruz kalır. SPF 30 veya daha yüksek koruyucu kullanın; özellikle diz altı ve baldır bölgesine daha fazla özen gösterin. Uzun yürüyüşler veya plaj gezilerinde her iki saatte bir güneş koruyucuyu yenileyin.",
            dailyCare = "Tıraş veya ağda sonrasında bacak derisi geçici olarak hassaslaşır; bu dönemde koku içermeyen, nazik bir nemlendirici tercih edin. Haftada bir uygulanan hafif bir peeling pürüzsüz ve canlı bir cilt görünümü sağlar.",
            selfExam = "Her iki bacağın ön, arka ve yan yüzlerini sistematik biçimde kontrol edin. Diz arkası ve kasık bölgesi sıklıkla gözden kaçan noktalardır; bunlara özellikle dikkat edin.",
            lifestyle = "Uzun süre hareketsiz kalmak bacak kan dolaşımını olumsuz etkiler. Düzenli yürüyüş veya egzersiz hem cilt hem de damar sağlığına katkı sağlar. Oturarak çalışıyorsanız ara ara kısa yürüyüşler yapın.",
            whenToSeeDoctor = "Bacakta yeni çıkan asimetrik ya da düzensiz kenarlı bir leke, hızla büyüyen bir ben veya uzun süredir geçmeyen bir yara varsa bir dermatologa başvurun."
        ),

        "Ayak" to RegionDetail(
            icon = "🦶",
            sunProtection = "Plajda kum üzerinde yürürken bile ayaklar yoğun UV yansımasına maruz kalır; bu nedenle ayak sırtına ve ayak bileğine güneş koruyucu uygulamayı unutmayın. Sandal giyen kişilerin ayak üstündeki açık alanlara da koruyucu sürmesi gerekir.",
            dailyCare = "Ayakları her gün ılık suyla yıkayın ve özellikle parmak aralarını dikkatlice kurulayın; nem birikimi mantar enfeksiyonuna zemin hazırlar. Tırnakları düz kesin ve sertleşmiş cilt bölgelerine düzenli olarak nemlendirici uygulayın.",
            selfExam = "Ayak tabanını ve topuğu iyi bir ışık altında ya da bir ayna yardımıyla düzenli olarak inceleyin. Tırnak altını ve tırnak yatağını da gözden geçirin. Koyu kahverengi veya siyah bir çizgi ya da renk değişikliği ciddiye alınması gereken bir uyarı işaretidir.",
            lifestyle = "Kamuya açık alanlarda (havuz kenarı, sauna, spor salonu duşları) mantar enfeksiyonunu önlemek için terlik kullanın. Dar veya sert tabanlı ayakkabılar sürtünme ve bası yaraları oluşturabilir; rahat ve nefes alabilen ayakkabıları tercih edin.",
            whenToSeeDoctor = "Tırnak altında yeni oluşan koyu bir çizgi ya da renk değişikliği, ayak tabanında büyüyen asimetrik bir lezyon veya iyileşmeye direnen bir yara varsa zaman kaybetmeden bir dermatologa gidin."
        )
    )

    private val regionList = listOf(
        "Baş / Yüz / Boyun", "Göğüs", "Kol / Ön kol",
        "El / Bilek", "Sırt", "Bacak", "Ayak"
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
        tvUrgency.setTextColor(Color.parseColor("#B85C2A"))

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

        addCard(container, "☀️  Güneş Koruması",            detail.sunProtection,    "#F4A574")
        addCard(container, "🌿  Günlük Bakım",               detail.dailyCare,        "#D17F52")
        addCard(container, "🔍  Nasıl Kontrol Edilir?",      detail.selfExam,         "#B85C2A")
        addCard(container, "🏃  Yaşam Tarzı Önerileri",      detail.lifestyle,        "#C8926A")
        addCard(container, "🏥  Ne Zaman Doktora Gidilmeli?", detail.whenToSeeDoctor, "#C05050")

        val dp = resources.displayMetrics.density
        val warnCard = androidx.cardview.widget.CardView(requireContext()).apply {
            radius = 12f * dp
            cardElevation = 0f
            setCardBackgroundColor(Color.parseColor("#FFF3E0"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (8 * dp).toInt()
            layoutParams = lp
        }
        val warnTv = TextView(requireContext()).apply {
            text = "ℹ️  Bu bilgiler genel sağlık rehberi niteliğindedir. Kesin tanı ve tedavi için mutlaka bir dermatologa başvurun."
            textSize = 11f
            setTextColor(Color.parseColor("#7A6E65"))
            setPadding((12 * dp).toInt(), (10 * dp).toInt(), (12 * dp).toInt(), (10 * dp).toInt())
            setLineSpacing(0f, 1.4f)
        }
        warnCard.addView(warnTv)
        container.addView(warnCard)

        btnDerma.setOnClickListener { openMaps("dermatoloji kliniği") }
        btnHospital.setOnClickListener { openMaps("hastane dermatoloji") }
    }

    private fun addCard(container: LinearLayout, title: String, content: String, color: String) {
        val dp = resources.displayMetrics.density
        val card = androidx.cardview.widget.CardView(requireContext()).apply {
            radius = 14f * dp
            cardElevation = 2f
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.bottomMargin = (10 * dp).toInt()
            layoutParams = lp
        }
        val inner = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((14 * dp).toInt(), (14 * dp).toInt(), (14 * dp).toInt(), (14 * dp).toInt())
        }
        val stripe = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (3 * dp).toInt()
            )
            background = GradientDrawable().apply {
                cornerRadius = 3f * dp
                setColor(Color.parseColor(color))
            }
        }
        val tvTitle = TextView(requireContext()).apply {
            text = title
            textSize = 12f
            setTextColor(Color.parseColor(color))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, (8 * dp).toInt(), 0, (6 * dp).toInt())
        }
        val tvContent = TextView(requireContext()).apply {
            text = content
            textSize = 13f
            setTextColor(Color.parseColor("#2C2318"))
            setLineSpacing(0f, 1.5f)
        }
        inner.addView(stripe)
        inner.addView(tvTitle)
        inner.addView(tvContent)
        card.addView(inner)
        card.alpha = 0f
        container.addView(card)
        card.animate()
            .alpha(1f)
            .translationYBy(-8f)
            .setDuration(200)
            .setStartDelay((container.childCount * 50).toLong())
            .start()
    }

    private fun openMaps(query: String) {
        val uri = Uri.parse("geo:0,0?q=$query")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/$query")))
        }
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