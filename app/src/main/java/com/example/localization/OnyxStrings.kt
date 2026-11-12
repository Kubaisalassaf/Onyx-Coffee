package com.example.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import java.text.NumberFormat
import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val shortName: String,
    val layoutDirection: LayoutDirection
) {
    ENGLISH("en", "English", "EN", LayoutDirection.Ltr),
    ARABIC("ar", "العربية", "عربي", LayoutDirection.Rtl),
    KURDISH("ku", "کوردی", "کوردی", LayoutDirection.Rtl)
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.ENGLISH }

object OnyxFormatter {
    fun formatIqd(amount: Long, language: AppLanguage): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        val formattedNumber = formatter.format(amount)
        return when (language) {
            AppLanguage.KURDISH -> "$formattedNumber دینار"
            AppLanguage.ARABIC -> "$formattedNumber د.ع"
            AppLanguage.ENGLISH -> "$formattedNumber IQD"
        }
    }
}

class OnyxStrings(private val lang: AppLanguage) {
    val isArabic: Boolean get() = lang == AppLanguage.ARABIC
    val isKurdish: Boolean get() = lang == AppLanguage.KURDISH
    val isRtl: Boolean get() = lang.layoutDirection == LayoutDirection.Rtl

    // Brand & Navigation
    val brandName: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئۆنیکس کۆفی"
        AppLanguage.ARABIC -> "أونيكس كوفي"
        AppLanguage.ENGLISH -> "ONYX Coffee"
    }
    val brandTagline: String get() = when (lang) {
        AppLanguage.KURDISH -> "بازاڕی قاوەی تایبەت و ئامێرە پیشەییەکان لە عێراق و کوردستان"
        AppLanguage.ARABIC -> "سوق القهوة المختصة والمعدات الاحترافية في العراق"
        AppLanguage.ENGLISH -> "Specialty Coffee & Professional Equipment Marketplace – Iraq"
    }
    val navHome: String get() = when (lang) {
        AppLanguage.KURDISH -> "سەرەکی"
        AppLanguage.ARABIC -> "الرئيسية"
        AppLanguage.ENGLISH -> "Home"
    }
    val navShop: String get() = when (lang) {
        AppLanguage.KURDISH -> "فرۆشگا"
        AppLanguage.ARABIC -> "المتجر"
        AppLanguage.ENGLISH -> "Shop"
    }
    val navJournal: String get() = when (lang) {
        AppLanguage.KURDISH -> "گۆڤار"
        AppLanguage.ARABIC -> "المجلة"
        AppLanguage.ENGLISH -> "Journal"
    }
    val navCart: String get() = when (lang) {
        AppLanguage.KURDISH -> "سەبەتە"
        AppLanguage.ARABIC -> "السلة"
        AppLanguage.ENGLISH -> "Cart"
    }
    val navAccount: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەژمارەکەم"
        AppLanguage.ARABIC -> "حسابي"
        AppLanguage.ENGLISH -> "Account"
    }
    val navAdmin: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەڕێوەبردن"
        AppLanguage.ARABIC -> "لوحة الإدارة"
        AppLanguage.ENGLISH -> "Admin"
    }

    // Hero Section
    val heroTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئەزموونێکی ناوازە لە قاوەی بێ وێنە"
        AppLanguage.ARABIC -> "تجربة قهوة فاخرة لا مثيل لها"
        AppLanguage.ENGLISH -> "Uncompromising Coffee Excellence"
    }
    val heroSubtitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "دەستکەوتنی باشترین جۆرەکانی قاوە، ئامێرەکانی ئێسپرێسۆ، و کەلوپەلی باریستا لەگەڵ گەیاندن بۆ هەموو پارێزگاکانی عێراق و هەرێمی کوردستان."
        AppLanguage.ARABIC -> "استكشف أندر محاصيل البن المختص، ومكائن الإسبريسو الاحترافية، ومعدات الباريستا مع توصيل لجميع محافظات العراق."
        AppLanguage.ENGLISH -> "Explore rare single-origin coffees, commercial espresso machines, and precision barista gear delivered across Iraq."
    }
    val heroShopBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "گەڕان لە کۆمەڵەکان"
        AppLanguage.ARABIC -> "تسوق المجموعة"
        AppLanguage.ENGLISH -> "Explore Collection"
    }
    val heroEquipmentBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئامێرەکانی ئێسپرێسۆ"
        AppLanguage.ARABIC -> "مكائن الإسبريسو"
        AppLanguage.ENGLISH -> "Espresso Gear"
    }

    // Badges & Labels
    val bestSeller: String get() = when (lang) {
        AppLanguage.KURDISH -> "پڕفرۆشترین"
        AppLanguage.ARABIC -> "الأكثر مبيعاً"
        AppLanguage.ENGLISH -> "Best Seller"
    }
    val newArrival: String get() = when (lang) {
        AppLanguage.KURDISH -> "تازە گەیشتوو"
        AppLanguage.ARABIC -> "وصل حديثاً"
        AppLanguage.ENGLISH -> "New Arrival"
    }
    val featured: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەڵبژێردراو"
        AppLanguage.ARABIC -> "مختار بعناية"
        AppLanguage.ENGLISH -> "Curated Selection"
    }
    val inStock: String get() = when (lang) {
        AppLanguage.KURDISH -> "لە کۆگادا هەیە"
        AppLanguage.ARABIC -> "متوفر بالمخزون"
        AppLanguage.ENGLISH -> "In Stock"
    }
    val lowStock: String get() = when (lang) {
        AppLanguage.KURDISH -> "بڕی کەم ماوە"
        AppLanguage.ARABIC -> "كمية محدودة"
        AppLanguage.ENGLISH -> "Low Stock"
    }
    val outOfStock: String get() = when (lang) {
        AppLanguage.KURDISH -> "تەواو بووە"
        AppLanguage.ARABIC -> "نفد من المخزون"
        AppLanguage.ENGLISH -> "Out of Stock"
    }

    // Product Specifications
    val origin: String get() = when (lang) {
        AppLanguage.KURDISH -> "وڵاتی سەرچاوە"
        AppLanguage.ARABIC -> "بلد المنشأ"
        AppLanguage.ENGLISH -> "Origin"
    }
    val roastLevel: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئاستی برژاندن"
        AppLanguage.ARABIC -> "درجة التحميص"
        AppLanguage.ENGLISH -> "Roast Level"
    }
    val beanType: String get() = when (lang) {
        AppLanguage.KURDISH -> "جۆری دەنک"
        AppLanguage.ARABIC -> "نوع البن"
        AppLanguage.ENGLISH -> "Bean Variety"
    }
    val brewMethod: String get() = when (lang) {
        AppLanguage.KURDISH -> "شێوازی ئامادەکردن"
        AppLanguage.ARABIC -> "طريقة التحضير المقترحة"
        AppLanguage.ENGLISH -> "Brewing Method"
    }
    val flavorProfile: String get() = when (lang) {
        AppLanguage.KURDISH -> "تامی و بۆن"
        AppLanguage.ARABIC -> "الإيحاءات والنكهات"
        AppLanguage.ENGLISH -> "Tasting Notes"
    }
    val acidity: String get() = when (lang) {
        AppLanguage.KURDISH -> "ترشیاتی (Acidity)"
        AppLanguage.ARABIC -> "الحمضية"
        AppLanguage.ENGLISH -> "Acidity"
    }
    val bitterness: String get() = when (lang) {
        AppLanguage.KURDISH -> "تاڵی (Bitterness)"
        AppLanguage.ARABIC -> "المرارة"
        AppLanguage.ENGLISH -> "Bitterness"
    }
    val body: String get() = when (lang) {
        AppLanguage.KURDISH -> "قورسایی (Body)"
        AppLanguage.ARABIC -> "القوام"
        AppLanguage.ENGLISH -> "Body"
    }
    val selectSize: String get() = when (lang) {
        AppLanguage.KURDISH -> "دیاریکردنی کێشی پاکێج"
        AppLanguage.ARABIC -> "اختر وزن العبوة"
        AppLanguage.ENGLISH -> "Select Package Size"
    }
    val addToCart: String get() = when (lang) {
        AppLanguage.KURDISH -> "زیادکردن بۆ سەبەتە"
        AppLanguage.ARABIC -> "إضافة إلى السلة"
        AppLanguage.ENGLISH -> "Add to Cart"
    }
    val buyNow: String get() = when (lang) {
        AppLanguage.KURDISH -> "کڕینی خێرا"
        AppLanguage.ARABIC -> "شراء فوري"
        AppLanguage.ENGLISH -> "Buy Now"
    }

    // Cart & Checkout
    val shoppingCart: String get() = when (lang) {
        AppLanguage.KURDISH -> "سەبەتەی کڕین"
        AppLanguage.ARABIC -> "حقيبة التسوق"
        AppLanguage.ENGLISH -> "Shopping Cart"
    }
    val cartEmpty: String get() = when (lang) {
        AppLanguage.KURDISH -> "سەبەتەکەت ئێستا بەتاڵە"
        AppLanguage.ARABIC -> "سلة التسوق فارغة حالياً"
        AppLanguage.ENGLISH -> "Your cart is empty"
    }
    val cartEmptyTip: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەندێک قاوەی تایبەت یان ئامێری ئامادەکردن زیاد بکە بۆ دەستپێکردن."
        AppLanguage.ARABIC -> "أضف بعض المحاصيل الفاخرة أو معدات التحضير لتبدأ رحلتك"
        AppLanguage.ENGLISH -> "Discover our single-origin coffees and precision equipment to begin."
    }
    val subtotal: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆی بەشەکی"
        AppLanguage.ARABIC -> "المجموع الفرعي"
        AppLanguage.ENGLISH -> "Subtotal"
    }
    val deliveryFee: String get() = when (lang) {
        AppLanguage.KURDISH -> "تێچووی گەیاندن"
        AppLanguage.ARABIC -> "أجور التوصيل"
        AppLanguage.ENGLISH -> "Delivery Fee"
    }
    val freeDelivery: String get() = when (lang) {
        AppLanguage.KURDISH -> "گەیاندنی بێبەرامبەر (داواکاری لە 100,000 دینار زیاتر)"
        AppLanguage.ARABIC -> "توصيل مجاني (طلب أكثر من 100,000 د.ع)"
        AppLanguage.ENGLISH -> "Free Delivery (Orders over 100,000 IQD)"
    }
    val discount: String get() = when (lang) {
        AppLanguage.KURDISH -> "داشکاندن"
        AppLanguage.ARABIC -> "الخصم"
        AppLanguage.ENGLISH -> "Discount"
    }
    val total: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆی گشتی"
        AppLanguage.ARABIC -> "المجموع الكلي"
        AppLanguage.ENGLISH -> "Final Total"
    }
    val promoPlaceholder: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆدی داشکاندن (نموونە: BAGHDAD10)"
        AppLanguage.ARABIC -> "رمز الكوبون (مثال: BAGHDAD10)"
        AppLanguage.ENGLISH -> "Coupon code (e.g. BAGHDAD10)"
    }
    val applyCoupon: String get() = when (lang) {
        AppLanguage.KURDISH -> "جێبەجێکردن"
        AppLanguage.ARABIC -> "تطبيق"
        AppLanguage.ENGLISH -> "Apply"
    }
    val proceedToCheckout: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەردەوامبوون بۆ تەواوکردنی داواکاری"
        AppLanguage.ARABIC -> "المتابعة لإتمام الطلب"
        AppLanguage.ENGLISH -> "Proceed to Checkout"
    }

    // Checkout Details
    val checkoutTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "تەواوکردنی داواکاری و گەیاندن"
        AppLanguage.ARABIC -> "إتمام الطلب والشحن"
        AppLanguage.ENGLISH -> "Checkout & Shipping"
    }
    val recipientInfo: String get() = when (lang) {
        AppLanguage.KURDISH -> "زانیاری وەرگر"
        AppLanguage.ARABIC -> "بيانات المستلم"
        AppLanguage.ENGLISH -> "Recipient Information"
    }
    val recipientName: String get() = when (lang) {
        AppLanguage.KURDISH -> "ناوی تەواو"
        AppLanguage.ARABIC -> "الاسم الكامل"
        AppLanguage.ENGLISH -> "Full Name"
    }
    val phonePlaceholder: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی مۆبایل (نموونە: 0750 123 4567)"
        AppLanguage.ARABIC -> "رقم الهاتف العراقي (مثال: 0770 123 4567)"
        AppLanguage.ENGLISH -> "Phone number (e.g. 0750 123 4567)"
    }
    val governorateSelect: String get() = when (lang) {
        AppLanguage.KURDISH -> "پارێزگا"
        AppLanguage.ARABIC -> "المحافظة"
        AppLanguage.ENGLISH -> "Governorate"
    }
    val cityStreet: String get() = when (lang) {
        AppLanguage.KURDISH -> "ناوچە / شەقام / نزیکترین شوێن"
        AppLanguage.ARABIC -> "المنطقة / الشارع / أقرب نقطة دالة"
        AppLanguage.ENGLISH -> "District / Street / Landmark"
    }
    val paymentMethod: String get() = when (lang) {
        AppLanguage.KURDISH -> "شێوازی پارەدان"
        AppLanguage.ARABIC -> "طريقة الدفع"
        AppLanguage.ENGLISH -> "Payment Method"
    }
    val placeOrder: String get() = when (lang) {
        AppLanguage.KURDISH -> "پشتڕاستکردنەوەی داواکاری"
        AppLanguage.ARABIC -> "تأكيد الطلب الآن"
        AppLanguage.ENGLISH -> "Confirm & Place Order"
    }
    val sandboxNotice: String get() = when (lang) {
        AppLanguage.KURDISH -> "دەروازەی پارەدانی ئەلیکترۆنی پارێزراوی عێراقی"
        AppLanguage.ARABIC -> "بوابة دفع إلكترونية آمنة تجريبية (جاهزة للربط الفعلي)"
        AppLanguage.ENGLISH -> "Secure Iraqi Payment Gateway (Sandbox/Demo Ready)"
    }

    // Order Tracking & Success
    val orderConfirmedTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "داواکارییەکەت بە سەرکەوتوویی وەرگیرا!"
        AppLanguage.ARABIC -> "تم استلام طلبك بنجاح!"
        AppLanguage.ENGLISH -> "Order Received Successfully!"
    }
    val orderNumberLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی داواکاری:"
        AppLanguage.ARABIC -> "رقم الطلب:"
        AppLanguage.ENGLISH -> "Order Number:"
    }
    val trackingNumberLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆدی بەدواداچوون:"
        AppLanguage.ARABIC -> "رقم التتبع:"
        AppLanguage.ENGLISH -> "Tracking Ref:"
    }
    val estimatedArrival: String get() = when (lang) {
        AppLanguage.KURDISH -> "کاتی گەیشتنی پێشبینیکراو:"
        AppLanguage.ARABIC -> "وقت التوصيل المتوقع:"
        AppLanguage.ENGLISH -> "Estimated Delivery:"
    }
    val trackOrderStatus: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەدواداچوونی دۆخی بارکردن"
        AppLanguage.ARABIC -> "متابعة حالة الشحنة"
        AppLanguage.ENGLISH -> "Track Order Status"
    }

    // Loyalty Program
    val onyxRewards: String get() = when (lang) {
        AppLanguage.KURDISH -> "یانەی خەڵاتەکانی ئۆنیکس"
        AppLanguage.ARABIC -> "برنامج مكافآت أونيكس"
        AppLanguage.ENGLISH -> "ONYX Rewards Club"
    }
    val yourPoints: String get() = when (lang) {
        AppLanguage.KURDISH -> "باڵانسی خاڵەکانت"
        AppLanguage.ARABIC -> "رصيد نقاطك"
        AppLanguage.ENGLISH -> "Your Points Balance"
    }
    val tierLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئاستی ئێستا"
        AppLanguage.ARABIC -> "المستوى الحالي"
        AppLanguage.ENGLISH -> "Current Tier"
    }
    val nextTierTip: String get() = when (lang) {
        AppLanguage.KURDISH -> "500 خاڵی تر کۆبکەرەوە بۆ بەدەستهێنانی ئاستی زیوی"
        AppLanguage.ARABIC -> "اجمع 500 نقطة إضافية للترقية إلى خبير فضي"
        AppLanguage.ENGLISH -> "Earn 500 more points to reach Silver Connoisseur"
    }

    // Admin Dashboard
    val adminTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "ناوەندی بەڕێوەبردنی ئۆنیکس - عێراق و کوردستان"
        AppLanguage.ARABIC -> "لوحة إدارة المتجر - العراق"
        AppLanguage.ENGLISH -> "ONYX Admin Central - Iraq"
    }
    val totalRevenue: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆی گشتی داهات"
        AppLanguage.ARABIC -> "إجمالي المبيعات"
        AppLanguage.ENGLISH -> "Total Revenue"
    }
    val totalOrders: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆی داواکارییەکان"
        AppLanguage.ARABIC -> "الطلبات الكلية"
        AppLanguage.ENGLISH -> "Total Orders"
    }
    val pendingOrders: String get() = when (lang) {
        AppLanguage.KURDISH -> "داواکارییە چاوەڕوانکراوەکان"
        AppLanguage.ARABIC -> "طلبات قيد الانتظار"
        AppLanguage.ENGLISH -> "Pending Orders"
    }
    val activeCustomers: String get() = when (lang) {
        AppLanguage.KURDISH -> "کڕیارە چالاکەکان"
        AppLanguage.ARABIC -> "العملاء النشطون"
        AppLanguage.ENGLISH -> "Active Customers"
    }
    val tabOverview: String get() = when (lang) {
        AppLanguage.KURDISH -> "پوختە"
        AppLanguage.ARABIC -> "نظرة عامة"
        AppLanguage.ENGLISH -> "Overview"
    }
    val tabProducts: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەرهەم و کۆگا"
        AppLanguage.ARABIC -> "المنتجات والمخزون"
        AppLanguage.ENGLISH -> "Products & Stock"
    }
    val tabOrders: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەڕێوەبردنی داواکارییەکان"
        AppLanguage.ARABIC -> "إدارة الطلبات"
        AppLanguage.ENGLISH -> "Orders Manager"
    }
    val tabCustomers: String get() = when (lang) {
        AppLanguage.KURDISH -> "کڕیاران و خەڵاتەکان"
        AppLanguage.ARABIC -> "العملاء والمكافآت"
        AppLanguage.ENGLISH -> "Clients & Loyalty"
    }
    val tabLogistics: String get() = when (lang) {
        AppLanguage.KURDISH -> "گەیاندن و تێچوو"
        AppLanguage.ARABIC -> "الشحن والتوصيل"
        AppLanguage.ENGLISH -> "Logistics & Fees"
    }
    val addNewProduct: String get() = when (lang) {
        AppLanguage.KURDISH -> "+ زیادکردنی بەرهەمی نوێ"
        AppLanguage.ARABIC -> "+ إضافة منتج جديد"
        AppLanguage.ENGLISH -> "+ Add New Product"
    }
    val updateStatus: String get() = when (lang) {
        AppLanguage.KURDISH -> "نوێکردنەوەی دۆخ"
        AppLanguage.ARABIC -> "تحديث الحالة"
        AppLanguage.ENGLISH -> "Update Status"
    }
    val filterAll: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەموو"
        AppLanguage.ARABIC -> "الكل"
        AppLanguage.ENGLISH -> "All"
    }

    // Clients & Loyalty CRM Strings
    val customersHeader: String get() = when (lang) {
        AppLanguage.KURDISH -> "تۆماری کڕیاران و بەڕێوەبردنی خاڵەکانی وەفاداری"
        AppLanguage.ARABIC -> "سجل العملاء وإدارة برامج المكافآت والولاء"
        AppLanguage.ENGLISH -> "Client Directory & Loyalty CRM"
    }
    val searchClientsHint: String get() = when (lang) {
        AppLanguage.KURDISH -> "گەڕان بە ناوی کڕیار، مۆبایل، یان ئیمەیڵ..."
        AppLanguage.ARABIC -> "بحث باسم العميل، الهاتف، أو البريد..."
        AppLanguage.ENGLISH -> "Search by client name, phone, or email..."
    }
    val clientOrderHistory: String get() = when (lang) {
        AppLanguage.KURDISH -> "مێژووی کڕینەکان"
        AppLanguage.ARABIC -> "سجل المشتريات"
        AppLanguage.ENGLISH -> "Order History"
    }
    val grantBonusBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەخشینی خاڵی پاداشت"
        AppLanguage.ARABIC -> "منح مكافأة / نقاط"
        AppLanguage.ENGLISH -> "Grant Bonus Points"
    }
    val bonusDialogTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەخشینی خاڵ و بەرزکردنەوەی ئاستی کڕیار"
        AppLanguage.ARABIC -> "منح نقاط مكافأة وترقية العميل"
        AppLanguage.ENGLISH -> "Grant Loyalty Bonus & Upgrade Tier"
    }
    val bonusPointsLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی خاڵەکانی پاداشت"
        AppLanguage.ARABIC -> "عدد نقاط المكافأة الممنوحة"
        AppLanguage.ENGLISH -> "Bonus Points Amount"
    }
    val bonusReasonLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "هۆکاری پاداشت / تێبینی (ئارەزوومەندانە)"
        AppLanguage.ARABIC -> "سبب المكافأة / ملاحظة (اختياري)"
        AppLanguage.ENGLISH -> "Bonus Reason / Promotion Note"
    }
    val confirmBonusBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "پشتڕاستکردنەوەی زیادکردنی پاداشت"
        AppLanguage.ARABIC -> "تأكيد إضافة المكافأة للحساب"
        AppLanguage.ENGLISH -> "Confirm & Add Bonus"
    }
    val totalLifetimeSpent: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆی کڕینەکان:"
        AppLanguage.ARABIC -> "إجمالي المشتريات:"
        AppLanguage.ENGLISH -> "Lifetime Spend:"
    }
    val clientOrdersCount: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی داواکارییەکان:"
        AppLanguage.ARABIC -> "عدد الطلبات:"
        AppLanguage.ENGLISH -> "Orders Placed:"
    }
    val noOrdersYet: String get() = when (lang) {
        AppLanguage.KURDISH -> "هیچ مێژوویەکی داواکاری نییە بۆ ئەم کڕیارە"
        AppLanguage.ARABIC -> "لا توجد طلبات سابقة لهذا العميل"
        AppLanguage.ENGLISH -> "No order history yet"
    }
    val bonusSuccessMsg: String get() = when (lang) {
        AppLanguage.KURDISH -> "پاداشت بە سەرکەوتوویی زیادکرا بۆ هەژماری کڕیار"
        AppLanguage.ARABIC -> "تمت إضافة المكافأة وتحديث رصيد العميل بنجاح"
        AppLanguage.ENGLISH -> "Bonus granted successfully"
    }

    // Inventory & Store Management
    val inventoryHeader: String get() = when (lang) {
        AppLanguage.KURDISH -> "بەڕێوەبردنی کۆگا و هاتنی کەلوپەل"
        AppLanguage.ARABIC -> "إدارة المخزون وتوريد الشحنات"
        AppLanguage.ENGLISH -> "Store Inventory & Stock Control"
    }
    val totalInventoryUnits: String get() = when (lang) {
        AppLanguage.KURDISH -> "کۆی ژمارەی کەلوپەل لە کۆگا"
        AppLanguage.ARABIC -> "إجمالي الكمية بالمخزن"
        AppLanguage.ENGLISH -> "Total Units in Warehouse"
    }
    val inventoryValuation: String get() = when (lang) {
        AppLanguage.KURDISH -> "نرخی کۆی کەلوپەلەکانی کۆگا"
        AppLanguage.ARABIC -> "قيمة المخزون الإجمالية"
        AppLanguage.ENGLISH -> "Total Inventory Value"
    }
    val manageStockBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "دیاریکردنی بڕ"
        AppLanguage.ARABIC -> "تعديل الكمية"
        AppLanguage.ENGLISH -> "Set Stock"
    }
    val restockDialogTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "نوێکردنەوەی بڕی کۆگا"
        AppLanguage.ARABIC -> "إدارة كمية المنتج والشحنات الواردة"
        AppLanguage.ENGLISH -> "Restock & Manage Inventory Quantity"
    }
    val currentStockLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "بڕی ئێستا لە کۆگادا:"
        AppLanguage.ARABIC -> "الكمية المتوفرة حالياً بالمخزن:"
        AppLanguage.ENGLISH -> "Current Stock in Warehouse:"
    }
    val enterNewStock: String get() = when (lang) {
        AppLanguage.KURDISH -> "بڕی گشتی نوێ بنووسە (دانە):"
        AppLanguage.ARABIC -> "أدخل الكمية الإجمالية الجديدة (قطع):"
        AppLanguage.ENGLISH -> "Enter New Total Stock Quantity:"
    }
    val quickAddHeader: String get() = when (lang) {
        AppLanguage.KURDISH -> "یان زیادکردنی خێرای بڕی بارکراو:"
        AppLanguage.ARABIC -> "أو أضف كمية شحنة واردة سريعة:"
        AppLanguage.ENGLISH -> "Or Quick Restock Batch:"
    }
    val restockBatchNote: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی باری شحن / تێبینی (ئارەزوومەندانە)"
        AppLanguage.ARABIC -> "رقم الشحنة / بوليصة الشحن (اختياري)"
        AppLanguage.ENGLISH -> "Shipment / Waybill Ref (Optional)"
    }
    val saveStockBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "پاشەکەوتکردن و نوێکردنەوەی کۆگا"
        AppLanguage.ARABIC -> "حفظ وتحديث رصيد المخزن"
        AppLanguage.ENGLISH -> "Save & Update Stock Balance"
    }
    val filterInStock: String get() = when (lang) {
        AppLanguage.KURDISH -> "لە کۆگادا هەیە"
        AppLanguage.ARABIC -> "متوفر بالمخزن"
        AppLanguage.ENGLISH -> "In Stock"
    }
    val filterLowStockAlert: String get() = when (lang) {
        AppLanguage.KURDISH -> "بڕی کەم (کەمتر لە 10)"
        AppLanguage.ARABIC -> "كمية حرجة (أقل من 10)"
        AppLanguage.ENGLISH -> "Low Stock (< 10)"
    }
    val filterOutOfStockAlert: String get() = when (lang) {
        AppLanguage.KURDISH -> "لە کۆگادا نەماوە (0)"
        AppLanguage.ARABIC -> "نفد من المخزن (0)"
        AppLanguage.ENGLISH -> "Out of Stock (0)"
    }
    val stockUnitsLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "دانە لە کۆگادا"
        AppLanguage.ARABIC -> "قطع بالمخزن"
        AppLanguage.ENGLISH -> "units in stock"
    }
    val searchInventoryHint: String get() = when (lang) {
        AppLanguage.KURDISH -> "گەڕان بە ناو، مارکە، یان کۆدی SKU..."
        AppLanguage.ARABIC -> "بحث بالاسم، الماركة، أو رمز SKU..."
        AppLanguage.ENGLISH -> "Search by product name, brand, or SKU..."
    }

    // Coffee Taste Quiz & Matcher
    val tasteFinderTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "ڕاوێژکاری زیرەکی قاوە"
        AppLanguage.ARABIC -> "مستشار القهوة الذكي"
        AppLanguage.ENGLISH -> "Smart Coffee Matcher"
    }
    val tasteFinderSubtitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "دڵنیا نیت چی هەڵبژێریت؟ تەنها لە 1 خولەکدا وەڵامی پرسیارەکان بدەرەوە بۆ دۆزینەوەی 3 باشترین قاوە بەپێی حەزی خۆت."
        AppLanguage.ARABIC -> "حائر في اختيار المحصول؟ أجب عن 5 أسئلة لاكتشاف أفضل 3 محاصيل تناسب ذوقك وتفضيلاتك بدقة."
        AppLanguage.ENGLISH -> "Not sure what to choose? Answer a 1-minute quiz about sourness, bitterness & roast to receive your top 3 matches."
    }
    val startQuizBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "دەستپێکردنی تاقیکردنەوەی چێژ"
        AppLanguage.ARABIC -> "ابدأ اختبار التذوق (1 دقيقة)"
        AppLanguage.ENGLISH -> "Start 1-Min Palate Quiz"
    }
    val quizDialogTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "دیاریکردنی باشترین قاوە بۆ چێژی تۆ"
        AppLanguage.ARABIC -> "اختبار تذوق القهوة وتحديد المحصول المثالي"
        AppLanguage.ENGLISH -> "Palate Profiler: Find Your Signature Coffee"
    }
    val questionAcidity: String get() = when (lang) {
        AppLanguage.KURDISH -> "1. حەزت لە چ ئاستێکی ترشیاتی قاوەیە (Acidity / Sourness)؟"
        AppLanguage.ARABIC -> "1. ما هو مستوى الحموضة والفاكهية (Sourness / Acidity) الذي تفضله؟"
        AppLanguage.ENGLISH -> "1. What is your preferred sourness & acidity level?"
    }
    val questionBitterness: String get() = when (lang) {
        AppLanguage.KURDISH -> "2. حەزت لە چ ئاستێکی تاڵی و هێزی قاوەیە (Bitterness)؟"
        AppLanguage.ARABIC -> "2. ما هو مستوى المرارة والحدة (Bitterness) المفضل لديك؟"
        AppLanguage.ENGLISH -> "2. What level of bitterness & strength do you enjoy?"
    }
    val questionRoast: String get() = when (lang) {
        AppLanguage.KURDISH -> "3. چ جۆرە برژاندنێکت پێ باشترە (Roast Grade)؟"
        AppLanguage.ARABIC -> "3. ما هي درجة التحميص (Roasting Grade) التي تميل إليها؟"
        AppLanguage.ENGLISH -> "3. Which degree of roasting do you prefer?"
    }
    val questionBrew: String get() = when (lang) {
        AppLanguage.KURDISH -> "4. زیاتر بە چ شێوازێک قاوە ئامادە دەکەیت؟"
        AppLanguage.ARABIC -> "4. ما هي طريقة التحضير التي تستخدمها في العادة؟"
        AppLanguage.ENGLISH -> "4. What is your everyday brewing method?"
    }
    val questionFlavor: String get() = when (lang) {
        AppLanguage.KURDISH -> "5. چ جۆرە بۆن و تامێک زیاتر سەرنجت ڕادەکێشێت؟"
        AppLanguage.ARABIC -> "5. ما هي عائلة النكهات والإيحاءات التي تفضلها في فنجانك؟"
        AppLanguage.ENGLISH -> "5. Which tasting notes family appeals most to your palate?"
    }
    val revealTopMatchesBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "دەرخستنی 3 باشترین قاوە بۆ تۆ"
        AppLanguage.ARABIC -> "كشف أفضل 3 بدائل مطابقة لذوقك"
        AppLanguage.ENGLISH -> "Reveal My Top 3 Matches"
    }
    val topMatchesHeader: String get() = when (lang) {
        AppLanguage.KURDISH -> "3 باشترین بەرهەم کە تەواو لەگەڵ چێژت دەگونجێن"
        AppLanguage.ARABIC -> "أفضل 3 بدائل تطابق ذوقك تماماً"
        AppLanguage.ENGLISH -> "Your Top 3 Curated Coffee Matches"
    }
    val topMatchesSubheader: String get() = when (lang) {
        AppLanguage.KURDISH -> "بە پێی حەزی تایبەتی خۆت لە ترشی، تاڵی، و برژاندن دیاریکراوە"
        AppLanguage.ARABIC -> "تمت مطابقتها وحسابها بدقة وفقاً لتفضيلاتك في الحموضة، المرارة، والتحميص"
        AppLanguage.ENGLISH -> "Ranked by palate algorithm based on your exact acidity, bitterness, and roasting preferences"
    }
    val matchScoreLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "ڕێژەی گونجان"
        AppLanguage.ARABIC -> "نسبة التطابق"
        AppLanguage.ENGLISH -> "Match"
    }
    val retakeQuizBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "دووبارەکردنەوەی تاقیکردنەوەکە"
        AppLanguage.ARABIC -> "إعادة اختبار التذوق"
        AppLanguage.ENGLISH -> "Retake Palate Quiz"
    }
    val rankBestMatch: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەڵبژاردەی یەکەم (بەرزترین گونجان)"
        AppLanguage.ARABIC -> "الخيار الأول (التطابق الأعلى)"
        AppLanguage.ENGLISH -> "Choice #1 (Best Match)"
    }
    val rankAltTwo: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەڵبژاردەی دووەم (جێگرەوەی نزیک)"
        AppLanguage.ARABIC -> "الخيار الثاني (بديل متقارب)"
        AppLanguage.ENGLISH -> "Choice #2 (Close Alternative)"
    }
    val rankAltThree: String get() = when (lang) {
        AppLanguage.KURDISH -> "هەڵبژاردەی سێیەم (جێگرەوەی زیاتر)"
        AppLanguage.ARABIC -> "الخيار الثالث (بديل إضافي)"
        AppLanguage.ENGLISH -> "Choice #3 (Alternative Option)"
    }

    // WhatsApp Integration Strings
    val whatsappSectionTitle: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئاگادارکردنەوەکانی واتسئاپ بۆ داواکارییەکان"
        AppLanguage.ARABIC -> "إشعارات الواتساب وتنبيهات الطلبات"
        AppLanguage.ENGLISH -> "WhatsApp Order Notifications"
    }
    val whatsappAdminNumberLabel: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی واتسئاپ بۆ وەرگرتنی داواکارییە نوێیەکان:"
        AppLanguage.ARABIC -> "رقم هاتف الواتساب لاستلام إشعارات الطلبات الفورية:"
        AppLanguage.ENGLISH -> "WhatsApp Number to Receive Instant Order Alerts:"
    }
    val whatsappNumberHint: String get() = when (lang) {
        AppLanguage.KURDISH -> "نموونە: 0750 123 4567 یان 9647501234567"
        AppLanguage.ARABIC -> "مثال: 0770 123 4567 أو 9647701234567"
        AppLanguage.ENGLISH -> "e.g., 0750 123 4567 or +964 770 123 4567"
    }
    val whatsappSaveBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "پاشەکەوتکردنی ژمارەی واتسئاپ"
        AppLanguage.ARABIC -> "حفظ رقم الواتساب"
        AppLanguage.ENGLISH -> "Save WhatsApp Number"
    }
    val whatsappSendOrderBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "ناردنی داواکاری بۆ واتسئاپ"
        AppLanguage.ARABIC -> "إرسال تفاصيل الطلب للواتساب"
        AppLanguage.ENGLISH -> "Notify via WhatsApp"
    }
    val whatsappSentSuccess: String get() = when (lang) {
        AppLanguage.KURDISH -> "ئاگاداری داواکاری بۆ واتسئاپ ئامادەکرا"
        AppLanguage.ARABIC -> "تم تجهيز إشعار الطلب على تطبيق الواتساب"
        AppLanguage.ENGLISH -> "WhatsApp notification prepared successfully"
    }
    val whatsappConfigSaved: String get() = when (lang) {
        AppLanguage.KURDISH -> "ژمارەی واتسئاپ بە سەرکەوتوویی پاشەکەوت کرا"
        AppLanguage.ARABIC -> "تم حفظ رقم هاتف الواتساب بنجاح"
        AppLanguage.ENGLISH -> "WhatsApp number saved successfully"
    }
    val whatsappNotifyCustomerBtn: String get() = when (lang) {
        AppLanguage.KURDISH -> "پەیوەندی بە کڕیار لە واتسئاپ"
        AppLanguage.ARABIC -> "مراسلة العميل عبر الواتساب"
        AppLanguage.ENGLISH -> "Chat with Customer on WhatsApp"
    }
    val autoWhatsAppToggle: String get() = when (lang) {
        AppLanguage.KURDISH -> "ناردنی ئۆتۆماتیکی ئاگاداری واتسئاپ لە کاتی هەر داواکارییەکی نوێ"
        AppLanguage.ARABIC -> "إرسال إشعار فوري تلقائي للواتساب عند كل طلب جديد"
        AppLanguage.ENGLISH -> "Auto-open WhatsApp notification on new orders"
    }
}

@Composable
fun onyxStrings(): OnyxStrings {
    val language = LocalAppLanguage.current
    return OnyxStrings(language)
}
