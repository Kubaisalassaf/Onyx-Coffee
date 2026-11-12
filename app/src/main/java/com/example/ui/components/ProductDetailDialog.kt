package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.LocalAppLanguage
import com.example.localization.OnyxFormatter
import com.example.localization.onyxStrings
import com.example.model.Product
import com.example.ui.theme.*

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (selectedGram: Int, qty: Int) -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    var selectedPackageGram by remember { mutableStateOf(product.coffeeSpecs?.defaultPackageGram ?: 250) }
    var quantity by remember { mutableIntStateOf(1) }

    val calculatedPricePerUnit = remember(product, selectedPackageGram) {
        val base = product.activePriceIqd
        when (selectedPackageGram) {
            500 -> (base * 1.9).toLong()
            1000 -> (base * 3.6).toLong()
            else -> base
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = OnyxDeepBlack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header with Back/Close and Brand
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(OnyxCardDark, CircleShape)
                            .testTag("close_detail_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = OnyxCream,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = product.brand.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = OnyxGold,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Box(modifier = Modifier.size(36.dp)) // Spacer balance
                }

                Divider(color = OnyxCardBorder)

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product Photo Hero Banner (if available)
                    if (product.imageResId != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, OnyxCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = product.imageResId),
                                contentDescription = product.nameEn,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    } else if (!product.imageUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, OnyxCardBorder, RoundedCornerShape(16.dp))
                        ) {
                            coil.compose.AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.nameEn,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }

                    // Title and Origin / Subcategory
                    Column {
                        Text(
                            text = product.localizedName(language),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                color = OnyxCream,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${product.subCategory} • SKU: ${product.sku}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                        )
                    }

                    // Rating & Reviews row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = OnyxGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${product.rating} (${product.reviewCount} ${if (strings.isKurdish) "هەڵسەنگاندن" else if (strings.isArabic) "تقييم" else "reviews"})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxCream,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Product Description
                    Text(
                        text = product.localizedDescription(language),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnyxWarmBeige,
                            lineHeight = 22.sp
                        )
                    )

                    // Coffee Specific Highlights
                    product.coffeeSpecs?.let { specs ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (strings.isArabic) "مواصفات المحصول الفاخر" else "Coffee Origin & Cupping Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnyxGold,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                // Origin & Altitude
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = strings.origin,
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                        )
                                        Text(
                                            text = if (strings.isArabic) specs.originCountryAr else specs.originCountry,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (strings.isArabic) "الارتفاع" else "Altitude",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                        )
                                        Text(
                                            text = specs.altitudeMeters,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream, fontWeight = FontWeight.SemiBold)
                                        )
                                    }
                                }

                                // Variety & Processing
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (strings.isArabic) "السلالة" else "Variety",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                        )
                                        Text(
                                            text = specs.variety,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = if (strings.isArabic) "المعالجة" else "Processing",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                        )
                                        Text(
                                            text = specs.processingMethod,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream)
                                        )
                                    }
                                }

                                // Roast Level & Brew Method
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = strings.roastLevel,
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                        )
                                        Text(
                                            text = if (strings.isArabic) specs.roastLevel.labelAr else specs.roastLevel.labelEn,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxGoldLight, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = strings.brewMethod,
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                        )
                                        Text(
                                            text = specs.recommendedBrew.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream)
                                        )
                                    }
                                }

                                // Flavor Notes Chips
                                Column {
                                    Text(
                                        text = strings.flavorProfile,
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val notes = if (strings.isArabic) specs.flavorProfileAr else specs.flavorProfile
                                        notes.forEach { note ->
                                            Surface(
                                                color = OnyxCoffeeBrownDark,
                                                shape = RoundedCornerShape(20.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
                                            ) {
                                                Text(
                                                    text = note,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = OnyxGold,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Acidity & Body Rating Bars
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Acidity / Sourness
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${strings.acidity} (${specs.acidityRating}/5)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { specs.acidityRating / 5f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = OnyxGold,
                                            trackColor = OnyxCardBorder
                                        )
                                    }

                                    // Bitterness
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${strings.bitterness} (${specs.bitternessRating}/5)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { specs.bitternessRating / 5f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = OnyxGoldDark,
                                            trackColor = OnyxCardBorder
                                        )
                                    }

                                    // Body
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${strings.body} (${specs.bodyRating}/5)",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { specs.bodyRating / 5f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = OnyxGoldLight,
                                            trackColor = OnyxCardBorder
                                        )
                                    }
                                }
                            }
                        }

                        // Package Size Selector (250g, 500g, 1000g)
                        Column {
                            Text(
                                text = strings.selectSize,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                specs.availablePackagesGram.forEach { gram ->
                                    val isSelected = gram == selectedPackageGram
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) OnyxGold else OnyxSurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) OnyxGold else OnyxCardBorder
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedPackageGram = gram }
                                            .testTag("size_${gram}g")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (gram >= 1000) "1 KG" else "$gram g",
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    color = if (isSelected) OnyxDeepBlack else OnyxCream,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Equipment Tech Specs (if machine/equipment)
                    product.equipmentSpecs?.let { eq ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (strings.isArabic) "المواصفات الفنية والهندسية" else "Technical Engineering Specifications",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnyxGold,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "• ${if (strings.isArabic) "المصنع" else "Manufacturer"}: ${eq.manufacturer} (${eq.modelNumber})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                                )
                                Text(
                                    text = "• ${if (strings.isArabic) "القدرة الكهربائية" else "Power"}: ${eq.powerWatts} Watts (${eq.voltage})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                                )
                                Text(
                                    text = "• ${if (strings.isArabic) "نظام الغلايات والضغط" else "Boiler & Pressure"}: ${eq.boilerType} / ${eq.pumpPressureBar} Bar",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                                )
                                Text(
                                    text = "• ${if (strings.isArabic) "الضمان الرسمي في العراق" else "Warranty in Iraq"}: ${eq.warrantyMonths} ${if (strings.isArabic) "شهراً ضمان شامل" else "Months Warranty"}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxGoldLight, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Sticky Bottom Bar: Quantity & Add to Cart
                Surface(
                    color = OnyxSurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Total Price Display
                        Column {
                            Text(
                                text = strings.total,
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                            )
                            Text(
                                text = OnyxFormatter.formatIqd(calculatedPricePerUnit * quantity, language),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = OnyxGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            if (product.basePriceUsd != null) {
                                Text(
                                    text = "≈ $${String.format("%.2f", product.basePriceUsd * quantity)} USD",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                                )
                            }
                        }

                        // Quantity Stepper & Add to Cart
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(OnyxDeepBlack, RoundedCornerShape(10.dp))
                                    .border(1.dp, OnyxCardBorder, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = OnyxCream,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = quantity.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnyxCream,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { quantity++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = OnyxCream,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Add to cart action button
                            Button(
                                onClick = {
                                    onAddToCart(selectedPackageGram, quantity)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnyxGold,
                                    contentColor = OnyxDeepBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                modifier = Modifier.testTag("confirm_add_to_cart_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.addToCart,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
