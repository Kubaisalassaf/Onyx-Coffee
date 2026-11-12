package com.example

import com.example.data.OnyxRepository
import com.example.localization.AppLanguage
import com.example.localization.OnyxFormatter
import com.example.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    @Before
    fun setup() {
        OnyxRepository.clearCart()
    }

    @Test
    fun testCurrencyFormatting() {
        val amount = 25000L
        val formattedEn = OnyxFormatter.formatIqd(amount, AppLanguage.ENGLISH)
        val formattedAr = OnyxFormatter.formatIqd(amount, AppLanguage.ARABIC)

        assertTrue(formattedEn.contains("25,000") && formattedEn.contains("IQD"))
        assertTrue(formattedAr.contains("25,000") && formattedAr.contains("د.ع"))
    }

    @Test
    fun testCartAdditionAndPricingMultiplier() {
        val sampleProduct = OnyxRepository.products.value.first()
        val basePrice = sampleProduct.activePriceIqd

        // 250g package
        val item250 = CartItem(sampleProduct, selectedPackageGram = 250, quantity = 2)
        assertEquals(basePrice * 2, item250.totalPriceIqd)

        // 500g package uses 1.9 multiplier
        val item500 = CartItem(sampleProduct, selectedPackageGram = 500, quantity = 1)
        val expected500 = (basePrice * 1.9).toLong()
        assertEquals(expected500, item500.totalPriceIqd)
    }

    @Test
    fun testCouponsValidation() {
        val validResult = OnyxRepository.applyCoupon("BAGHDAD10")
        assertTrue(validResult.isSuccess)
        assertEquals(10, validResult.getOrNull()?.discountPercent)

        val invalidResult = OnyxRepository.applyCoupon("NONEXISTENT_CODE")
        assertTrue(invalidResult.isFailure)
    }

    @Test
    fun testOrderPlacementAndLoyaltyPoints() {
        val initialUser = OnyxRepository.currentUser.value
        val initialPoints = initialUser.loyaltyPoints

        val sampleProduct = OnyxRepository.products.value.first()
        OnyxRepository.addToCart(sampleProduct, 250, 1)

        val deliveryAddress = initialUser.addresses.first()
        val placedOrder = OnyxRepository.placeOrder(
            deliveryAddress = deliveryAddress,
            paymentProvider = PaymentProvider.FIB,
            notes = "Test Unit Order"
        )

        assertNotNull(placedOrder.orderNumber)
        assertTrue(placedOrder.orderNumber.startsWith("ONYX-IQ-"))
        assertEquals(PaymentStatus.PAID, placedOrder.paymentStatus)
        assertEquals(OrderStatus.CONFIRMED, placedOrder.orderStatus)

        // Points earned should be total / 1000
        val pointsEarned = (placedOrder.finalTotalIqd / 1000).toInt()
        val updatedUser = OnyxRepository.currentUser.value
        assertEquals(initialPoints + pointsEarned, updatedUser.loyaltyPoints)
    }

    @Test
    fun testAdminOperations() {
        val initialProductCount = OnyxRepository.products.value.size
        val newProduct = Product(
            id = "test_p_1",
            sku = "COF-TEST-001",
            nameEn = "Test Bean",
            nameAr = "بن اختبار",
            descriptionEn = "Test Description",
            descriptionAr = "وصف اختبار",
            category = ProductCategory.COFFEE,
            subCategory = "Single Origin",
            brand = "ONYX",
            regularPriceIqd = 30000,
            stockQuantity = 20
        )

        OnyxRepository.addProduct(newProduct)
        assertEquals(initialProductCount + 1, OnyxRepository.products.value.size)

        // Adjust stock
        OnyxRepository.updateStock("test_p_1", 5)
        val updated = OnyxRepository.products.value.first { it.id == "test_p_1" }
        assertEquals(25, updated.stockQuantity)

        // Set exact stock quantity (store management)
        OnyxRepository.setStock("test_p_1", 120)
        val exactStock = OnyxRepository.products.value.first { it.id == "test_p_1" }
        assertEquals(120, exactStock.stockQuantity)

        // Delete product
        OnyxRepository.deleteProduct("test_p_1")
        assertEquals(initialProductCount, OnyxRepository.products.value.size)
    }

    @Test
    fun testGovernorateDeliveryFeeCustomization() {
        val defaultBaghdadFee = OnyxRepository.governorateDeliveryFees.value[IraqiGovernorate.BAGHDAD]
        assertNotNull(defaultBaghdadFee)

        // Admin updates fee to 4500 IQD
        OnyxRepository.updateDeliveryFee(IraqiGovernorate.BAGHDAD, 4500L)
        assertEquals(4500L, OnyxRepository.governorateDeliveryFees.value[IraqiGovernorate.BAGHDAD])
    }

    @Test
    fun testProductEntityFieldsAndDomainMapping() {
        val entity = com.example.data.local.ProductEntity(
            id = "p_colombia_geisha",
            name = "Colombia Geisha Huila Reserve",
            nameAr = "كولومبيا غيشا هويلا ريزيرف",
            description = "Prized Geisha varietal grown in mineral-rich volcanic soil.",
            descriptionAr = "سلالة الغيشا الأسطورية المزروعة في تربة هويلا البركانية.",
            priceIqd = 45000L,
            promoPriceIqd = 40000L,
            category = "COFFEE",
            imageUrl = "https://images.onyx.iq/products/geisha.jpg",
            brand = "ONYX Private Collection",
            stockQuantity = 18
        )

        assertEquals("p_colombia_geisha", entity.id)
        assertEquals("Colombia Geisha Huila Reserve", entity.name)
        assertEquals("Prized Geisha varietal grown in mineral-rich volcanic soil.", entity.description)
        assertEquals(45000L, entity.priceIqd)
        assertEquals("COFFEE", entity.category)
        assertEquals("https://images.onyx.iq/products/geisha.jpg", entity.imageUrl)

        // Test toDomainModel
        val domain = entity.toDomainModel()
        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.nameEn)
        assertEquals(entity.nameAr, domain.nameAr)
        assertEquals(entity.description, domain.descriptionEn)
        assertEquals(entity.priceIqd, domain.regularPriceIqd)
        assertEquals(entity.promoPriceIqd, domain.promoPriceIqd)
        assertEquals(ProductCategory.COFFEE, domain.category)
        assertEquals(entity.imageUrl, domain.imageUrl)

        // Test fromDomainModel
        val mappedEntity = com.example.data.local.ProductEntity.fromDomainModel(domain)
        assertEquals(entity.id, mappedEntity.id)
        assertEquals(entity.name, mappedEntity.name)
        assertEquals(entity.priceIqd, mappedEntity.priceIqd)
        assertEquals(entity.category, mappedEntity.category)
        assertEquals(entity.imageUrl, mappedEntity.imageUrl)
    }

    @Test
    fun testExchangeRateConversionAndProductRecalculation() {
        val config = ExchangeRateConfig(rateIqdPerUsd = 1500.0)
        // 18 USD * 1500 = 27,000 IQD
        val converted = config.convertUsdToIqd(18.0)
        assertEquals(27000L, converted)

        // Test updating market exchange rate to 1,520 IQD/USD
        OnyxRepository.updateExchangeRate(1520.0)
        assertEquals(1520.0, OnyxRepository.exchangeRate.value.rateIqdPerUsd, 0.001)

        // Check that products with basePriceUsd auto-recalculated
        val geisha = OnyxRepository.products.value.first { it.id == "p_colombia_geisha" }
        assertNotNull(geisha.basePriceUsd)
        val expectedIqd = (Math.round(geisha.basePriceUsd!! * 1520.0 / 250.0) * 250)
        assertEquals(expectedIqd, geisha.regularPriceIqd)
    }

    @Test
    fun testCoffeeTasteMatcherFruityLightRoast() {
        val products = OnyxRepository.products.value
        val answers = CoffeeQuizAnswers(
            acidity = AcidityPreference.HIGH,
            bitterness = BitternessPreference.MILD,
            roast = RoastPreference.LIGHT,
            brewMethod = QuizBrewMethod.POUR_OVER,
            flavor = FlavorPreference.FRUITY_FLORAL
        )

        val top3 = CoffeeTasteMatcher.findTopMatches(products, answers, limit = 3)
        assertEquals(3, top3.size)
        assertEquals(1, top3[0].rank)
        assertEquals(2, top3[1].rank)
        assertEquals(3, top3[2].rank)

        // Best match should have high match percentage (> 85%) and be a light, fruity coffee
        assertTrue(top3[0].matchPercentage >= 85)
        assertTrue(top3[0].matchPercentage >= top3[1].matchPercentage)
        assertTrue(top3[1].matchPercentage >= top3[2].matchPercentage)

        val bestProduct = top3[0].product
        assertEquals(ProductCategory.COFFEE, bestProduct.category)
        assertNotNull(bestProduct.coffeeSpecs)
        // Light roast or high acidity
        assertTrue(bestProduct.coffeeSpecs!!.roastLevel == RoastLevel.LIGHT || bestProduct.coffeeSpecs!!.acidityRating >= 4)
    }

    @Test
    fun testCoffeeTasteMatcherDarkBoldRoast() {
        val products = OnyxRepository.products.value
        val answers = CoffeeQuizAnswers(
            acidity = AcidityPreference.LOW,
            bitterness = BitternessPreference.INTENSE,
            roast = RoastPreference.DARK,
            brewMethod = QuizBrewMethod.ESPRESSO,
            flavor = FlavorPreference.SPICE_EARTHY
        )

        val top3 = CoffeeTasteMatcher.findTopMatches(products, answers, limit = 3)
        assertEquals(3, top3.size)
        // Best match should be dark/bold coffee like Sumatra Mandheling Dark Reserve
        val best = top3[0]
        assertEquals("p_sumatra_dark_roast", best.product.id)
        assertEquals(5, best.product.coffeeSpecs?.bitternessRating)
        assertEquals(1, best.product.coffeeSpecs?.acidityRating)
        assertTrue(best.matchPercentage >= 90)
    }

    @Test
    fun testCoffeeTasteMatcherBalancedProfile() {
        val products = OnyxRepository.products.value
        val answers = CoffeeQuizAnswers(
            acidity = AcidityPreference.BALANCED,
            bitterness = BitternessPreference.BALANCED,
            roast = RoastPreference.MEDIUM,
            brewMethod = QuizBrewMethod.ESPRESSO,
            flavor = FlavorPreference.CHOCOLATE_NUTS
        )

        val top3 = CoffeeTasteMatcher.findTopMatches(products, answers, limit = 3)
        assertEquals(3, top3.size)
        assertTrue(top3[0].matchPercentage >= 80)
        // Verify reasons are populated in both languages
        assertTrue(top3[0].matchReasonsEn.isNotEmpty())
        assertTrue(top3[0].matchReasonsAr.isNotEmpty())
    }

    @Test
    fun testCustomerAccountHistoryAndOrderInspection() {
        val customers = OnyxRepository.customers.value
        assertTrue("Customers list should contain seeded Iraqi customers", customers.isNotEmpty())
        
        // Check Baghdad customer Haider
        val haider = customers.firstOrNull { it.id == "user_iq_001" }
        assertNotNull(haider)
        assertEquals("Haider Al-Baghdadi", haider?.fullName)
        assertEquals(IraqiGovernorate.BAGHDAD, haider?.addresses?.firstOrNull()?.governorate)

        // Check Erbil VIP customer Diyar
        val diyar = customers.firstOrNull { it.id == "user_iq_002" }
        assertNotNull(diyar)
        assertEquals(LoyaltyTier.ONYX_VIP, diyar?.loyaltyTier)

        // Verify order history matching for customer
        val orders = OnyxRepository.orders.value
        val haiderOrders = orders.filter { it.customerId == "user_iq_001" }
        assertTrue("Haider should have previous orders", haiderOrders.isNotEmpty())
        
        val lifetimeSpent = haiderOrders.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.finalTotalIqd }
        assertTrue("Lifetime spent should be > 0", lifetimeSpent > 0L)
    }

    @Test
    fun testGrantCustomerLoyaltyBonusAndTierProgression() {
        // Customer 4 (Ali Al-Najafi) starts at BRONZE with 240 points
        val initialAli = OnyxRepository.customers.value.first { it.id == "user_iq_004" }
        assertEquals(LoyaltyTier.BRONZE, initialAli.loyaltyTier)
        val initialPoints = initialAli.loyaltyPoints

        // Grant 400 bonus points -> 640 points -> should auto-upgrade to SILVER (min 500)
        OnyxRepository.grantCustomerBonus(
            customerId = "user_iq_004",
            bonusPoints = 400,
            reason = "Ramadan Barista Gift"
        )

        val upgradedAli = OnyxRepository.customers.value.first { it.id == "user_iq_004" }
        assertEquals(initialPoints + 400, upgradedAli.loyaltyPoints)
        assertEquals(LoyaltyTier.SILVER, upgradedAli.loyaltyTier)

        // Grant direct VIP tier upgrade with 2000 points
        OnyxRepository.grantCustomerBonus(
            customerId = "user_iq_004",
            bonusPoints = 2000,
            newTier = LoyaltyTier.ONYX_VIP,
            reason = "Special Wholesale Partner Upgrade"
        )

        val vipAli = OnyxRepository.customers.value.first { it.id == "user_iq_004" }
        assertEquals(initialPoints + 2400, vipAli.loyaltyPoints)
        assertEquals(LoyaltyTier.ONYX_VIP, vipAli.loyaltyTier)

        // Test sync with active currentUser (Haider user_iq_001)
        val initialSessionUserPoints = OnyxRepository.currentUser.value.loyaltyPoints
        OnyxRepository.grantCustomerBonus(
            customerId = "user_iq_001",
            bonusPoints = 500,
            reason = "Store anniversary celebration"
        )
        assertEquals(initialSessionUserPoints + 500, OnyxRepository.currentUser.value.loyaltyPoints)
    }

    @Test
    fun testKurdishLanguageSupport() {
        val kurdish = AppLanguage.KURDISH
        assertEquals("ku", kurdish.code)
        assertEquals("کوردی", kurdish.displayName)
        assertEquals(androidx.compose.ui.unit.LayoutDirection.Rtl, kurdish.layoutDirection)

        // Test currency format
        val formattedKu = OnyxFormatter.formatIqd(35000L, kurdish)
        assertTrue(formattedKu.contains("35,000") && formattedKu.contains("دینار"))

        // Test Kurdish Strings
        val stringsKu = com.example.localization.OnyxStrings(kurdish)
        assertTrue(stringsKu.isKurdish)
        assertTrue(stringsKu.isRtl)
        assertEquals("ئۆنیکس کۆفی", stringsKu.brandName)
        assertEquals("سەرەکی", stringsKu.navHome)
        assertEquals("فرۆشگا", stringsKu.navShop)
        assertEquals("سەبەتە", stringsKu.navCart)
        assertEquals("هەژمارەکەم", stringsKu.navAccount)
        assertEquals("بەڕێوەبردن", stringsKu.navAdmin)

        // Test Kurdish Governorates
        assertEquals("هەولێر", IraqiGovernorate.ERBIL.localizedName(kurdish))
        assertEquals("سلێمانی", IraqiGovernorate.SULAYMANIYAH.localizedName(kurdish))
        assertEquals("دهۆک", IraqiGovernorate.DUHOK.localizedName(kurdish))
        assertEquals("بەغدا", IraqiGovernorate.BAGHDAD.localizedName(kurdish))

        // Test Kurdish Product Categories
        assertEquals("دەنکە قاوە", ProductCategory.COFFEE.localizedLabel(kurdish))
        assertEquals("ئامێرەکانی قاوە", ProductCategory.MACHINES.localizedLabel(kurdish))

        // Test Kurdish Loyalty Tiers
        assertEquals("باریستای برۆنزی", LoyaltyTier.BRONZE.localizedBadge(kurdish))
        assertEquals("ئەندامێتی ڕەشی ئۆنیکس VIP", LoyaltyTier.ONYX_VIP.localizedBadge(kurdish))
    }

    @Test
    fun testWhatsAppNotificationFormattingAndPhoneSanitization() {
        // Test phone sanitization
        val rawLocal1 = "0750 123 4567"
        val rawLocal2 = "+964 770 987 6543"
        val rawLocal3 = "7801122334"

        assertEquals("9647501234567", com.example.util.WhatsAppNotificationHelper.sanitizePhoneNumber(rawLocal1))
        assertEquals("9647709876543", com.example.util.WhatsAppNotificationHelper.sanitizePhoneNumber(rawLocal2))
        assertEquals("9647801122334", com.example.util.WhatsAppNotificationHelper.sanitizePhoneNumber(rawLocal3))

        // Test store settings default
        val settings = OnyxRepository.storeSettings.value
        assertNotNull(settings.adminWhatsAppNumber)
        assertTrue(settings.adminWhatsAppNumber.isNotEmpty())

        // Update admin whatsapp number
        OnyxRepository.updateAdminWhatsAppNumber("+9647509998877")
        assertEquals("+9647509998877", OnyxRepository.storeSettings.value.adminWhatsAppNumber)

        // Test generating notification message in Kurdish and Arabic
        val sampleOrder = OnyxRepository.orders.value.first()
        val kuMessage = com.example.util.WhatsAppNotificationHelper.generateOrderWhatsAppMessage(
            order = sampleOrder,
            language = AppLanguage.KURDISH
        )
        val arMessage = com.example.util.WhatsAppNotificationHelper.generateOrderWhatsAppMessage(
            order = sampleOrder,
            language = AppLanguage.ARABIC
        )

        assertTrue(kuMessage.contains(sampleOrder.orderNumber))
        assertTrue(kuMessage.contains("ئۆنیکس کۆفی"))
        assertTrue(kuMessage.contains(sampleOrder.customerName))

        assertTrue(arMessage.contains(sampleOrder.orderNumber))
        assertTrue(arMessage.contains("أونيكس كوفي"))
        assertTrue(arMessage.contains(sampleOrder.customerName))
    }
}

