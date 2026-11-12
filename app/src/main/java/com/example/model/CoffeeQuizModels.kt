package com.example.model

/**
 * Questionnaire models and recommendation scoring engine
 * for personalized coffee palate matching in ONYX Coffee.
 */

enum class AcidityPreference(
    val targetRating: Int,
    val labelEn: String,
    val labelAr: String,
    val descEn: String,
    val descAr: String
) {
    LOW(
        targetRating = 1,
        labelEn = "Low / Smooth & Gentle",
        labelAr = "منخفضة / هادئة وسلسة",
        descEn = "Very gentle, non-citrus, soft on the stomach with sweet roundness",
        descAr = "طعم هادئ جداً ومريح بدون أي حمضيات حادة، مع حلاوة طبيعية مستديرة"
    ),
    BALANCED(
        targetRating = 3,
        labelEn = "Balanced / Mild Citrus",
        labelAr = "متوازنة / انتعاش خفيف",
        descEn = "Crisp and juicy with subtle notes of green apple, plum, or mild orange",
        descAr = "توازن مثالي ومنعش مع لمسات لطيفة من التفاح الأخضر أو البرتقال الحلو"
    ),
    HIGH(
        targetRating = 5,
        labelEn = "Bright & Fruity / High Acidity",
        labelAr = "زاهية وفاكهية / حموضة بارزة",
        descEn = "Vibrant, sparkling, floral with wild berries and tropical notes",
        descAr = "حموضة فاكهية منعشة تحاكي التوت البري، الفواكه الاستوائية والأزهار العطرية"
    )
}

enum class BitternessPreference(
    val targetRating: Int,
    val labelEn: String,
    val labelAr: String,
    val descEn: String,
    val descAr: String
) {
    MILD(
        targetRating = 1,
        labelEn = "Mild & Sweet (Low Bitterness)",
        labelAr = "خفيفة وسلسة (مرارة منعدمة)",
        descEn = "Very clean, tea-like clarity, honey sweetness without heavy bitter notes",
        descAr = "سلاسة فائقة، نقاء شاي الزهور، حلاوة عسل طبيعية بدون مرارة قوية"
    ),
    BALANCED(
        targetRating = 3,
        labelEn = "Balanced / Classic Coffee Tone",
        labelAr = "متوازنة / طعم القهوة الكلاسيكي",
        descEn = "Rich traditional coffee presence, pleasant dark chocolate balance",
        descAr = "طعم القهوة الأصيل المألوف بتوازن ناعم بين الشوكولاتة الداكنة والحلاوة"
    ),
    INTENSE(
        targetRating = 5,
        labelEn = "Bold & Intense Bitterness",
        labelAr = "قوية وبارزة (مرارة محببة عميقة)",
        descEn = "Deep roasted character, baker's cocoa, syrupy body, heavy espresso bite",
        descAr = "تحميص داكن عميق، كاكاو مر، قوام كثيف وكريما غنية لمحبي القهوة القوية"
    )
}

enum class RoastPreference(
    val matches: List<RoastLevel>,
    val labelEn: String,
    val labelAr: String,
    val descEn: String,
    val descAr: String
) {
    LIGHT(
        matches = listOf(RoastLevel.LIGHT),
        labelEn = "Light Roast",
        labelAr = "تحميص فاتح",
        descEn = "Preserves the bean's native floral, tea-like and berry aromatics",
        descAr = "يحافظ على النكهات الأصلية للمحصول كالأزهار البرية والتوت"
    ),
    MEDIUM(
        matches = listOf(RoastLevel.MEDIUM_LIGHT, RoastLevel.MEDIUM),
        labelEn = "Medium Roast",
        labelAr = "تحميص متوسط",
        descEn = "Golden balance of caramelized sugar, roasted nuts, and juicy fruits",
        descAr = "توازن ذهبي بين الكراميل، المكسرات المحمصة، والنكهات المتناسقة"
    ),
    DARK(
        matches = listOf(RoastLevel.MEDIUM_DARK, RoastLevel.DARK),
        labelEn = "Dark Roast",
        labelAr = "تحميص داكن",
        descEn = "Smoky fudge, dark cocoa, robust crema and low acidity",
        descAr = "كاكاو داكن، كريما كثيفة، إيحاءات خشبية مدخنة وحمضية شبه منعدمة"
    ),
    ANY(
        matches = RoastLevel.values().toList(),
        labelEn = "Any / Roaster's Choice",
        labelAr = "أي درجة تحميص مناسبة",
        descEn = "Let our roastmaster pick the best roast profile for your taste",
        descAr = "دع خبير التحميص يختار أفضل درجة تناسب نكهاتك المفضلة"
    )
}

enum class QuizBrewMethod(
    val methods: List<BrewMethod>,
    val labelEn: String,
    val labelAr: String,
    val iconName: String
) {
    ESPRESSO(
        methods = listOf(BrewMethod.ESPRESSO),
        labelEn = "Espresso & Milk Drinks",
        labelAr = "إسبريسو ومشروبات الحليب (لاتيه، فلات وايت)",
        iconName = "espresso"
    ),
    POUR_OVER(
        methods = listOf(BrewMethod.V60, BrewMethod.CHEMEX, BrewMethod.AEROPRESS),
        labelEn = "V60, Chemex & Drip Filter",
        labelAr = "تقطير وفلتر يدوي (V60، كيمكس)",
        iconName = "filter"
    ),
    TURKISH(
        methods = listOf(BrewMethod.TURKISH),
        labelEn = "Traditional Iraqi / Turkish Ibrik",
        labelAr = "قهوة تركية / عراقية بالركوة",
        iconName = "turkish"
    ),
    COLD_BREW_PRESS(
        methods = listOf(BrewMethod.COLD_BREW, BrewMethod.FRENCH_PRESS),
        labelEn = "French Press & Cold Brew",
        labelAr = "فرينش برس أو كولد برو مبرد",
        iconName = "french_press"
    )
}

enum class FlavorPreference(
    val keywords: List<String>,
    val labelEn: String,
    val labelAr: String,
    val badge: String
) {
    FRUITY_FLORAL(
        keywords = listOf("berry", "blueberry", "jasmine", "peach", "papaya", "lemongrass", "apple", "cherry", "fruit"),
        labelEn = "Fruity & Floral",
        labelAr = "فاكهية وزهرية (توت، ياسمين، خوخ، بابايا)",
        badge = "🌸 🍓"
    ),
    CHOCOLATE_NUTS(
        keywords = listOf("chocolate", "cocoa", "fudge", "hazelnut", "caramel", "almond", "honey", "nut", "praline", "toffee"),
        labelEn = "Chocolate, Nuts & Caramel",
        labelAr = "شوكولاتة، كراميل ومكسرات محمصة",
        badge = "🍫 🌰"
    ),
    SPICE_EARTHY(
        keywords = listOf("cardamom", "fig", "spice", "cinnamon", "cedar", "smoky", "plum", "molasses", "earthy"),
        labelEn = "Spiced & Exotic Aromatics",
        labelAr = "توابل شرقية، هيل، تين مجفف وكاكاو",
        badge = "☕ 🌿"
    )
}

data class CoffeeQuizAnswers(
    val acidity: AcidityPreference = AcidityPreference.BALANCED,
    val bitterness: BitternessPreference = BitternessPreference.BALANCED,
    val roast: RoastPreference = RoastPreference.MEDIUM,
    val brewMethod: QuizBrewMethod = QuizBrewMethod.ESPRESSO,
    val flavor: FlavorPreference = FlavorPreference.CHOCOLATE_NUTS
)

data class CoffeeRecommendation(
    val product: Product,
    val matchPercentage: Int,
    val matchReasonsEn: List<String>,
    val matchReasonsAr: List<String>,
    val rank: Int // 1, 2, 3
)

object CoffeeTasteMatcher {

    fun findTopMatches(
        products: List<Product>,
        answers: CoffeeQuizAnswers,
        limit: Int = 3
    ): List<CoffeeRecommendation> {
        val coffees = products.filter { it.category == ProductCategory.COFFEE && it.coffeeSpecs != null }
        if (coffees.isEmpty()) return emptyList()

        val scoredList = coffees.map { product ->
            val specs = product.coffeeSpecs!!
            val reasonsEn = mutableListOf<String>()
            val reasonsAr = mutableListOf<String>()

            // 1. Acidity Scoring (Max 25 points)
            val acidityDiff = Math.abs(answers.acidity.targetRating - specs.acidityRating)
            val acidityScore = (25 - (acidityDiff * 5)).coerceAtLeast(5)
            if (acidityDiff == 0) {
                reasonsEn.add("Exact acidity match (${specs.acidityRating}/5)")
                reasonsAr.add("مستوى حموضة متطابق تماماً (${specs.acidityRating}/5)")
            } else if (acidityDiff <= 1) {
                reasonsEn.add("Close acidity alignment (${specs.acidityRating}/5)")
                reasonsAr.add("حموضة متقاربة جداً (${specs.acidityRating}/5)")
            }

            // 2. Bitterness Scoring (Max 25 points)
            val bitternessDiff = Math.abs(answers.bitterness.targetRating - specs.bitternessRating)
            val bitternessScore = (25 - (bitternessDiff * 5)).coerceAtLeast(5)
            if (bitternessDiff == 0) {
                reasonsEn.add("Desired bitterness level (${specs.bitternessRating}/5)")
                reasonsAr.add("مستوى المرارة المطلوب (${specs.bitternessRating}/5)")
            } else if (bitternessDiff <= 1) {
                reasonsEn.add("Pleasing bitterness intensity (${specs.bitternessRating}/5)")
                reasonsAr.add("مرارة متوازنة ومناسبة (${specs.bitternessRating}/5)")
            }

            // 3. Roast Level Match (Max 20 points)
            val roastScore = if (answers.roast == RoastPreference.ANY) {
                reasonsEn.add("Roast profile fits taste notes")
                reasonsAr.add("درجة تحميص ملائمة لإيحاءات القهوة")
                20
            } else if (answers.roast.matches.contains(specs.roastLevel)) {
                reasonsEn.add("Preferred ${specs.roastLevel.labelEn}")
                reasonsAr.add("درجة التحميص المفضلة (${specs.roastLevel.labelAr})")
                20
            } else {
                10
            }

            // 4. Brew Method Match (Max 15 points)
            val brewScore = if (answers.brewMethod.methods.contains(specs.recommendedBrew)) {
                reasonsEn.add("Optimized for ${answers.brewMethod.labelEn}")
                reasonsAr.add("مثالية لطريقة التحضير (${answers.brewMethod.labelAr})")
                15
            } else if (
                (answers.brewMethod == QuizBrewMethod.ESPRESSO && specs.roastLevel.ordinal >= RoastLevel.MEDIUM.ordinal) ||
                (answers.brewMethod == QuizBrewMethod.POUR_OVER && specs.roastLevel.ordinal <= RoastLevel.MEDIUM.ordinal)
            ) {
                reasonsEn.add("Great versatile extraction")
                reasonsAr.add("استخلاص رائع ومتعدد الاستخدامات")
                11
            } else {
                6
            }

            // 5. Flavor Notes Match (Max 15 points)
            val keywords = answers.flavor.keywords
            val matchingNotes = specs.flavorProfile.filter { note ->
                keywords.any { kw -> note.lowercase().contains(kw.lowercase()) }
            }
            val flavorScore = if (matchingNotes.isNotEmpty()) {
                reasonsEn.add("Tasting notes: ${matchingNotes.take(2).joinToString(", ")}")
                reasonsAr.add("إيحاءات متطابقة: ${matchingNotes.take(2).joinToString(", ")}")
                15
            } else {
                8
            }

            val totalRaw = acidityScore + bitternessScore + roastScore + brewScore + flavorScore
            // Scale smoothly into realistic 72% - 99% match bracket
            val matchPercentage = (totalRaw).coerceIn(68, 99)

            Triple(product, matchPercentage, Pair(reasonsEn, reasonsAr))
        }

        return scoredList
            .sortedByDescending { it.second }
            .take(limit)
            .mapIndexed { index, triple ->
                CoffeeRecommendation(
                    product = triple.first,
                    matchPercentage = triple.second,
                    matchReasonsEn = triple.third.first,
                    matchReasonsAr = triple.third.second,
                    rank = index + 1
                )
            }
    }
}
