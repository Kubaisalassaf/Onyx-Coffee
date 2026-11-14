package com.example.data

import com.example.R
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object OnyxRepository {

    // --- Current User Profile ---
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_iq_001",
            fullName = "Haider Al-Baghdadi",
            email = "haider.coffee@onyx.iq",
            phone = "+964 770 123 4567",
            role = UserRole.ADMIN, // Admin capable for dashboard demo
            loyaltyPoints = 1450,
            loyaltyTier = LoyaltyTier.GOLD,
            addresses = listOf(
                DeliveryAddress(
                    id = "addr_1",
                    title = "Home",
                    governorate = IraqiGovernorate.BAGHDAD,
                    cityOrDistrict = "Al-Mansour, District 603",
                    streetAddress = "Street 14, Villa 8B",
                    recipientName = "Haider Al-Baghdadi",
                    phone = "+964 770 123 4567",
                    deliveryNotes = "Near Damascus Square, call upon arrival",
                    isDefault = true
                ),
                DeliveryAddress(
                    id = "addr_2",
                    title = "Boutique Cafe",
                    governorate = IraqiGovernorate.ERBIL,
                    cityOrDistrict = "Gulan Street, Empire World",
                    streetAddress = "Tower 4, Ground Floor Cafe",
                    recipientName = "Aram Kurdistan",
                    phone = "+964 750 987 6543",
                    deliveryNotes = "ONYX Roastery distribution hub",
                    isDefault = false
                )
            ),
            favoriteProductIds = setOf("p_ethiopia_1", "p_machine_lamarzocco", "p_acacia_scale")
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // --- Registered Clients Directory ---
    private val _customers = MutableStateFlow<List<UserProfile>>(createInitialCustomers())
    val customers: StateFlow<List<UserProfile>> = _customers.asStateFlow()

    // --- Product Catalog State ---
    private val _products = MutableStateFlow<List<Product>>(createInitialProducts())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // --- Shopping Cart State ---
    private val _cartItems = MutableStateFlow<List<CartItem>>(
        listOf(
            CartItem(
                product = createInitialProducts().first { it.id == "p_ethiopia_1" },
                selectedPackageGram = 500,
                quantity = 2
            ),
            CartItem(
                product = createInitialProducts().first { it.id == "p_acacia_scale" },
                selectedPackageGram = 250,
                quantity = 1
            )
        )
    )
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // --- Applied Coupon ---
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    // --- Orders State ---
    private val _orders = MutableStateFlow<List<Order>>(createInitialOrders())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    // --- Governorate Delivery Fees (Mutable by Admin) ---
    private val _governorateDeliveryFees = MutableStateFlow(
        IraqiGovernorate.values().associate { it to it.standardDeliveryFeeIqd }.toMutableMap()
    )
    val governorateDeliveryFees: StateFlow<Map<IraqiGovernorate, Long>> = _governorateDeliveryFees.asStateFlow()

    // --- Available Coupons ---
    private val _coupons = MutableStateFlow(
        listOf(
            Coupon("BAGHDAD10", 10, 30000, true, "10% off for Baghdad specialty coffee lovers", "خصم 10% لمحبي القهوة المختصة في بغداد"),
            Coupon("ONYXVIP", 15, 60000, true, "15% off exclusive VIP barista discount", "خصم 15% حصري لأعضاء نادي أونيكس VIP"),
            Coupon("ERBILSPECIAL", 12, 45000, true, "12% off on all espresso equipment", "خصم 12% على جميع معدات الإسبريسو")
        )
    )
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    // --- Journal Articles ---
    private val _articles = MutableStateFlow(createInitialArticles())
    val articles: StateFlow<List<JournalArticle>> = _articles.asStateFlow()

    // --- Iraqi Market Exchange Rate (USD to IQD) ---
    private val _exchangeRate = MutableStateFlow(ExchangeRateConfig(rateIqdPerUsd = 1500.0, autoCalculateFromUsd = true))
    val exchangeRate: StateFlow<ExchangeRateConfig> = _exchangeRate.asStateFlow()

    fun updateExchangeRate(newRate: Double) {
        val config = _exchangeRate.value.copy(
            rateIqdPerUsd = newRate,
            lastUpdatedEpoch = System.currentTimeMillis()
        )
        _exchangeRate.value = config

        // Automatically recalculate product prices tied to USD base price
        if (config.autoCalculateFromUsd) {
            _products.value = _products.value.map { product ->
                if (product.basePriceUsd != null) {
                    val recalculatedRegular = config.convertUsdToIqd(product.basePriceUsd)
                    val recalculatedPromo = if (product.promoPriceIqd != null) {
                        (recalculatedRegular * 0.9).toLong()
                    } else null
                    product.copy(
                        regularPriceIqd = recalculatedRegular,
                        promoPriceIqd = recalculatedPromo
                    )
                } else product
            }
        }
    }

    // --- Store Settings & Admin WhatsApp Notifications ---
    private val _storeSettings = MutableStateFlow(
        StoreSettings(
            adminWhatsAppNumber = "+9647723058251",
            autoSendWhatsAppOnOrder = true,
            storeName = "ONYX Coffee Roasters Iraq"
        )
    )
    val storeSettings: StateFlow<StoreSettings> = _storeSettings.asStateFlow()

    fun updateStoreSettings(newSettings: StoreSettings) {
        _storeSettings.value = newSettings
    }

    fun updateAdminWhatsAppNumber(newNumber: String) {
        _storeSettings.value = _storeSettings.value.copy(adminWhatsAppNumber = newNumber)
    }

    fun updateAutoNotifyWhatsApp(enabled: Boolean) {
        _storeSettings.value = _storeSettings.value.copy(autoSendWhatsAppOnOrder = enabled)
    }

    // --- Website Bridge & Sync State (onyxcoffeeiraq.com) ---
    private val _websiteConfig = MutableStateFlow(WebsiteIntegrationConfig())
    val websiteConfig: StateFlow<WebsiteIntegrationConfig> = _websiteConfig.asStateFlow()

    private val _websiteSyncLogs = MutableStateFlow<List<String>>(
        listOf("Initialized App Links & API bridge for https://onyxcoffeeiraq.com")
    )
    val websiteSyncLogs: StateFlow<List<String>> = _websiteSyncLogs.asStateFlow()

    private fun appendWebsiteLog(entry: String) {
        val stamped = "[${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())}] $entry"
        _websiteSyncLogs.value = (listOf(stamped) + _websiteSyncLogs.value).take(25)
    }

    fun updateWebsiteConfig(newConfig: WebsiteIntegrationConfig) {
        val normalizedUrl = com.example.data.remote.OnyxWebsiteSyncService.normalizeBaseUrl(newConfig.websiteUrl).trimEnd('/')
        val updated = newConfig.copy(websiteUrl = normalizedUrl)
        _websiteConfig.value = updated
        _storeSettings.value = _storeSettings.value.copy(websiteUrl = normalizedUrl)
        appendWebsiteLog("Saved website config: $normalizedUrl (${updated.platformType.name})")
    }

    suspend fun testWebsiteConnection(): com.example.data.remote.WebsiteConnectionCheckResult {
        val current = _websiteConfig.value
        _websiteConfig.value = current.copy(
            connectionStatus = WebsiteConnectionStatus.TESTING,
            statusDetail = "Testing ${current.websiteUrl}..."
        )
        val result = com.example.data.remote.OnyxWebsiteSyncService.testConnection(current)
        _websiteConfig.value = _websiteConfig.value.copy(
            connectionStatus = result.status,
            statusDetail = result.detail,
            lastSyncedEpoch = System.currentTimeMillis()
        )
        appendWebsiteLog("Connection test -> ${result.detail}")
        return result
    }

    suspend fun syncProductsFromWebsite(): com.example.data.remote.WebsiteProductSyncResult {
        val current = _websiteConfig.value
        _websiteConfig.value = current.copy(
            connectionStatus = WebsiteConnectionStatus.TESTING,
            statusDetail = "Syncing catalog from ${current.websiteUrl}..."
        )
        val result = com.example.data.remote.OnyxWebsiteSyncService.fetchProducts(current)
        if (result.success && result.products.isNotEmpty()) {
            val existingById = _products.value.associateBy { it.id }.toMutableMap()
            result.products.forEach { remoteProduct ->
                existingById[remoteProduct.id] = remoteProduct
            }
            _products.value = existingById.values.toList()
        }
        _websiteConfig.value = _websiteConfig.value.copy(
            connectionStatus = result.status,
            statusDetail = result.detail,
            lastSyncedEpoch = System.currentTimeMillis()
        )
        appendWebsiteLog("Catalog sync -> ${result.detail}")
        return result
    }

    suspend fun syncOrderToWebsite(order: Order): com.example.data.remote.WebsiteOrderSyncResult {
        val current = _websiteConfig.value
        val result = com.example.data.remote.OnyxWebsiteSyncService.pushOrder(order, current)
        _websiteConfig.value = _websiteConfig.value.copy(
            connectionStatus = result.status,
            statusDetail = result.detail,
            lastSyncedEpoch = System.currentTimeMillis()
        )
        appendWebsiteLog("Order ${order.orderNumber} -> ${result.detail}")
        return result
    }

    suspend fun refreshOrderTrackingFromWebsite(orderNumber: String): com.example.data.remote.WebsiteOrderSyncResult {
        val current = _websiteConfig.value
        val result = com.example.data.remote.OnyxWebsiteSyncService.fetchOrderTrackingStatus(orderNumber, current)
        if (result.success && result.remoteOrderStatus != null) {
            _orders.value = _orders.value.map { order ->
                if (order.orderNumber.equals(orderNumber, ignoreCase = true) ||
                    order.trackingNumber.equals(orderNumber, ignoreCase = true)
                ) {
                    order.copy(orderStatus = result.remoteOrderStatus)
                } else {
                    order
                }
            }
        }
        appendWebsiteLog("Track $orderNumber -> ${result.detail}")
        return result
    }

    // --- Cart Actions ---
    fun addToCart(product: Product, packageGram: Int = 250, quantity: Int = 1) {
        val currentList = _cartItems.value.toMutableList()
        val existingIndex = currentList.indexOfFirst {
            it.product.id == product.id && it.selectedPackageGram == packageGram
        }

        if (existingIndex >= 0) {
            val existing = currentList[existingIndex]
            currentList[existingIndex] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentList.add(CartItem(product, packageGram, quantity))
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(cartItem: CartItem, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(cartItem)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.product.id == cartItem.product.id && it.selectedPackageGram == cartItem.selectedPackageGram
        }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(quantity = newQty)
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.value = _cartItems.value.filterNot {
            it.product.id == cartItem.product.id && it.selectedPackageGram == cartItem.selectedPackageGram
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
    }

    fun applyCoupon(code: String): Result<Coupon> {
        val trimmed = code.trim().uppercase()
        val found = _coupons.value.find { it.code == trimmed && it.active }
        return if (found != null) {
            _appliedCoupon.value = found
            Result.success(found)
        } else {
            Result.failure(IllegalArgumentException("Invalid or expired coupon code: $code"))
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // --- Order Checkout ---
    fun placeOrder(
        deliveryAddress: DeliveryAddress,
        paymentProvider: PaymentProvider,
        notes: String = ""
    ): Order {
        val items = _cartItems.value
        val subtotal = items.sumOf { it.totalPriceIqd }
        val fee = if (subtotal >= 100000) 0L else (_governorateDeliveryFees.value[deliveryAddress.governorate] ?: 6000L)
        val coupon = _appliedCoupon.value
        val discount = if (coupon != null && subtotal >= coupon.minOrderIqd) {
            (subtotal * (coupon.discountPercent / 100.0)).toLong()
        } else 0L

        val total = (subtotal + fee - discount).coerceAtLeast(0L)
        val orderNum = "ONYX-IQ-${(1000..9999).random()}"
        val tracking = "IQ-EXP-${(100000..999999).random()}"

        val order = Order(
            id = UUID.randomUUID().toString(),
            orderNumber = orderNum,
            customerId = _currentUser.value.id,
            customerName = deliveryAddress.recipientName,
            customerPhone = deliveryAddress.phone,
            deliveryAddress = deliveryAddress,
            items = items,
            subtotalIqd = subtotal,
            deliveryFeeIqd = fee,
            discountIqd = discount,
            finalTotalIqd = total,
            paymentProvider = paymentProvider,
            paymentStatus = if (paymentProvider.isOnline) PaymentStatus.PAID else PaymentStatus.PENDING,
            orderStatus = OrderStatus.CONFIRMED,
            trackingNumber = tracking,
            transactionRef = if (paymentProvider.isOnline) "TXN-${UUID.randomUUID().toString().take(8).uppercase()}" else "",
            notes = notes
        )

        // Prepend to orders
        _orders.value = listOf(order) + _orders.value

        // Award loyalty points: 1 point per 1,000 IQD
        val pointsEarned = (total / 1000).toInt()
        val newPoints = _currentUser.value.loyaltyPoints + pointsEarned
        val newTier = when {
            newPoints >= LoyaltyTier.ONYX_VIP.minPoints -> LoyaltyTier.ONYX_VIP
            newPoints >= LoyaltyTier.GOLD.minPoints -> LoyaltyTier.GOLD
            newPoints >= LoyaltyTier.SILVER.minPoints -> LoyaltyTier.SILVER
            else -> LoyaltyTier.BRONZE
        }
        _currentUser.value = _currentUser.value.copy(
            loyaltyPoints = newPoints,
            loyaltyTier = newTier
        )

        clearCart()
        return order
    }

    // --- Admin Operations ---
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.id == orderId) it.copy(orderStatus = newStatus) else it
        }
    }

    fun addProduct(product: Product) {
        _products.value = listOf(product) + _products.value
    }

    fun updateProduct(product: Product) {
        _products.value = _products.value.map {
            if (it.id == product.id) product else it
        }
    }

    fun deleteProduct(productId: String) {
        _products.value = _products.value.filterNot { it.id == productId }
    }

    fun updateStock(productId: String, delta: Int) {
        _products.value = _products.value.map {
            if (it.id == productId) {
                val newStock = (it.stockQuantity + delta).coerceAtLeast(0)
                it.copy(stockQuantity = newStock)
            } else it
        }
    }

    fun setStock(productId: String, exactQuantity: Int) {
        _products.value = _products.value.map {
            if (it.id == productId) {
                it.copy(stockQuantity = exactQuantity.coerceAtLeast(0))
            } else it
        }
    }

    fun updateDeliveryFee(governorate: IraqiGovernorate, feeIqd: Long) {
        val current = _governorateDeliveryFees.value.toMutableMap()
        current[governorate] = feeIqd
        _governorateDeliveryFees.value = current
    }

    // --- Loyalty CRM Operations ---
    fun grantCustomerBonus(customerId: String, bonusPoints: Int, newTier: LoyaltyTier? = null, reason: String = "") {
        _customers.value = _customers.value.map { customer ->
            if (customer.id == customerId) {
                val updatedPoints = (customer.loyaltyPoints + bonusPoints).coerceAtLeast(0)
                val targetTier = newTier ?: when {
                    updatedPoints >= LoyaltyTier.ONYX_VIP.minPoints -> LoyaltyTier.ONYX_VIP
                    updatedPoints >= LoyaltyTier.GOLD.minPoints -> LoyaltyTier.GOLD
                    updatedPoints >= LoyaltyTier.SILVER.minPoints -> LoyaltyTier.SILVER
                    else -> LoyaltyTier.BRONZE
                }
                customer.copy(
                    loyaltyPoints = updatedPoints,
                    loyaltyTier = targetTier
                )
            } else customer
        }

        // If it's also the active session user, sync immediately
        if (_currentUser.value.id == customerId) {
            val updatedPoints = (_currentUser.value.loyaltyPoints + bonusPoints).coerceAtLeast(0)
            val targetTier = newTier ?: when {
                updatedPoints >= LoyaltyTier.ONYX_VIP.minPoints -> LoyaltyTier.ONYX_VIP
                updatedPoints >= LoyaltyTier.GOLD.minPoints -> LoyaltyTier.GOLD
                updatedPoints >= LoyaltyTier.SILVER.minPoints -> LoyaltyTier.SILVER
                else -> LoyaltyTier.BRONZE
            }
            _currentUser.value = _currentUser.value.copy(
                loyaltyPoints = updatedPoints,
                loyaltyTier = targetTier
            )
        }
    }

    // --- Favorites ---
    fun toggleFavorite(productId: String) {
        val user = _currentUser.value
        val favs = user.favoriteProductIds.toMutableSet()
        if (favs.contains(productId)) {
            favs.remove(productId)
        } else {
            favs.add(productId)
        }
        _currentUser.value = user.copy(favoriteProductIds = favs)
    }

    fun toggleUserRole() {
        val current = _currentUser.value
        val newRole = if (current.role == UserRole.ADMIN) UserRole.CUSTOMER else UserRole.ADMIN
        _currentUser.value = current.copy(role = newRole)
    }

    // --- Initial Mock Data ---
    private fun createInitialProducts(): List<Product> = listOf(
        Product(
            id = "p_ethiopia_1",
            sku = "COF-ETH-001",
            nameEn = "Ethiopia Yirgacheffe G1 Natural",
            nameAr = "إثيوبيا ييرغاتشيف G1 مجففة",
            nameKu = "ئیسیۆپیا یەرگاچیف G1 وشکراوە",
            descriptionEn = "Hand-picked heirloom cherries from high altitudes of Yirgacheffe. Distinctive floral aroma with rich notes of wild blueberry, bergamot, and sweet peach syrup.",
            descriptionAr = "حبات كرز منتقاة يدوياً من سلالة أصلية في مرتفعات ييرغاتشيف. عطرية زهرية مميزة مع إيحاءات التوت البري، البرغموت، وشراب الخوخ الحلو.",
            descriptionKu = "قاوەی نایابی ناوچەی یەرگاچیف لە بەرزاییەکان. خاوەنی بۆنێکی گوڵاوی سەرنجڕاکێشە بە تامی تووتڕکی کێوی و قۆخی شیرین.",
            category = ProductCategory.COFFEE,
            subCategory = "Single Origin Specialty",
            brand = "ONYX Reserve",
            regularPriceIqd = 28000,
            promoPriceIqd = 24500,
            basePriceUsd = 16.50,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 45,
            isFeatured = true,
            isBestSeller = true,
            isNewArrival = true,
            rating = 4.95,
            reviewCount = 54,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Ethiopia",
                originCountryAr = "إثيوبيا",
                originCountryKu = "ئیسیۆپیا",
                region = "Yirgacheffe / Gedeb",
                altitudeMeters = "2,050m - 2,200m",
                variety = "Heirloom (سلالات برية)",
                processingMethod = "Natural Sun-Dried (مجففة)",
                roastLevel = RoastLevel.LIGHT,
                beanType = BeanType.ARABICA,
                flavorProfile = listOf("Wild Blueberry", "Jasmine", "Bergamot", "Peach"),
                flavorProfileAr = listOf("توت بري", "ياسمين", "برغموت", "خوخ مجفف"),
                acidityRating = 5,
                bodyRating = 3,
                bitternessRating = 1,
                aromaDescription = "Intense floral bouquet with sweet ripe berry aromatics",
                recommendedBrew = BrewMethod.V60,
                defaultPackageGram = 250
            )
        ),
        Product(
            id = "p_colombia_geisha",
            sku = "COF-COL-002",
            nameEn = "Colombia Geisha Huila Reserve",
            nameAr = "كولومبيا غيشا هويلا ريزيرف",
            nameKu = "کۆڵۆمبیا گێشا هوێلا ڕیزێرڤ",
            descriptionEn = "Prized Geisha varietal grown in mineral-rich volcanic soil of Huila. Silky body with exquisite notes of jasmine flower, lemongrass, and papaya.",
            descriptionAr = "سلالة الغيشا الأسطورية المزروعة في تربة هويلا البركانية الغنية بالمعادن. قوام حريري مذهل مع إيحاءات أزهار الياسمين وعشبة الليمون والبابايا.",
            descriptionKu = "جۆری ناوازە و بەناوبانگی گێشا لە خاکی گڕکانی هوێلا. قەبارەیەکی ئاوریشمی لەگەڵ تامی گوڵی یاسەمین، لیمۆن گراس، و پاپایا.",
            category = ProductCategory.COFFEE,
            subCategory = "Micro-Lot Geisha",
            brand = "ONYX Private Collection",
            regularPriceIqd = 45000,
            promoPriceIqd = null,
            basePriceUsd = 30.00,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 18,
            isFeatured = true,
            isBestSeller = false,
            isNewArrival = true,
            rating = 5.0,
            reviewCount = 31,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Colombia",
                originCountryAr = "كولومبيا",
                region = "Huila / Pitalito",
                altitudeMeters = "1,900m",
                variety = "Geisha (غيشا)",
                processingMethod = "Washed 48h Ferment (مغسولة)",
                roastLevel = RoastLevel.LIGHT,
                beanType = BeanType.ARABICA,
                flavorProfile = listOf("Jasmine", "Lemongrass", "Papaya", "Cane Sugar"),
                flavorProfileAr = listOf("أزهار الياسمين", "ليمون غراس", "بابايا", "سكر القصب"),
                acidityRating = 5,
                bodyRating = 3,
                bitternessRating = 1,
                aromaDescription = "Exotic florals with crisp tropical citrus",
                recommendedBrew = BrewMethod.CHEMEX,
                defaultPackageGram = 250
            )
        ),
        Product(
            id = "p_yemen_mokha",
            sku = "COF-YEM-003",
            nameEn = "Yemen Mokha Matari Ancient Heirloom",
            nameAr = "يمن موكا مطري أثري عريق",
            descriptionEn = "Grown on ancient stepped terraces in the northern highlands of Bani Matar. Complex notes of dark dried figs, cardamom spice, and rich melted cocoa.",
            descriptionAr = "مزروع على مصاطب صخرية تاريخية في المرتفعات الشمالية لبني مطر. طبقات معقدة من التين المجفف، الهيل العطري، والكاكاو الغني.",
            category = ProductCategory.COFFEE,
            subCategory = "Heritage Coffee",
            brand = "ONYX Heritage",
            regularPriceIqd = 55000,
            promoPriceIqd = 49000,
            basePriceUsd = 36.00,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 12,
            isFeatured = true,
            isBestSeller = true,
            isNewArrival = false,
            rating = 4.9,
            reviewCount = 24,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Yemen",
                originCountryAr = "اليمن",
                region = "Bani Matar",
                altitudeMeters = "2,300m",
                variety = "Udaini / Dawairi",
                processingMethod = "Traditional Rooftop Natural",
                roastLevel = RoastLevel.MEDIUM,
                beanType = BeanType.ARABICA,
                flavorProfile = listOf("Dried Figs", "Cardamom", "Dark Chocolate", "Spiced Plum"),
                flavorProfileAr = listOf("تين مجفف", "هيل عطري", "شوكولاتة داكنة", "برقوق مبهر"),
                acidityRating = 3,
                bodyRating = 5,
                bitternessRating = 3,
                aromaDescription = "Warm Eastern spices and molasses sweetness",
                recommendedBrew = BrewMethod.TURKISH,
                defaultPackageGram = 250
            )
        ),
        Product(
            id = "p_onyx_velvet_blend",
            sku = "COF-BLD-004",
            nameEn = "ONYX Black Velvet Espresso Blend",
            nameAr = "خلطة أونيكس المخملية للإسبريسو",
            descriptionEn = "Our flagship house espresso roasted for dense golden crema and velvet mouthfeel. Deep chocolate fudge, roasted hazelnut, and caramel drizzle.",
            descriptionAr = "خلطتنا المميزة للإسبريسو المحمصة لإنتاج كريما ذهبية كثيفة وقوام مخملي. إيحاءات الشوكولاتة الداكنة، البندق المحمص، والكراميل.",
            category = ProductCategory.COFFEE,
            subCategory = "Espresso Blends",
            brand = "ONYX Signature",
            regularPriceIqd = 22000,
            promoPriceIqd = 19500,
            basePriceUsd = 14.50,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 120,
            isFeatured = true,
            isBestSeller = true,
            isNewArrival = false,
            rating = 4.88,
            reviewCount = 142,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Brazil & Guatemala",
                originCountryAr = "البرازيل وغواتيمالا",
                region = "Cerrado & Antigua",
                altitudeMeters = "1,400m - 1,750m",
                variety = "Bourbon & Catuai",
                processingMethod = "Pulped Natural & Washed",
                roastLevel = RoastLevel.MEDIUM_DARK,
                beanType = BeanType.BLEND,
                flavorProfile = listOf("Dark Chocolate Fudge", "Roasted Hazelnut", "Golden Caramel"),
                flavorProfileAr = listOf("شوكولاتة فادج", "بندق محمص", "كراميل ذهبي"),
                acidityRating = 2,
                bodyRating = 5,
                bitternessRating = 4,
                aromaDescription = "Praline, roasted nuts, and melted baker's chocolate",
                recommendedBrew = BrewMethod.ESPRESSO,
                defaultPackageGram = 500
            )
        ),
        Product(
            id = "p_guatemala_antigua",
            sku = "COF-GUA-005",
            nameEn = "Guatemala Antigua Los Volcanes",
            nameAr = "غواتيمالا أنتيغوا لوس فولكانيس",
            descriptionEn = "Cultivated between three majestic volcanoes in rich volcanic pumice. Highly balanced cup with velvety milk chocolate, sweet Meyer lemon, and toasted almond.",
            descriptionAr = "محصول مميز مزروع بين ثلاثة براكين مهيبة في تربة بركانية خصبة. كوب متوازن بنعومة الشوكولاتة بالحليب، البرتقال الحلو، واللوز المحمص.",
            category = ProductCategory.COFFEE,
            subCategory = "Single Origin Specialty",
            brand = "ONYX Reserve",
            regularPriceIqd = 26000,
            promoPriceIqd = null,
            basePriceUsd = 17.00,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 35,
            isFeatured = false,
            isBestSeller = true,
            isNewArrival = true,
            rating = 4.92,
            reviewCount = 38,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Guatemala",
                originCountryAr = "غواتيمالا",
                region = "Antigua Valley",
                altitudeMeters = "1,650m - 1,800m",
                variety = "Bourbon & Typica",
                processingMethod = "Fully Washed & Patio Sun-Dried",
                roastLevel = RoastLevel.MEDIUM,
                beanType = BeanType.ARABICA,
                flavorProfile = listOf("Milk Chocolate", "Toasted Almond", "Meyer Lemon", "Caramel"),
                flavorProfileAr = listOf("شوكولاتة بالحليب", "لوز محمص", "ليمون حلو", "كراميل"),
                acidityRating = 3,
                bodyRating = 4,
                bitternessRating = 2,
                aromaDescription = "Warm hazelnut praline and blossom honey",
                recommendedBrew = BrewMethod.V60,
                defaultPackageGram = 250
            )
        ),
        Product(
            id = "p_sumatra_dark_roast",
            sku = "COF-SUM-006",
            nameEn = "Sumatra Mandheling Dark Reserve",
            nameAr = "سومطرة مانديلينغ تحميص داكن",
            descriptionEn = "Traditional Giling Basah (wet-hulled) process delivering an exceptionally dense body, notes of smoked dark cocoa, cedarwood, and rich earthy molasses.",
            descriptionAr = "معالجة التجريد الرطب التقليدية التي تمنح قواماً ثقيلاً استثنائياً، مع إيحاءات الكاكاو الداكن المدخن، خشب الأرز، ودبس السكر الكثيف.",
            category = ProductCategory.COFFEE,
            subCategory = "Dark Roast Reserve",
            brand = "ONYX Dark",
            regularPriceIqd = 24000,
            promoPriceIqd = 21500,
            basePriceUsd = 15.50,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 40,
            isFeatured = false,
            isBestSeller = false,
            isNewArrival = true,
            rating = 4.85,
            reviewCount = 22,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Indonesia (Sumatra)",
                originCountryAr = "إندونيسيا (سومطرة)",
                region = "Lake Toba / Lintong",
                altitudeMeters = "1,350m - 1,550m",
                variety = "Ateng / Tim Tim",
                processingMethod = "Wet-Hulled (Giling Basah)",
                roastLevel = RoastLevel.DARK,
                beanType = BeanType.ARABICA,
                flavorProfile = listOf("Smoked Dark Cocoa", "Cedar Wood", "Earthy Molasses", "Brown Spice"),
                flavorProfileAr = listOf("كاكاو داكن مدخن", "خشب الأرز", "دبس السكر", "توابل دافئة"),
                acidityRating = 1,
                bodyRating = 5,
                bitternessRating = 5,
                aromaDescription = "Deep cedar embers, dark Baker's chocolate, and tobacco leaf",
                recommendedBrew = BrewMethod.ESPRESSO,
                defaultPackageGram = 250
            )
        ),
        Product(
            id = "p_costa_rica_tarrazu",
            sku = "COF-CRI-007",
            nameEn = "Costa Rica Tarrazú Honey Reserve",
            nameAr = "كوستاريكا تارازو هوني ريزيرف",
            descriptionEn = "Yellow Honey processed micro-lot from the famous Tarrazú highlands. Natural sugarcane sweetness bursting with red apple, honeycomb, and black cherry.",
            descriptionAr = "محصول معالجة عسلية صفراء من مرتفعات تارازو الشهيرة. حلاوة طبيعية تشبه قصب السكر مع نكهات التفاح الأحمر، شمع العسل، والكرز الأسود.",
            category = ProductCategory.COFFEE,
            subCategory = "Honey Process Specialty",
            brand = "ONYX Reserve",
            regularPriceIqd = 31000,
            promoPriceIqd = 27500,
            basePriceUsd = 20.00,
            imageResId = R.drawable.img_coffee_bag_1793862833173,
            stockQuantity = 25,
            isFeatured = true,
            isBestSeller = false,
            isNewArrival = true,
            rating = 4.97,
            reviewCount = 18,
            coffeeSpecs = CoffeeSpecs(
                originCountry = "Costa Rica",
                originCountryAr = "كوستاريكا",
                region = "Tarrazú Highlands",
                altitudeMeters = "1,700m - 1,900m",
                variety = "Caturra & Catuai",
                processingMethod = "Yellow Honey Process",
                roastLevel = RoastLevel.MEDIUM_LIGHT,
                beanType = BeanType.ARABICA,
                flavorProfile = listOf("Red Apple", "Honeycomb", "Black Cherry", "Vanilla Cream"),
                flavorProfileAr = listOf("تفاح أحمر", "شمع العسل", "كرز أسود", "كريمة الفانيلا"),
                acidityRating = 4,
                bodyRating = 4,
                bitternessRating = 2,
                aromaDescription = "Sweet honeycomb, baked red fruits, and golden vanilla",
                recommendedBrew = BrewMethod.V60,
                defaultPackageGram = 250
            )
        ),
        // --- Machines ---
        Product(
            id = "p_machine_lamarzocco",
            sku = "MAC-LM-001",
            nameEn = "La Marzocco Linea Mini R (Dual Boiler)",
            nameAr = "لامارزوكو لينيا ميني R (دبل بويلر)",
            descriptionEn = "The gold standard for home baristas and boutique cafes. Dual insulated stainless steel boilers, integrated timer, PID temperature control, and saturated group head.",
            descriptionAr = "المعيار الذهبي لعشاق الإسبريسو والمقاهي المختصة. غلايتان معزولتان من الفولاذ المقاوم للصدأ، مؤقت شوت مدمج، تحكم رقمي PID، ومجموعة تشبع حراري.",
            category = ProductCategory.MACHINES,
            subCategory = "Commercial & Luxury Home",
            brand = "La Marzocco (Florence, Italy)",
            regularPriceIqd = 6200000,
            promoPriceIqd = 5850000,
            basePriceUsd = 3900.00,
            imageResId = R.drawable.img_espresso_machine_1793862848184,
            stockQuantity = 4,
            isFeatured = true,
            isBestSeller = true,
            isNewArrival = true,
            rating = 5.0,
            reviewCount = 19,
            equipmentSpecs = EquipmentSpecs(
                manufacturer = "La Marzocco Italy",
                modelNumber = "Linea Mini R (2026 Edition)",
                powerWatts = 1820,
                pumpPressureBar = 9,
                boilerCapacityLiters = 3.5,
                boilerType = "Independent Dual Boiler (Steam 3.0L / Coffee 0.17L)",
                dimensionsCm = "37.7 x 35.7 x 45.3 cm",
                warrantyMonths = 24,
                voltage = "220-240V / 50Hz (Iraq Official)",
                sparePartsAvailable = true
            )
        ),
        Product(
            id = "p_machine_victoria_arduino",
            sku = "MAC-VA-002",
            nameEn = "Victoria Arduino Eagle One Prima",
            nameAr = "فيكتوريا أردوينو إيغل ون بريما",
            descriptionEn = "Groundbreaking NEO engine espresso machine combining sustainable energy efficiency with barista-level precision, bluetooth app control, and instant steam.",
            descriptionAr = "تقنية NEO الرائدة التي تجمع بين كفاءة الطاقة والتحكم الدقيق للباريستا مع تحكم عبر البلوتوث وبخار فوري فائق القوة.",
            category = ProductCategory.MACHINES,
            subCategory = "Professional 1-Group",
            brand = "Victoria Arduino",
            regularPriceIqd = 7400000,
            promoPriceIqd = null,
            basePriceUsd = 4900.00,
            imageResId = R.drawable.img_espresso_machine_1793862848184,
            stockQuantity = 2,
            isFeatured = true,
            isBestSeller = false,
            isNewArrival = true,
            rating = 4.95,
            reviewCount = 11,
            equipmentSpecs = EquipmentSpecs(
                manufacturer = "Simonelli Group (Italy)",
                modelNumber = "Eagle One Prima",
                powerWatts = 2600,
                pumpPressureBar = 9,
                boilerCapacityLiters = 1.5,
                boilerType = "NEO Instant Flash Heating + Steam Boiler",
                dimensionsCm = "41.1 x 51.0 x 37.9 cm",
                warrantyMonths = 36,
                voltage = "220-240V / 50Hz",
                sparePartsAvailable = true
            )
        ),
        // --- Equipment & Grinders ---
        Product(
            id = "p_grinder_mahlkonig",
            sku = "EQP-MK-001",
            nameEn = "Mahlkönig EK43S Allround Grinder",
            nameAr = "مطحنة مالكونيغ EK43S الاحترافية",
            descriptionEn = "The undisputed king of specialty coffee grinders. Massive 98mm cast steel flat burrs delivering unmatched particle size uniformity across all grind sizes.",
            descriptionAr = "الملك بلا منازع في عالم طحن القهوة المختصة. شفرات فولاذية مسطحة عملاقة 98 مم توفر تناسقاً هندسياً مطلقاً لحبيبات البن.",
            category = ProductCategory.EQUIPMENT,
            subCategory = "Professional Grinders",
            brand = "Mahlkönig (Germany)",
            regularPriceIqd = 3850000,
            promoPriceIqd = 3600000,
            basePriceUsd = 2400.00,
            imageResId = R.drawable.img_coffee_grinder_1793862861591,
            stockQuantity = 6,
            isFeatured = true,
            isBestSeller = true,
            isNewArrival = false,
            rating = 5.0,
            reviewCount = 38,
            equipmentSpecs = EquipmentSpecs(
                manufacturer = "Hemro Group (Germany)",
                modelNumber = "EK43S Short Hopper",
                powerWatts = 1300,
                pumpPressureBar = 0,
                boilerCapacityLiters = 0.0,
                boilerType = "98mm Cast Steel Flat Burrs",
                dimensionsCm = "23 x 68 x 41 cm",
                warrantyMonths = 24,
                voltage = "220-240V / 50Hz",
                sparePartsAvailable = true
            )
        ),
        Product(
            id = "p_acacia_scale",
            sku = "EQP-ACA-002",
            nameEn = "Acaia Lunar Precision Espresso Scale",
            nameAr = "ميزان أكايا لونار المقاوم للماء للإسبريسو",
            descriptionEn = "Ultra-fast 20ms response time, built-in flow-rate indicator, anodized aluminum body with water-resistant protection designed specifically for drip trays.",
            descriptionAr = "سرعة استجابة فائقة 20 مللي ثانية، مؤشر معدل التدفق، وهيكل ألومنيوم مقاوم للماء مصمم خصيصاً ليناسب مساحة صينية التنقيط.",
            category = ProductCategory.EQUIPMENT,
            subCategory = "Smart Scales",
            brand = "Acaia",
            regularPriceIqd = 340000,
            promoPriceIqd = 310000,
            basePriceUsd = 220.00,
            imageResId = R.drawable.img_coffee_grinder_1793862861591,
            stockQuantity = 22,
            isFeatured = false,
            isBestSeller = true,
            isNewArrival = false,
            rating = 4.96,
            reviewCount = 89,
            equipmentSpecs = EquipmentSpecs(
                manufacturer = "Acaia Technologies",
                modelNumber = "Lunar 2026 Black",
                powerWatts = 5,
                pumpPressureBar = 0,
                boilerCapacityLiters = 0.0,
                boilerType = "Lithium USB-C Rechargeable",
                dimensionsCm = "10.5 x 10.5 x 1.5 cm",
                warrantyMonths = 12,
                voltage = "5V USB-C",
                sparePartsAvailable = true
            )
        ),
        // --- Accessories ---
        Product(
            id = "p_origami_dripper",
            sku = "ACC-ORI-001",
            nameEn = "Origami Dripper M (Matte Black) + Wood Ring",
            nameAr = "قمع أوريغامي M أسود مطفي مع حلقة خشبية",
            descriptionEn = "Crafted in Mino, Japan from high-density ceramic. 20 vertical ribs create the ultimate air channels for clean, sweet, and articulate pour-over extraction.",
            descriptionAr = "صُنِع في مينو باليابان من السيراميك عالي الكثافة. 20 طية رأسية توفر تدفق هواء مثالي لاستخلاص نقي وحلو متكامل.",
            category = ProductCategory.ACCESSORIES,
            subCategory = "Pour-Over Drippers",
            brand = "Origami Japan",
            regularPriceIqd = 55000,
            promoPriceIqd = 48000,
            stockQuantity = 35,
            isFeatured = false,
            isBestSeller = true,
            isNewArrival = false,
            rating = 4.9,
            reviewCount = 67
        ),
        Product(
            id = "p_wpm_pitcher",
            sku = "ACC-WPM-002",
            nameEn = "WPM Milk Pitcher 450ml (Titanium Gold Spout)",
            nameAr = "بيتشر تبخير الحليب WPM سعة 450 مل (فوهة ذهبية)",
            descriptionEn = "Engineered for world latte art champions with specialized tapered spout, balanced ergonomic handle, and ultra-durable titanium gold plating.",
            descriptionAr = "مصمم لأبطال رسم الحليب اللاتيه آرت بفوهة مدببة دقيقة، مقبض مريح متوازن، وطلاء تيتانيوم ذهبي فاخر وفائق المتانة.",
            category = ProductCategory.ACCESSORIES,
            subCategory = "Barista Pitchers",
            brand = "WPM",
            regularPriceIqd = 65000,
            promoPriceIqd = null,
            stockQuantity = 28,
            isFeatured = false,
            isBestSeller = false,
            isNewArrival = true,
            rating = 4.85,
            reviewCount = 42
        )
    )

    private fun createInitialOrders(): List<Order> = listOf(
        Order(
            id = "ord_001",
            orderNumber = "ONYX-IQ-8421",
            customerId = "user_iq_001",
            customerName = "Haider Al-Baghdadi",
            customerPhone = "+964 770 123 4567",
            deliveryAddress = DeliveryAddress(
                id = "addr_1",
                title = "Home",
                governorate = IraqiGovernorate.BAGHDAD,
                cityOrDistrict = "Al-Mansour",
                streetAddress = "Street 14, Villa 8B",
                recipientName = "Haider Al-Baghdadi",
                phone = "+964 770 123 4567"
            ),
            items = listOf(
                CartItem(
                    product = createInitialProducts().first { it.id == "p_yemen_mokha" },
                    selectedPackageGram = 250,
                    quantity = 2
                )
            ),
            subtotalIqd = 98000,
            deliveryFeeIqd = 5000,
            discountIqd = 9800,
            finalTotalIqd = 93200,
            paymentProvider = PaymentProvider.FIB,
            paymentStatus = PaymentStatus.PAID,
            orderStatus = OrderStatus.SHIPPED,
            trackingNumber = "IQ-EXP-482910",
            transactionRef = "FIB-TX-99812",
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 2
        ),
        Order(
            id = "ord_002",
            orderNumber = "ONYX-IQ-7652",
            customerId = "user_iq_002",
            customerName = "Diyar Barzan",
            customerPhone = "+964 750 334 8899",
            deliveryAddress = DeliveryAddress(
                id = "addr_erbil",
                title = "Roastery",
                governorate = IraqiGovernorate.ERBIL,
                cityOrDistrict = "Dream City",
                streetAddress = "Avenue 7, Villa 12",
                recipientName = "Diyar Barzan",
                phone = "+964 750 334 8899"
            ),
            items = listOf(
                CartItem(
                    product = createInitialProducts().first { it.id == "p_machine_lamarzocco" },
                    selectedPackageGram = 250,
                    quantity = 1
                )
            ),
            subtotalIqd = 5850000,
            deliveryFeeIqd = 0,
            discountIqd = 0,
            finalTotalIqd = 5850000,
            paymentProvider = PaymentProvider.QI_CARD,
            paymentStatus = PaymentStatus.PAID,
            orderStatus = OrderStatus.PROCESSING,
            trackingNumber = "IQ-EXP-872199",
            transactionRef = "QI-SEC-10294",
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 1
        )
    )

    private fun createInitialArticles(): List<JournalArticle> = listOf(
        JournalArticle(
            id = "art_1",
            titleEn = "Dialing In Espresso for Iraq's Climate: Humidity & Grind Dynamics",
            titleAr = "معايرة الإسبريسو في مناخ العراق: أسرار الرطوبة وتعديل درجة الطحن",
            excerptEn = "How extreme seasonal weather in Baghdad and Basra alters bean degassing, extraction speed, and puck resistance.",
            excerptAr = "كيف تؤثر التغيرات الموسمية ودرجات الحرارة في بغداد والبصرة على تفريغ غازات البن ومقاومة قرص الإسبريسو.",
            contentEn = "When brewing specialty espresso in Iraq, fluctuating ambient temperatures and dry desert air directly affect the porosity of roasted beans. To maintain silky crema and balanced extraction, baristas must micro-adjust the burr gap twice daily...",
            contentAr = "عند تحضير الإسبريسو المختص في العراق، تؤثر التقلبات المناخية الملحوظة بين الصيف والشتاء على مسامية حبوب القهوة المحمصة. للحفاظ على الكريما الذهبية والنكهات الزهرية، نوصي بمعايرة درجة الطحن مرتين يومياً...",
            author = "Kareem Hassan, Head of Quality",
            category = "Barista Guide",
            readMinutes = 5,
            dateString = "October 2026"
        ),
        JournalArticle(
            id = "art_2",
            titleEn = "The Rare Terroir of Yemen Mokha: Ancient Stepped Terraces",
            titleAr = "تاريخ بن موكا اليمني العريق: أسرار المصاطب الصخرية المعلقة",
            excerptEn = "Exploring the 500-year legacy of dry-processed heirloom varietals cultivated at altitudes exceeding 2,300 meters.",
            excerptAr = "استكشاف إرث 500 عام من زراعة السلالات التاريخية المجففة شمسياً على ارتفاعات تتجاوز 2,300 متر.",
            contentEn = "Yemeni coffee beans represent the genetic heritage of Arabica coffee worldwide. Harvested from ancient micro-climates, these cherries are slowly dried under mountain sunlight...",
            contentAr = "يمثل البن اليمني الشجرة الجينية الأصيلة للقهوة العربية حول العالم. تمنحه التربة الجبلية العالية وطرق التجفيف التراثية نكهات فريدة لا تتكرر من التين والهيل والكاكاو...",
            author = "Sarah Al-Husseini, Q-Grader",
            category = "Origins",
            readMinutes = 7,
            dateString = "September 2026"
        )
    )

    private fun createInitialCustomers(): List<UserProfile> = listOf(
        UserProfile(
            id = "user_iq_001",
            fullName = "Haider Al-Baghdadi",
            email = "haider.coffee@onyx.iq",
            phone = "+964 770 123 4567",
            role = UserRole.ADMIN,
            loyaltyPoints = 1450,
            loyaltyTier = LoyaltyTier.GOLD,
            addresses = listOf(
                DeliveryAddress(
                    id = "addr_1",
                    title = "Home Villa",
                    governorate = IraqiGovernorate.BAGHDAD,
                    cityOrDistrict = "Al-Mansour, District 603",
                    streetAddress = "Street 14, Villa 8B",
                    recipientName = "Haider Al-Baghdadi",
                    phone = "+964 770 123 4567",
                    isDefault = true
                )
            ),
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 180
        ),
        UserProfile(
            id = "user_iq_002",
            fullName = "Diyar Barzan",
            email = "diyar.barzan@erbilcafe.iq",
            phone = "+964 750 334 8899",
            role = UserRole.CUSTOMER,
            loyaltyPoints = 5850,
            loyaltyTier = LoyaltyTier.ONYX_VIP,
            addresses = listOf(
                DeliveryAddress(
                    id = "addr_erbil",
                    title = "Specialty Roastery",
                    governorate = IraqiGovernorate.ERBIL,
                    cityOrDistrict = "Dream City Commercial",
                    streetAddress = "Avenue 7, Building 12",
                    recipientName = "Diyar Barzan",
                    phone = "+964 750 334 8899",
                    isDefault = true
                )
            ),
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 95
        ),
        UserProfile(
            id = "user_iq_003",
            fullName = "Noor Al-Basri",
            email = "noor.basra.coffee@gmail.com",
            phone = "+964 780 554 1122",
            role = UserRole.CUSTOMER,
            loyaltyPoints = 680,
            loyaltyTier = LoyaltyTier.SILVER,
            addresses = listOf(
                DeliveryAddress(
                    id = "addr_basra",
                    title = "Corniche Cafe",
                    governorate = IraqiGovernorate.BASRA,
                    cityOrDistrict = "Al-Ashar Corniche",
                    streetAddress = "Shatt Al-Arab Promenade No. 4",
                    recipientName = "Noor Al-Basri",
                    phone = "+964 780 554 1122",
                    isDefault = true
                )
            ),
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 45
        ),
        UserProfile(
            id = "user_iq_004",
            fullName = "Ali Al-Najafi",
            email = "ali.najaf.barista@yahoo.com",
            phone = "+964 771 998 3344",
            role = UserRole.CUSTOMER,
            loyaltyPoints = 240,
            loyaltyTier = LoyaltyTier.BRONZE,
            addresses = listOf(
                DeliveryAddress(
                    id = "addr_najaf",
                    title = "Home",
                    governorate = IraqiGovernorate.NAJAF,
                    cityOrDistrict = "Al-Rawan Street",
                    streetAddress = "House 19, Sector 2",
                    recipientName = "Ali Al-Najafi",
                    phone = "+964 771 998 3344",
                    isDefault = true
                )
            ),
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 20
        ),
        UserProfile(
            id = "user_iq_005",
            fullName = "Zaid Al-Mosuli",
            email = "zaid.mosul.espresso@outlook.com",
            phone = "+964 751 223 7788",
            role = UserRole.CUSTOMER,
            loyaltyPoints = 820,
            loyaltyTier = LoyaltyTier.SILVER,
            addresses = listOf(
                DeliveryAddress(
                    id = "addr_mosul",
                    title = "Boutique Coffee Spot",
                    governorate = IraqiGovernorate.NINEVEH,
                    cityOrDistrict = "Al-Zuhur District",
                    streetAddress = "Near Grand Mosque Commercial",
                    recipientName = "Zaid Al-Mosuli",
                    phone = "+964 751 223 7788",
                    isDefault = true
                )
            ),
            createdAtEpoch = System.currentTimeMillis() - 86400000L * 60
        )
    )
}
