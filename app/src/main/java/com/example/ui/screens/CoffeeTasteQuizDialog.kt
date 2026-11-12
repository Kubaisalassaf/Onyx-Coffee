package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.localization.OnyxFormatter
import com.example.localization.onyxStrings
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun CoffeeTasteQuizDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    val strings = onyxStrings()

    // Quiz selections state
    var selectedAcidity by remember { mutableStateOf(AcidityPreference.BALANCED) }
    var selectedBitterness by remember { mutableStateOf(BitternessPreference.BALANCED) }
    var selectedRoast by remember { mutableStateOf(RoastPreference.MEDIUM) }
    var selectedBrew by remember { mutableStateOf(QuizBrewMethod.ESPRESSO) }
    var selectedFlavor by remember { mutableStateOf(FlavorPreference.CHOCOLATE_NUTS) }

    // Results state
    var isShowingResults by remember { mutableStateOf(false) }
    var topRecommendations by remember { mutableStateOf<List<CoffeeRecommendation>>(emptyList()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = OnyxDeepBlack,
            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(OnyxSurfaceDark, OnyxDeepBlack)
                        )
                    )
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(OnyxCoffeeBrownDark, CircleShape)
                                .border(1.dp, OnyxGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = OnyxGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.tasteFinderTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    color = OnyxGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (strings.isArabic) "اكتشف القهوة المناسبة لذوقك الخاص" else "Find your personalized cup profile",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(OnyxCardDark, CircleShape)
                            .border(1.dp, OnyxCardBorder, CircleShape)
                            .testTag("close_taste_quiz_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = OnyxCream,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = OnyxCardBorder)

                // Body: Either Quiz Form or Top 3 Results
                AnimatedVisibility(
                    visible = !isShowingResults,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    QuizQuestionsContent(
                        acidity = selectedAcidity,
                        onAcidityChange = { selectedAcidity = it },
                        bitterness = selectedBitterness,
                        onBitternessChange = { selectedBitterness = it },
                        roast = selectedRoast,
                        onRoastChange = { selectedRoast = it },
                        brewMethod = selectedBrew,
                        onBrewChange = { selectedBrew = it },
                        flavor = selectedFlavor,
                        onFlavorChange = { selectedFlavor = it },
                        onSubmit = {
                            val answers = CoffeeQuizAnswers(
                                acidity = selectedAcidity,
                                bitterness = selectedBitterness,
                                roast = selectedRoast,
                                brewMethod = selectedBrew,
                                flavor = selectedFlavor
                            )
                            topRecommendations = CoffeeTasteMatcher.findTopMatches(products, answers, limit = 3)
                            isShowingResults = true
                        }
                    )
                }

                AnimatedVisibility(
                    visible = isShowingResults,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    QuizResultsContent(
                        recommendations = topRecommendations,
                        acidity = selectedAcidity,
                        bitterness = selectedBitterness,
                        roast = selectedRoast,
                        onProductClick = onProductClick,
                        onAddToCart = onAddToCart,
                        onRetakeQuiz = { isShowingResults = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizQuestionsContent(
    acidity: AcidityPreference,
    onAcidityChange: (AcidityPreference) -> Unit,
    bitterness: BitternessPreference,
    onBitternessChange: (BitternessPreference) -> Unit,
    roast: RoastPreference,
    onRoastChange: (RoastPreference) -> Unit,
    brewMethod: QuizBrewMethod,
    onBrewChange: (QuizBrewMethod) -> Unit,
    flavor: FlavorPreference,
    onFlavorChange: (FlavorPreference) -> Unit,
    onSubmit: () -> Unit
) {
    val strings = onyxStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Intro Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnyxCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = OnyxGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.tasteFinderSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnyxWarmBeige,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // --- QUESTION 1: SOURNESS / ACIDITY ---
        item {
            QuizSectionCard(
                stepNumber = 1,
                title = strings.questionAcidity,
                badge = if (strings.isArabic) "حمضية وتوت" else "Acidity / Sourness"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AcidityPreference.values().forEach { option ->
                        val isSelected = acidity == option
                        SelectableQuizOption(
                            title = if (strings.isArabic) option.labelAr else option.labelEn,
                            subtitle = if (strings.isArabic) option.descAr else option.descEn,
                            isSelected = isSelected,
                            ratingIndicator = "${option.targetRating}/5",
                            onClick = { onAcidityChange(option) }
                        )
                    }
                }
            }
        }

        // --- QUESTION 2: BITTERNESS & STRENGTH ---
        item {
            QuizSectionCard(
                stepNumber = 2,
                title = strings.questionBitterness,
                badge = if (strings.isArabic) "مرارة وكريما" else "Bitterness & Strength"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BitternessPreference.values().forEach { option ->
                        val isSelected = bitterness == option
                        SelectableQuizOption(
                            title = if (strings.isArabic) option.labelAr else option.labelEn,
                            subtitle = if (strings.isArabic) option.descAr else option.descEn,
                            isSelected = isSelected,
                            ratingIndicator = "${option.targetRating}/5",
                            onClick = { onBitternessChange(option) }
                        )
                    }
                }
            }
        }

        // --- QUESTION 3: DEGREE OF ROASTING ---
        item {
            QuizSectionCard(
                stepNumber = 3,
                title = strings.questionRoast,
                badge = if (strings.isArabic) "درجة التحميص" else "Roasting Grade"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoastPreference.values().forEach { option ->
                        val isSelected = roast == option
                        SelectableQuizOption(
                            title = if (strings.isArabic) option.labelAr else option.labelEn,
                            subtitle = if (strings.isArabic) option.descAr else option.descEn,
                            isSelected = isSelected,
                            ratingIndicator = null,
                            onClick = { onRoastChange(option) }
                        )
                    }
                }
            }
        }

        // --- QUESTION 4: BREWING METHOD ---
        item {
            QuizSectionCard(
                stepNumber = 4,
                title = strings.questionBrew,
                badge = if (strings.isArabic) "طريقة التحضير" else "Brewing Style"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuizBrewMethod.values().forEach { option ->
                        val isSelected = brewMethod == option
                        SelectableQuizOption(
                            title = if (strings.isArabic) option.labelAr else option.labelEn,
                            subtitle = null,
                            isSelected = isSelected,
                            ratingIndicator = null,
                            onClick = { onBrewChange(option) }
                        )
                    }
                }
            }
        }

        // --- QUESTION 5: FLAVOR PROFILE NOTES ---
        item {
            QuizSectionCard(
                stepNumber = 5,
                title = strings.questionFlavor,
                badge = if (strings.isArabic) "النوتات المفضلة" else "Tasting Notes"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlavorPreference.values().forEach { option ->
                        val isSelected = flavor == option
                        SelectableQuizOption(
                            title = "${option.badge}  ${if (strings.isArabic) option.labelAr else option.labelEn}",
                            subtitle = null,
                            isSelected = isSelected,
                            ratingIndicator = null,
                            onClick = { onFlavorChange(option) }
                        )
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_taste_quiz_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnyxGold,
                    contentColor = OnyxDeepBlack
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.revealTopMatchesBtn,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun QuizResultsContent(
    recommendations: List<CoffeeRecommendation>,
    acidity: AcidityPreference,
    bitterness: BitternessPreference,
    roast: RoastPreference,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onRetakeQuiz: () -> Unit
) {
    val strings = onyxStrings()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OnyxGold.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                ) {
                    Text(
                        text = if (strings.isArabic) "نتائج تحليل الذوق الشخصي" else "TASTE PROFILE MATCH REPORT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnyxGoldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = strings.topMatchesHeader,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Serif,
                        color = OnyxCream,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = strings.topMatchesSubheader,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnyxWarmBeige,
                        lineHeight = 18.sp
                    )
                )
            }
        }

        // Summary tags of user choices
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnyxCardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.acidity,
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                        )
                        Text(
                            text = "${acidity.targetRating}/5",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.bitterness,
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                        )
                        Text(
                            text = "${bitterness.targetRating}/5",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.roastLevel,
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                        )
                        Text(
                            text = if (strings.isArabic) roast.labelAr else roast.labelEn,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // 3 Alternative Recommendation Cards
        items(recommendations) { rec ->
            RecommendationItemCard(
                recommendation = rec,
                onProductClick = { onProductClick(rec.product) },
                onAddToCart = { onAddToCart(rec.product) }
            )
        }

        // Retake / Modify Quiz Button
        item {
            OutlinedButton(
                onClick = onRetakeQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("retake_taste_quiz_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxCream),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = OnyxGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.retakeQuizBtn,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun RecommendationItemCard(
    recommendation: CoffeeRecommendation,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val strings = onyxStrings()
    val product = recommendation.product
    val specs = product.coffeeSpecs

    // Distinct badge coloring based on rank
    val (rankBadgeText, ribbonColor, ribbonBg) = when (recommendation.rank) {
        1 -> Triple(strings.rankBestMatch, OnyxGold, OnyxGold.copy(alpha = 0.2f))
        2 -> Triple(strings.rankAltTwo, Color(0xFFD4AF37), OnyxSurfaceDark)
        else -> Triple(strings.rankAltThree, OnyxWarmBeige, OnyxSurfaceDark)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProductClick)
            .testTag("rec_card_${recommendation.rank}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OnyxCardDark),
        border = androidx.compose.foundation.BorderStroke(
            width = if (recommendation.rank == 1) 1.5.dp else 1.dp,
            color = if (recommendation.rank == 1) OnyxGold else OnyxCardBorder
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Rank Header & Match Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ribbonBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (recommendation.rank == 1) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = OnyxGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = rankBadgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ribbonColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OnyxDeepBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${recommendation.matchPercentage}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = strings.matchScoreLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxCream,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Main Product Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Product Photo Thumbnail
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OnyxSurfaceDark)
                        .border(1.dp, OnyxCardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.imageResId != null) {
                        Image(
                            painter = painterResource(id = product.imageResId),
                            contentDescription = product.nameEn,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!product.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.nameEn,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Coffee,
                            contentDescription = null,
                            tint = OnyxGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (strings.isArabic) product.nameAr else product.nameEn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Origin & Roast Level Badge
                    specs?.let { sp ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OnyxCoffeeBrownDark
                            ) {
                                Text(
                                    text = if (strings.isArabic) sp.originCountryAr else sp.originCountry,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OnyxGoldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OnyxSurfaceDark
                            ) {
                                Text(
                                    text = if (strings.isArabic) sp.roastLevel.labelAr else sp.roastLevel.labelEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OnyxWarmBeige,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Price & USD Reference
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = OnyxFormatter.formatIqd(product.activePriceIqd, com.example.localization.LocalAppLanguage.current),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (product.basePriceUsd != null) {
                            Text(
                                text = "≈ $${String.format("%.2f", product.basePriceUsd)} USD",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OnyxMutedText,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }

            // Sensory Breakdown Bars (Acidity, Bitterness, Body)
            specs?.let { sp ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Acidity
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.acidity,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 9.sp)
                                )
                                Text(
                                    text = "${sp.acidityRating}/5",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxCream, fontSize = 9.sp)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { sp.acidityRating / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = OnyxGold,
                                trackColor = OnyxCardBorder
                            )
                        }

                        // Bitterness
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.bitterness,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 9.sp)
                                )
                                Text(
                                    text = "${sp.bitternessRating}/5",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxCream, fontSize = 9.sp)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { sp.bitternessRating / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = OnyxGoldDark,
                                trackColor = OnyxCardBorder
                            )
                        }

                        // Body
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = strings.body,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 9.sp)
                                )
                                Text(
                                    text = "${sp.bodyRating}/5",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxCream, fontSize = 9.sp)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { sp.bodyRating / 5f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = OnyxGoldLight,
                                trackColor = OnyxCardBorder
                            )
                        }
                    }

                    // Why it matched chips
                    val reasons = if (strings.isArabic) recommendation.matchReasonsAr else recommendation.matchReasonsEn
                    if (reasons.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            reasons.take(2).forEach { reason ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OnyxSurfaceDark,
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, OnyxCardBorder)
                                ) {
                                    Text(
                                        text = "✓ $reason",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = OnyxGoldLight,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onProductClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxCream),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = if (strings.isArabic) "التفاصيل" else "View Specs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.weight(1.4f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnyxGold,
                        contentColor = OnyxDeepBlack
                    ),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.addToCart,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizSectionCard(
    stepNumber: Int,
    title: String,
    badge: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = OnyxCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(OnyxGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$stepNumber",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxDeepBlack,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = OnyxGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = OnyxCream,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 22.sp
                )
            )

            content()
        }
    }
}

@Composable
private fun SelectableQuizOption(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    ratingIndicator: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) OnyxCoffeeBrownDark else OnyxSurfaceDark,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) OnyxGold else OnyxCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = OnyxGold,
                    unselectedColor = OnyxMutedText
                ),
                modifier = Modifier.size(20.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isSelected) OnyxCream else OnyxWarmBeige,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnyxMutedText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }

            if (ratingIndicator != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) OnyxGold else OnyxCardDark
                ) {
                    Text(
                        text = ratingIndicator,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) OnyxDeepBlack else OnyxMutedText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}
