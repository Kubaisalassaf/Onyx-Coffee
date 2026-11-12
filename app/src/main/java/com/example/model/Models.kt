package com.example.model

import com.example.localization.AppLanguage

/**
 * ONYX Coffee & Equipment Marketplace
 * Comprehensive Data Schema with Multilingual Support (English, Arabic, Kurdish)
 */

enum class UserRole {
    CUSTOMER,
    ADMIN,
    BARISTA_STAFF,
    LOGISTICS_MANAGER
}

enum class LoyaltyTier(
    val minPoints: Int,
    val discountRate: Double,
    val badgeLabelEn: String,
    val badgeLabelAr: String,
    val badgeLabelKu: String
) {
    BRONZE(0, 0.0, "Bronze Barista", "باريستا برونزي", "باریستای برۆنزی"),
    SILVER(500, 0.05, "Silver Connoisseur", "خبير فضي", "شارەزای زیوی"),
    GOLD(1500, 0.10, "Gold Master", "أستاذ ذهبي", "ماستەری زێڕین"),
    ONYX_VIP(3000, 0.15, "ONYX Black VIP", "عضوية أونيكس السوداء", "ئەندامێتی ڕەشی ئۆنیکس VIP");

    fun localizedBadge(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> badgeLabelKu
        AppLanguage.ARABIC -> badgeLabelAr
        AppLanguage.ENGLISH -> badgeLabelEn
    }
}

data class DeliveryAddress(
    val id: String,
    val title: String, // e.g. "Home", "Cafe", "Office"
    val governorate: IraqiGovernorate,
    val cityOrDistrict: String,
    val streetAddress: String,
    val recipientName: String,
    val phone: String,
    val deliveryNotes: String = "",
    val isDefault: Boolean = false
)

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole = UserRole.CUSTOMER,
    val addresses: List<DeliveryAddress> = emptyList(),
    val loyaltyPoints: Int = 240,
    val loyaltyTier: LoyaltyTier = LoyaltyTier.BRONZE,
    val favoriteProductIds: Set<String> = emptySet(),
    val createdAtEpoch: Long = System.currentTimeMillis()
)

enum class IraqiGovernorate(
    val nameEn: String,
    val nameAr: String,
    val nameKu: String,
    val standardDeliveryFeeIqd: Long,
    val estimatedHours: String
) {
    BAGHDAD("Baghdad", "بغداد", "بەغدا", 5000, "24-48 hrs"),
    ERBIL("Erbil", "أربيل", "هەولێر", 6000, "24-48 hrs"),
    BASRA("Basra", "البصرة", "بەسرە", 7000, "48-72 hrs"),
    SULAYMANIYAH("Sulaymaniyah", "السليمانية", "سلێمانی", 6000, "24-48 hrs"),
    DUHOK("Duhok", "دهوك", "دهۆک", 6000, "48 hrs"),
    NAJAF("Najaf", "النجف الأشرف", "نەجەف", 6000, "24-48 hrs"),
    KARBALA("Karbala", "كربلاء المقدسة", "کەربەلا", 6000, "24-48 hrs"),
    BABYLON("Babylon", "بابل", "بابل", 6000, "24-48 hrs"),
    KIRKUK("Kirkuk", "كركوك", "کەرکووک", 6000, "24-48 hrs"),
    NINEVEH("Nineveh (Mosul)", "نينوى (الموصل)", "نەینەوا (موسڵ)", 7000, "48-72 hrs"),
    ANBAR("Anbar", "الأنبار", "ئەنبار", 7000, "48-72 hrs"),
    DIYALA("Diyala", "ديالى", "دیالە", 6000, "24-48 hrs"),
    WASIT("Wasit", "واسط", "واست", 6500, "48 hrs"),
    MAYSAN("Maysan", "ميسان", "میسان", 7000, "48-72 hrs"),
    DHI_QAR("Dhi Qar (Nasiriyah)", "ذي قار (الناصرية)", "زیقار (ناسیریە)", 7000, "48-72 hrs"),
    MUTHANNA("Muthanna (Samawah)", "المثنى (السماوة)", "موسەننا (سەماوە)", 7000, "48-72 hrs"),
    QADISIYYAH("Al-Qadisiyyah (Diwaniyah)", "القادسية (الديوانية)", "قادسیە (دیوانیە)", 6500, "48 hrs"),
    SALADIN("Saladin (Tikrit)", "صلاح الدين (تكريت)", "سەڵاحەدین (تکریت)", 7000, "48-72 hrs");

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> nameKu
        AppLanguage.ARABIC -> nameAr
        AppLanguage.ENGLISH -> nameEn
    }
}

enum class ProductCategory(
    val labelEn: String,
    val labelAr: String,
    val labelKu: String
) {
    ALL("All Products", "جميع المنتجات", "هەموو بەرهەمەکان"),
    COFFEE("Coffee Beans", "حبوب القهوة", "دەنکە قاوە"),
    MACHINES("Coffee Machines", "مكائن القهوة", "ئامێرەکانی قاوە"),
    EQUIPMENT("Brewing Equipment", "معدات التحضير", "کەلوپەلی ئامادەکردن"),
    ACCESSORIES("Accessories", "إكسسوارات ومستلزمات", "پێداویستییەکان");

    fun localizedLabel(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> labelKu
        AppLanguage.ARABIC -> labelAr
        AppLanguage.ENGLISH -> labelEn
    }
}

enum class RoastLevel(val labelEn: String, val labelAr: String, val labelKu: String = "") {
    LIGHT("Light Roast", "تحميص فاتح", "برژاندنی کاڵ"),
    MEDIUM_LIGHT("Medium-Light", "متوسط - فاتح", "مامناوەند - کاڵ"),
    MEDIUM("Medium Roast", "تحميص متوسط", "برژاندنی مامناوەند"),
    MEDIUM_DARK("Medium-Dark", "متوسط - غامق", "مامناوەند - تۆخ"),
    DARK("Dark Roast (Espresso)", "تحميص غامق", "برژاندنی تۆخ (ئێسپرێسۆ)")
}

enum class BeanType(val labelEn: String, val labelAr: String, val labelKu: String = "") {
    ARABICA("100% Arabica", "100% أرابيكا", "100% عەرەبیکا"),
    ROBUSTA("Fine Robusta", "روبوستا فاخرة", "ڕۆبۆستای نایاب"),
    BLEND("Signature Blend", "خلطة أونيكس الخاصة", "تێکەڵەی تایبەتی ئۆنیکس"),
    GREEN_BEAN("Green (Unroasted)", "بن أخضر غير محمص", "قاوەی سەوزی برژێنراو")
}

enum class BrewMethod(val labelEn: String, val labelAr: String, val labelKu: String = "") {
    ESPRESSO("Espresso", "إسبريسو", "ئێسپرێسۆ"),
    V60("V60 Pour-Over", "تقطير V60", "پاڵاوتنی V60"),
    CHEMEX("Chemex", "كيمكس", "کێمێکس"),
    AEROPRESS("AeroPress", "ايروبرس", "ئایرۆپرێس"),
    FRENCH_PRESS("French Press", "فرينش برس", "فرێنچ پرێس"),
    COLD_BREW("Cold Brew", "كولد برو", "کۆڵد برو"),
    TURKISH("Turkish Ibrik", "قهوة تركية", "قاوەی تورکی"),
    COMMERCIAL("Commercial Barista", "باريستا تجاري", "باریستای پیشەیی")
}

data class CoffeeSpecs(
    val originCountry: String,
    val originCountryAr: String,
    val originCountryKu: String = "",
    val region: String,
    val farmOrProducer: String = "",
    val altitudeMeters: String = "1800m - 2200m",
    val variety: String,
    val processingMethod: String,
    val roastLevel: RoastLevel,
    val beanType: BeanType,
    val flavorProfile: List<String>,
    val flavorProfileAr: List<String>,
    val flavorProfileKu: List<String> = emptyList(),
    val acidityRating: Int = 4, // 1 to 5
    val bodyRating: Int = 4,    // 1 to 5
    val bitternessRating: Int = 2, // 1 to 5: 1 = mild/sweet, 5 = bold/intense
    val aromaDescription: String,
    val recommendedBrew: BrewMethod,
    val defaultPackageGram: Int = 250,
    val availablePackagesGram: List<Int> = listOf(250, 500, 1000)
)

data class EquipmentSpecs(
    val manufacturer: String,
    val modelNumber: String,
    val powerWatts: Int,
    val pumpPressureBar: Int = 15,
    val boilerCapacityLiters: Double = 2.0,
    val boilerType: String = "Dual Boiler",
    val dimensionsCm: String = "38 x 42 x 40 cm",
    val warrantyMonths: Int = 24,
    val voltage: String = "220-240V / 50Hz (Iraq Standard)",
    val sparePartsAvailable: Boolean = true
)

data class ExchangeRateConfig(
    val rateIqdPerUsd: Double = 1500.0, // e.g. 1 USD = 1,500 IQD
    val autoCalculateFromUsd: Boolean = true,
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
) {
    fun convertUsdToIqd(usd: Double): Long {
        val raw = usd * rateIqdPerUsd
        return (Math.round(raw / 250.0) * 250)
    }
}

data class Product(
    val id: String,
    val sku: String,
    val nameEn: String,
    val nameAr: String,
    val nameKu: String = "",
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionKu: String = "",
    val category: ProductCategory,
    val subCategory: String,
    val brand: String,
    val regularPriceIqd: Long,
    val promoPriceIqd: Long? = null,
    val basePriceUsd: Double? = null,
    val stockQuantity: Int,
    val imageResId: Int? = null,
    val imageUrl: String? = null,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val rating: Double = 4.9,
    val reviewCount: Int = 28,
    val coffeeSpecs: CoffeeSpecs? = null,
    val equipmentSpecs: EquipmentSpecs? = null
) {
    val activePriceIqd: Long
        get() = promoPriceIqd ?: regularPriceIqd

    val hasDiscount: Boolean
        get() = promoPriceIqd != null && promoPriceIqd < regularPriceIqd

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> if (nameKu.isNotBlank()) nameKu else nameAr
        AppLanguage.ARABIC -> nameAr
        AppLanguage.ENGLISH -> nameEn
    }

    fun localizedDescription(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> if (descriptionKu.isNotBlank()) descriptionKu else descriptionAr
        AppLanguage.ARABIC -> descriptionAr
        AppLanguage.ENGLISH -> descriptionEn
    }
}

data class CartItem(
    val product: Product,
    val selectedPackageGram: Int = 250,
    val quantity: Int = 1
) {
    val unitPriceIqd: Long
        get() {
            val base = product.activePriceIqd
            return when (selectedPackageGram) {
                500 -> (base * 1.9).toLong()
                1000 -> (base * 3.6).toLong()
                else -> base
            }
        }

    val totalPriceIqd: Long
        get() = unitPriceIqd * quantity
}

enum class PaymentProvider(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val nameKu: String,
    val isOnline: Boolean,
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionKu: String
) {
    FIB(
        id = "fib",
        nameEn = "First Iraqi Bank (FIB)",
        nameAr = "المصرف العراقي الأول (FIB)",
        nameKu = "بانکی یەکەمی عێراقی (FIB)",
        isOnline = true,
        descriptionEn = "Scan QR or instant pay via FIB mobile application",
        descriptionAr = "مسح رمز QR أو الدفع المباشر عبر تطبيق FIB",
        descriptionKu = "سکانی کۆدی QR یان پارەدانی خێرا لە ڕێگەی ئەپی FIB"
    ),
    FASTPAY(
        id = "fastpay",
        nameEn = "FastPay",
        nameAr = "فاست باي",
        nameKu = "فاست پای (FastPay)",
        isOnline = true,
        descriptionEn = "Pay with your FastPay mobile wallet & SMS OTP",
        descriptionAr = "الدفع بواسطة محفظة فاست باي ورسالة التأكيد",
        descriptionKu = "پارەدان لە ڕێگەی جزدانی فاست پای و کۆدی OTP"
    ),
    QI_CARD(
        id = "qi_card",
        nameEn = "Qi Card / Master Card Iraq",
        nameAr = "كي كارد / ماستركارد العراق",
        nameKu = "کی کارت / ماستەرکارتی عێراق",
        isOnline = true,
        descriptionEn = "Iraqi National Qi Card & local banking cards",
        descriptionAr = "بطاقة كي كارد الوطنية والبطاقات المصرفية المحلية",
        descriptionKu = "کارتی نیشتمانیی کی کارت و کارتە بانکییە ناوخۆییەکان"
    ),
    ZAIN_CASH(
        id = "zain_cash",
        nameEn = "Zain Cash",
        nameAr = "زين كاش",
        nameKu = "زەین کاش (Zain Cash)",
        isOnline = true,
        descriptionEn = "Instant payment through Zain Cash wallet & PIN",
        descriptionAr = "الدفع الفوري عبر محفظة زين كاش والرمز السري",
        descriptionKu = "پارەدانی ڕاستەوخۆ لە ڕێگەی جزدانی زەین کاش و کۆدی PIN"
    ),
    COD(
        id = "cod",
        nameEn = "Cash on Delivery (COD)",
        nameAr = "الدفع نقداً عند الاستلام",
        nameKu = "پارەدان لە کاتی وەرگرتندا (نەختینە)",
        isOnline = false,
        descriptionEn = "Pay in Iraqi Dinars (IQD) upon package arrival",
        descriptionAr = "ادفع نقداً بالدينار العراقي عند وصول مندوب التوصيل",
        descriptionKu = "پارەدان بە دیناری عێراقی لە کاتی گەیشتنی پاکێجەکە"
    );

    fun localizedName(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> nameKu
        AppLanguage.ARABIC -> nameAr
        AppLanguage.ENGLISH -> nameEn
    }

    fun localizedDescription(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> descriptionKu
        AppLanguage.ARABIC -> descriptionAr
        AppLanguage.ENGLISH -> descriptionEn
    }
}

enum class OrderStatus(
    val code: String,
    val labelEn: String,
    val labelAr: String,
    val labelKu: String,
    val stepIndex: Int
) {
    PENDING("pending", "Pending", "قيد الانتظار", "چاوەڕوانکراو", 1),
    CONFIRMED("confirmed", "Confirmed", "تم التأكيد", "پشتڕاستکرایەوە", 2),
    PROCESSING("processing", "Processing (Roasting/Packing)", "قيد التحضير والتحميص", "لە قۆناغی ئامادەکردندا", 3),
    READY_FOR_DISPATCH("ready", "Ready for Dispatch", "جاهز للشحن", "ئامادەیە بۆ ناردن", 4),
    SHIPPED("shipped", "Shipped with Courier", "تم الشحن مع المندوب", "نێردرا لەگەڵ گەیەنەر", 5),
    DELIVERED("delivered", "Delivered", "تم التوصيل بنجاح", "بە سەرکەوتوویی گەیشت", 6),
    CANCELLED("cancelled", "Cancelled", "ملغي", "هەڵوەشێنراوە", 0),
    RETURNED("returned", "Returned", "مرتجع", "گەڕێندراوە", 0);

    fun localizedLabel(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> labelKu
        AppLanguage.ARABIC -> labelAr
        AppLanguage.ENGLISH -> labelEn
    }
}

enum class PaymentStatus(val labelEn: String, val labelAr: String, val labelKu: String) {
    PENDING("Payment Pending", "بانتظار الدفع", "چاوەڕوانی پارەدان"),
    PAID("Paid Successfully", "تم الدفع بنجاح", "پارەدرا بە سەرکەوتوویی"),
    FAILED("Payment Failed", "فشلت عملية الدفع", "پارەدان سەرکەوتوو نەبوو"),
    REFUNDED("Refunded", "تمت استعادة المبلغ", "بڕی پارەکە گەڕێندرایەوە");

    fun localizedLabel(language: AppLanguage): String = when (language) {
        AppLanguage.KURDISH -> labelKu
        AppLanguage.ARABIC -> labelAr
        AppLanguage.ENGLISH -> labelEn
    }
}

data class Order(
    val id: String,
    val orderNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: DeliveryAddress,
    val items: List<CartItem>,
    val subtotalIqd: Long,
    val deliveryFeeIqd: Long,
    val discountIqd: Long = 0,
    val finalTotalIqd: Long,
    val paymentProvider: PaymentProvider,
    val paymentStatus: PaymentStatus,
    val orderStatus: OrderStatus,
    val trackingNumber: String,
    val transactionRef: String = "",
    val createdAtEpoch: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class Coupon(
    val code: String,
    val discountPercent: Int,
    val minOrderIqd: Long,
    val active: Boolean = true,
    val descriptionEn: String,
    val descriptionAr: String,
    val descriptionKu: String = ""
)

data class JournalArticle(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val titleKu: String = "",
    val excerptEn: String,
    val excerptAr: String,
    val excerptKu: String = "",
    val contentEn: String,
    val contentAr: String,
    val contentKu: String = "",
    val author: String,
    val category: String,
    val readMinutes: Int,
    val dateString: String
)

data class StoreSettings(
    val adminWhatsAppNumber: String = "+9647501234567",
    val autoSendWhatsAppOnOrder: Boolean = true,
    val storeName: String = "ONYX Coffee Roasters Iraq"
)
