package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.LocalAppLanguage
import com.example.localization.onyxStrings
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    featuredCoffees: List<Product>,
    featuredMachines: List<Product>,
    bestSellers: List<Product>,
    favoriteIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAddToCart: (Product) -> Unit,
    onNavigateToShop: (ProductCategory) -> Unit,
    onNavigateToJournal: () -> Unit,
    onOpenTasteQuiz: () -> Unit = {}
) {
    val strings = onyxStrings()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp) // space for bottom nav
    ) {
        // --- 1. HERO SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            // Hero Image
            Image(
                painter = painterResource(id = R.drawable.onyx_hero_banner_1793509575689),
                contentDescription = "ONYX Coffee Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark Luxury Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                OnyxDeepBlack.copy(alpha = 0.6f),
                                OnyxDeepBlack
                            )
                        )
                    )
            )

            // Content Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OnyxGold.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                ) {
                    Text(
                        text = if (strings.isArabic) "سوق القهوة الأول في العراق" else "IRAQ'S PREMIER COFFEE MARKETPLACE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnyxGoldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = strings.heroTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = OnyxCream,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                )

                Text(
                    text = strings.heroSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OnyxWarmBeige,
                        lineHeight = 20.sp
                    ),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onNavigateToShop(ProductCategory.COFFEE) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnyxGold,
                            contentColor = OnyxDeepBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("hero_shop_beans_btn")
                    ) {
                        Text(text = strings.heroShopBtn, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onNavigateToShop(ProductCategory.MACHINES) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxCream),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("hero_shop_machines_btn")
                    ) {
                        Text(text = strings.heroEquipmentBtn, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- 2. IRAQ LOGISTICS NOTICE BAR ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(12.dp),
            color = OnyxCardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(OnyxCoffeeBrownDark, CircleShape)
                        .border(1.dp, OnyxGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = OnyxGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (strings.isArabic) "توصيل سريع لكافة محافظات العراق" else "Fast Nationwide Delivery (18 Governorates)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (strings.isArabic) "بغداد وأربيل خلال 24-48 ساعة • شحن مجاني للطلبات فوق 100,000 د.ع" else "Baghdad & Erbil in 24-48h • Free Shipping on orders over 100,000 IQD",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                    )
                }
            }
        }

        // --- 2.5 IRAQI MARKET EXCHANGE RATE TICKER ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            shape = RoundedCornerShape(10.dp),
            color = OnyxSurfaceDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyExchange,
                    contentDescription = null,
                    tint = OnyxGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (strings.isArabic) "سعر صرف السوق: $100 = 150,000 د.ع • تحديث تلقائي لأسعار الاستيراد" else "Market Rate: $100 = 150,000 IQD • Dynamic live conversion",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = OnyxGoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // --- 3. CATEGORY SHORTCUTS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (strings.isArabic) "استكشف الأقسام الرئيسية" else "Explore Categories",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = OnyxGold,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categoryList = listOf(
                    Triple(ProductCategory.COFFEE, Icons.Default.Coffee, if (strings.isArabic) "البن المختص" else "Specialty Coffee"),
                    Triple(ProductCategory.MACHINES, Icons.Default.PrecisionManufacturing, if (strings.isArabic) "مكائن الإسبريسو" else "Espresso"),
                    Triple(ProductCategory.EQUIPMENT, Icons.Default.Tune, if (strings.isArabic) "المطاحن والمعدات" else "Grinders"),
                    Triple(ProductCategory.ACCESSORIES, Icons.Default.LocalCafe, if (strings.isArabic) "الإكسسوارات" else "Accessories")
                )

                categoryList.forEach { (cat, icon, label) ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToShop(cat) },
                        colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(OnyxCardDark, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = OnyxGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnyxCream,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // --- 3.5 SMART COFFEE PALATE MATCHER BANNER ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onOpenTasteQuiz)
                .testTag("taste_quiz_home_banner"),
            shape = RoundedCornerShape(16.dp),
            color = OnyxCardDark,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, OnyxGold)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                OnyxCoffeeBrownDark,
                                OnyxSurfaceDark
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = OnyxGold.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = OnyxGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (strings.isArabic) "مستشار التذوق الذكي" else "PALATE PROFILER",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = OnyxGoldLight,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }

                        Text(
                            text = if (strings.isArabic) "1 دقيقة ⏱️" else "1 MIN ⏱️",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                        )
                    }

                    Text(
                        text = if (strings.isArabic) "حائر في اختيار القهوة المناسبة لذوقك؟" else "Not Sure Which Coffee Matches Your Palate?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = if (strings.isArabic) "أجب عن أسئلة سريعة حول الحموضة، المرارة، ودرجة التحميص لنرشح لك أفضل 3 محاصيل تناسبك بدقة." else "Take our quick questionnaire on sourness, bitterness & roast degree to reveal your top 3 curated alternatives.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnyxWarmBeige,
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onOpenTasteQuiz,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnyxGold,
                                contentColor = OnyxDeepBlack
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.startQuizBtn,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = OnyxGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- 4. FEATURED SPECIALTY COFFEES ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (strings.isArabic) "محاصيل البن المختص الحصرية" else "Curated Specialty Coffee",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (strings.isArabic) "سلالات نادرة محصودة حديثاً ومحمصة باحتراف" else "Micro-lots & rare single origins roasted to perfection",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                    )
                }

                TextButton(onClick = { onNavigateToShop(ProductCategory.COFFEE) }) {
                    Text(
                        text = if (strings.isArabic) "عرض الكل" else "View All",
                        color = OnyxGold,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(featuredCoffees) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = favoriteIds.contains(product.id),
                        onToggleFavorite = { onToggleFavorite(product.id) },
                        onProductClick = { onProductClick(product) },
                        onQuickAddToCart = { onQuickAddToCart(product) },
                        modifier = Modifier.width(220.dp)
                    )
                }
            }
        }

        // --- 5. PROFESSIONAL ESPRESSO & COMMERCIAL GEAR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (strings.isArabic) "مكائن ومعدات القهوة الاحترافية" else "Commercial & Precision Equipment",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = if (strings.isArabic) "وكالات إيطالية وألمانية معتمدة مع ضمان وصيانة محلية" else "Official Italian & German brands with Iraq warranty",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                    )
                }

                TextButton(onClick = { onNavigateToShop(ProductCategory.MACHINES) }) {
                    Text(
                        text = if (strings.isArabic) "عرض الكل" else "View All",
                        color = OnyxGold,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(featuredMachines) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = favoriteIds.contains(product.id),
                        onToggleFavorite = { onToggleFavorite(product.id) },
                        onProductClick = { onProductClick(product) },
                        onQuickAddToCart = { onQuickAddToCart(product) },
                        modifier = Modifier.width(240.dp)
                    )
                }
            }
        }

        // --- 6. WHY CHOOSE ONYX IRAQ ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (strings.isArabic) "لماذا يختار المحترفون أونيكس كوفي؟" else "Why Choose ONYX Coffee Iraq?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = OnyxGold,
                        fontWeight = FontWeight.Bold
                    )
                )

                val reasons = listOf(
                    Triple(Icons.Default.Verified, if (strings.isArabic) "جودة عالمية معتمدة" else "100% Specialty Arabica", if (strings.isArabic) "تقييم كبس أعلى من 85 نقطة وخلو تام من العيوب." else "SCA cupping scores 85+ with zero primary defects."),
                    Triple(Icons.Default.Payment, if (strings.isArabic) "بوابات دفع عراقية رسمية" else "Local Iraqi Payment Gateways", if (strings.isArabic) "دعم كامل لـ FIB، فاست باي، كي كارد، زين كاش، والدفع عند الاستلام." else "Seamless integration with FIB, FastPay, Qi Card, Zain Cash & COD."),
                    Triple(Icons.Default.SupportAgent, if (strings.isArabic) "دعم فني وقطع غيار أصلية" else "Authorized Service & Parts", if (strings.isArabic) "صيانة معتمدة لمكائن الإسبريسو والمطاحن في بغداد وأربيل." else "Certified barista technicians & original Italian spare parts in Iraq.")
                )

                reasons.forEach { (icon, title, desc) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = OnyxGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                            )
                        }
                    }
                }
            }
        }
    }
}
