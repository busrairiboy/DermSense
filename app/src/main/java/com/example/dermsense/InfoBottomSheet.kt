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

    // Her bölge için çok detaylı öneriler
    data class RegionDetail(
        val icon: String,
        val sunProtection: String,
        val dailyCare: String,
        val selfExam: String,
        val lifestyle: String,
        val whenToSeeDoctor: String
    )

    private val regionDetails = mapOf(
        "Baş / Yüz" to RegionDetail(
            icon = "👤",
            sunProtection = "Yüz için SPF 50+ mineral bazlı (çinko oksit) güneş koruyucu kullanın. Her sabah nemlendiricinizin üzerine uygulayın. Gün içinde 4-6 saatte bir yenileyin. Geniş kenarlı şapka (7cm+) ve UV koruyucu güneş gözlüğü günlük rutininizin parçası olmalı.",
            dailyCare = "Yüzünüzü sabah akşam nazikçe temizleyin. Retinol veya niasinamid içeren ürünler cilt yenilenmesine yardımcı olur. Gece nemlendirici kullanmak önemlidir. Göz çevresine ayrı bir göz kremi uygulayın. Dudakları da SPF içeren balzam ile koruyun.",
            selfExam = "Her ay aynada yüzünüzü detaylıca inceleyin. Alın, şakak, burun, yanak, çene ve kulak arkasını kontrol edin. Yeni çıkan, değişen veya kaşınan lekeler varsa not alın. Dudak kenarlarındaki değişimlere dikkat edin. Boyun ve kulak arkasını da unutmayın.",
            lifestyle = "Sigara içmek yüz cildini ciddi ölçüde yaşlandırır ve kanser riskini artırır. Yeterli uyku (7-8 saat) cilt yenilenmesini destekler. Bol su içmek (günde 2-2.5L) cilt nemini korur. A, C ve E vitaminleri içeren beslenme cilt sağlığı için önemlidir.",
            whenToSeeDoctor = "Yüzde herhangi bir lezyon 6 haftadan uzun süre değişmeden kalmıyorsa veya büyüyorsa, yeni oluşan asimetrik lekeler, kabuklanma ve iyileşmeyen yaralar varsa dermatologa başvurun. Yıllık rutin kontrol önerilir."
        ),
        "Boyun" to RegionDetail(
            icon = "🫀",
            sunProtection = "Boyun bölgesi güneş koruyucu uygulamada en sık atlanan yerdir. Yüze uygularken mutlaka boyuna da uzatın. Boyunluklu giysiler veya fular da etkili UV koruması sağlar. V yaka kıyafetlerde özellikle dikkatli olun.",
            dailyCare = "Boyun cildi yüz kadar nazik olmasına rağmen çoğu zaman bakım rutinine dahil edilmez. Yüzünüze uyguladığınız nemlendirici ve serumu boyuna da uygulayın. Boynu yukarıdan aşağıya nazikçe masaj yaparak uygulayın. Telefon veya bilgisayar kullanırken tekrarlayan boyun katlantılarına dikkat edin.",
            selfExam = "Aynada boyun önü ve yanlarını inceleyin. Arka boynu kontrol etmek için el aynası kullanın. Saç çizgisi yakınındaki bölgeleri özellikle kontrol edin. Lenf bezi şişliği veya anormal çıkıntılara dikkat edin.",
            lifestyle = "Uzun süre aşağıya bakmak (telefon, tablet) boyunda tekrarlayan katlantılar oluşturabilir. Egzersiz ve iyi duruş alışkanlıkları bu durumu azaltır. Yüksek yaka giysiler veya eşarp seçimi hem koruma hem de estetik açıdan faydalıdır.",
            whenToSeeDoctor = "Boyunda ağrısız şişlik, hızlı büyüyen lezyon veya yara iyileşmiyorsa hemen doktora gidin. Tiroid bölgesindeki şişlikler de mutlaka değerlendirilmelidir."
        ),
        "Göğüs" to RegionDetail(
            icon = "🫁",
            sunProtection = "Dekolteli kıyafet giyildiğinde göğüs bölgesine mutlaka SPF 30+ uygulanmalıdır. Su geçirmez formüller havuz ve deniz aktiviteleri için idealdir. Plajda güneşlenirken her 2 saatte bir yenileyin. Halter üstü ve bikinilerin açıkta bıraktığı bölgelere dikkat edin.",
            dailyCare = "Göğüs derisi ince ve hassastır. Kollajen üretimini destekleyen C vitamini ve peptit içeren kremler kullanın. Nemlendiriciye ek olarak bir yağ (argan, jojoba) uygulayabilirsiniz. Sütyen kayışlarının sürtünme yaratabileceği bölgeleri kontrol edin.",
            selfExam = "Her ay duş sonrası göğüs bölgesini inceleyin. Memeler dahil tüm göğüs cildini kontrol edin. Meme başı veya areola çevresindeki lezyon değişimlerini not alın. Göğüs üstü ve sternum (göğüs kemiği) bölgesini de kontrol edin.",
            lifestyle = "Sigara cilt elastikiyetini azaltır ve kanser riskini artırır. Sağlıklı kiloda kalmak cilt sağlığını destekler. Hamilelik ve emzirme döneminde cilt değişimleri olabilir, şüpheli lezyonları dermatologa gösterin.",
            whenToSeeDoctor = "Göğüste yeni çıkan veya değişen ben, iyileşmeyen yara veya egzama benzeri leke, meme başı akıntısı veya içe çöküklük varsa mutlaka doktora gidin. Kadınlarda aylık kendi kendine meme muayenesi önerilir."
        ),
        "Kol / Ön kol" to RegionDetail(
            icon = "💪",
            sunProtection = "Kollar yoğun UV maruziyetine açıktır. Araba sürerken direksiyona yakın kol pencereden gelen UV alır — bu nedenle araç içinde de SPF uygulayın. Uzun kollu UV koruyucu (UPF 50+) giysiler güneş koruyucuya pratik bir alternatiftir. Kol dışı yüzeyi (güneşe bakan taraf) daha fazla risk altındadır.",
            dailyCare = "Kollarınızı düzenli olarak nemlendirin, özellikle dirsek bölgesi kuru ve pullu olabilir. Hafif eksfoliyasyon (haftada 1-2 kez) eski hücreleri uzaklaştırır. Tıraş veya lazer epilasyon sonrası cildi sakinleştirici ürünler kullanın.",
            selfExam = "Her iki kolu da iç ve dış yüzden kontrol edin. Dirsek kıvrımlarını ve iç kolu (güneşe az maruz kalan bölgeler dahil) inceleyin. Kol altı lenf bezleri bölgesini de kontrol edin.",
            lifestyle = "Kısa kollu spor yaparken (koşu, bisiklet) kolları güneş koruyucu ile koruyun. Su sporlarında su geçirmez SPF kullanın. Tarım veya dış mekan çalışmalarında uzun kollu giysi tercih edin.",
            whenToSeeDoctor = "Kolda hızla büyüyen, renk değiştiren veya kaşınan lezyon, iyileşmeyen yara ya da 6 haftadan uzun süren şikayetler varsa dermatologa başvurun."
        ),
        "El / Bilek" to RegionDetail(
            icon = "🤚",
            sunProtection = "El arkası çok yüksek UV maruziyeti alır ve bu bölgede yaşlanma belirtileri erken görünür. El yıkamadan sonra güneş koruyucuyu yenileyin. Koruyucu eldivenler hem UV hem de kimyasal maruziyete karşı koruma sağlar. SPF içeren el kremi hem nemlendirici hem koruyucu olarak kullanılabilir.",
            dailyCare = "Elleri sık sık yıkamak cildi kurutur — her yıkamadan sonra nemlendirici kullanın. Üre içeren kremler el derisi için oldukça etkilidir. Bulaşık yıkarken eldiven giyin. Tırnak çevresini nemli tutmak çatlama ve enfeksiyonları önler.",
            selfExam = "El sırtını, avucu ve her parmağı dikkatlice kontrol edin. Tırnak altını ve tırnak yatağını da inceleyin — melanom bazen tırnak altında görülür. Tırnaklarda koyu çizgi veya renk değişimi varsa mutlaka kontrol ettirin.",
            lifestyle = "Bahçe işi ve el işi için koruyucu eldiven kullanın. Kimyasal temizlik ürünlerine elleri maruz bırakmayın. Soğuk havalarda kalın eldiven kullanmak el cildini korur.",
            whenToSeeDoctor = "Tırnak altında koyu çizgi veya renk değişimi, el sırtında büyüyen asimetrik lezyon, uzun süredir iyileşmeyen yara varsa dermatologa gidin. Tırnak melanomunun erken belirtileri tırnak altında başlar."
        ),
        "Sırt" to RegionDetail(
            icon = "🔙",
            sunProtection = "Sırt bölgesi kendinizin ulaşamadığı yerdir ve yardım almanız gerekir. Sırt için özel püskürtmeli SPF ürünler pratik bir çözümdür. Yüzme, spor veya plajda mutlaka sırtınızı da koruyun. Sörf ve yüzme mayolarının kapattığı alanlara dikkat edin.",
            dailyCare = "Sırtınıza losyon veya nemlendirici uygulamak için uzun saplı fırça veya aplikatör kullanın. Akne veya sivilce eğilimi varsa salisilik asit içeren ürünler kullanılabilir. Duş sonrası sırtı nazikçe ve tamamen kurulayın.",
            selfExam = "Sırtı kendi başınıza kontrol etmek zordur. Bir partnerin sırtınızı düzenli olarak incelemesini isteyin veya iki ayna kullanın. Sırt melanomları geç fark edildiği için en tehlikeli grupta yer alır. Yılda en az bir kez dermatoloji muayenesi şiddetle önerilir.",
            lifestyle = "Spor yaparken ter tutan kıyafetler sırtta mantar enfeksiyonuna yol açabilir — hızlı kuruyan kumaşlar tercih edin. Sırt çantası kullanıyorsanız sırtı sık sık havalandırın.",
            whenToSeeDoctor = "Sırtı düzenli olarak kendinizin kontrol etmesi zor olduğundan yılda bir kez dermatolog tarafından incelenmesi önerilir. Partner tarafından fark edilen her yeni veya değişen lezyon için randevu alın."
        ),
        "Karın" to RegionDetail(
            icon = "🫃",
            sunProtection = "Karın bölgesi genellikle giysiyle örtülüdür. Ancak yaz aylarında plaj, havuz ve tatilde açık kalır. Bu dönemlerde su geçirmez SPF 50+ kullanın ve her 2 saatte bir yenileyin. Karın derisinin ince ve hassas olduğunu unutmayın.",
            dailyCare = "Karın bölgesini nemlendirmek, özellikle hamilelik döneminde çatlak izlerini önlemeye yardımcı olabilir. C vitamini ve hyalüronik asit içeren serumlar cilt elastikiyetini destekler. Çatlak iziyle mücadelede argan yağı veya shea butter etkili olabilir.",
            selfExam = "Karın bölgesini ayna önünde inceleyin. Göbek çevresini ve katlantı bölgelerini kontrol edin. Karın derisi katlantılarında mantar enfeksiyonu veya cilt tahrişi olabilir.",
            lifestyle = "Sağlıklı kiloda kalmak karın derisi sağlığı için önemlidir. Kilo değişimleri cilt elastikiyetini etkileyebilir. Sıkı kıyafetler uzun süre giyildiğinde cilt tahrişine yol açabilir.",
            whenToSeeDoctor = "Karında uzun süredir iyileşmeyen yara, büyüyen lezyon veya katlantı bölgesinde tekrarlayan enfeksiyon varsa doktora başvurun."
        ),
        "Bacak" to RegionDetail(
            icon = "🦵",
            sunProtection = "Etek, şort veya mayo giyildiğinde bacaklar direkt UV alır. SPF 30+ kullanın ve özellikle diz altı ve baldır bölgesine dikkat edin. Güneş altında uzanırken bacakları unutmayın. Güneşten korumalı tayt ve çoraplar da etkili alternatiflerdir.",
            dailyCare = "Tıraş veya ağda sonrası cilt hassaslaşır — nazik nemlendirici kullanın. Bacak cildi çabuk kuruyabilir, özellikle kış aylarında yoğun nemlendirici şarttır. Hafif eksfoliyasyon (haftada 1) pürüzsüz cilt için önerilir. Uyluğun iç yüzünde sürtünme iltihabına karşı vücut pudrası veya anti-friction ürün kullanın.",
            selfExam = "Her iki bacağın ön, arka ve yan yüzlerini kontrol edin. Diz arkası ve kasık bölgesini de inceleyin — bu alanlar gözden kaçabilir. Bacaklarda çok sayıda ben varsa bunları takip edin. Yavaş büyüyen veya renk değiştiren lezyonlara dikkat edin.",
            lifestyle = "Uzun süre ayakta durma veya oturma bacak dolaşımını olumsuz etkileyebilir. Egzersiz ve bacakları yükseltmek kan dolaşımını iyileştirir. Varisi olanlar için kompresyon çorapları önerilir.",
            whenToSeeDoctor = "Bacakta yeni çıkan asimetrik leke, hızlı büyüyen ben, kaşınan veya kanayan lezyon varsa dermatologa gidin. Özellikle kadınlarda diz altı melanomlar geç fark edilebilir."
        ),
        "Ayak" to RegionDetail(
            icon = "🦶",
            sunProtection = "Ayak tabanı ve parmak aralarındaki lezyonlar çok nadir görülse de en tehlikeli melanom türü olan akral lentiginöz melanom bu bölgede gelişir. Plajda yürürken bile ayaklara SPF uygulayın. Kum üzerindeki UV yansıması beklenenden çok daha fazladır.",
            dailyCare = "Ayakları her gün sıcak suyla yıkayın ve özellikle parmak aralarını iyice kurulayın. Ayak tabanı ve topuklara yoğun nemlendirici veya çatlak kremi uygulayın. Tırnakları düzgün (düz) kesin. Nefes alabilen ayakkabı ve çorap tercih edin. Mantar enfeksiyonuna karşı kuru tutmak önemlidir.",
            selfExam = "Ayak tabanını düzenli olarak kontrol edin — bunu yapmak için el aynası kullanabilirsiniz. Tırnak altı ve tırnak yatağını inceleyin. Parmak aralarını ve topukları kontrol edin. Koyu kahverengi veya siyah çizgiler tırnak altında ciddi uyarı işaretidir.",
            lifestyle = "Kamuya açık alanlarda (havuz, sauna, spor salonu) mantar enfeksiyonunu önlemek için terlik kullanın. Dar ve sıkı ayakkabılar uzun süre giyilmemeli. Diabetik hastalar ayaklarını her gün kontrol etmelidir.",
            whenToSeeDoctor = "Tırnak altında koyu çizgi veya leke, ayak tabanında büyüyen asimetrik lezyon, iyileşmeyen yara veya siğil benzeri oluşum varsa mutlaka dermatologa gidin. Acral melanom geç tanındığında çok tehlikelidir."
        )
    )

    private val regionList = listOf(
        "Baş / Yüz", "Boyun", "Göğüs", "Kol / Ön kol",
        "El / Bilek", "Sırt", "Karın", "Bacak", "Ayak"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_sheet_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinnerRegion  = view.findViewById<Spinner>(R.id.spinnerRegion)
        val btnGetInfo     = view.findViewById<Button>(R.id.btnGetInfo)
        val btnDerma       = view.findViewById<Button>(R.id.btnDerma)
        val btnHospital    = view.findViewById<Button>(R.id.btnHospital)
        val infoContainer  = view.findViewById<LinearLayout>(R.id.infoContainer)
        val tvUrgency      = view.findViewById<TextView>(R.id.tvUrgency)
        val diagnosis      = arguments?.getString("diagnosis") ?: ""
        val defaultRegion  = arguments?.getString("region") ?: regionList[0]

        // Teşhis bilgisi başlık olarak göster
        tvUrgency.text = "📊 Tarama Sonucu: $diagnosis"
        tvUrgency.setTextColor(Color.parseColor("#C9A84C"))

        val regionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, regionList)
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRegion.adapter = regionAdapter
        spinnerRegion.setSelection(regionList.indexOf(defaultRegion).coerceAtLeast(0))

        // İlk açılışta otomatik göster
        showRegionInfo(defaultRegion, infoContainer, btnDerma, btnHospital)

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

        addCard(container, "☀️ Güneş Koruması", detail.sunProtection, "#FFD700")
        addCard(container, "🧴 Günlük Bakım", detail.dailyCare, "#C9A84C")
        addCard(container, "🔍 Nasıl Kontrol Edilir?", detail.selfExam, "#7B9AC9")
        addCard(container, "🏃 Yaşam Tarzı Önerileri", detail.lifestyle, "#AA88FF")
        addCard(container, "🏥 Ne Zaman Doktora Gidilmeli?", detail.whenToSeeDoctor, "#FF6B6B")

        // Uyarı kartı
        val dp = resources.displayMetrics.density
        val warnCard = androidx.cardview.widget.CardView(requireContext()).apply {
            radius = 12f * dp; cardElevation = 0f
            setCardBackgroundColor(Color.parseColor("#1A1225"))
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = (8*dp).toInt(); layoutParams = lp
        }
        val warnTv = TextView(requireContext()).apply {
            text = "⚕️  Bu bilgiler genel sağlık rehberi niteliğindedir. Tanı ve tedavi için mutlaka bir dermatologa başvurun."
            textSize = 11f; setTextColor(Color.parseColor("#7B7B9A"))
            setPadding((12*dp).toInt(), (10*dp).toInt(), (12*dp).toInt(), (10*dp).toInt())
            setLineSpacing(0f, 1.3f)
        }
        warnCard.addView(warnTv); container.addView(warnCard)

        btnDerma.setOnClickListener { openMaps("dermatoloji kliniği") }
        btnHospital.setOnClickListener { openMaps("hastane dermatoloji") }
    }

    private fun addCard(container: LinearLayout, title: String, content: String, color: String) {
        val dp = resources.displayMetrics.density
        val card = androidx.cardview.widget.CardView(requireContext()).apply {
            radius = 14f * dp; cardElevation = 3f
            setCardBackgroundColor(Color.parseColor("#1A1A32"))
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
            setTextColor(Color.parseColor("#CCCCDD"))
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