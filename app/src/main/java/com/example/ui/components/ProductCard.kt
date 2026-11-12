package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppLanguage
import com.example.localization.OnyxFormatter
import com.example.localization.onyxStrings
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.theme.*

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onProductClick: () -> Unit,
    onQuickAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, OnyxCardBorder, RoundedCornerShape(16.dp))
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Image Placeholder with Gradient & Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                OnyxCoffeeBrownDark,
                                OnyxDeepBlack
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageResId != null) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = product.imageResId),
                        contentDescription = product.nameEn,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else if (!product.imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.nameEn,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    // Central Coffee/Machine Icon Illustration
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (product.category) {
                                ProductCategory.COFFEE -> Icons.Default.Coffee
                                ProductCategory.MACHINES -> Icons.Default.PrecisionManufacturing
                                else -> Icons.Default.Coffee
                            },
                            contentDescription = null,
                            tint = OnyxGold.copy(alpha = 0.85f),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = product.brand.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxGoldLight,
                                letterSpacing = 1.sp,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Badges Row (Best Seller, New, Promo)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (product.isBestSeller) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = OnyxGold,
                            contentColor = OnyxDeepBlack
                        ) {
                            Text(
                                text = strings.bestSeller,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (product.hasDiscount) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = StatusError,
                            contentColor = OnyxWhite
                        ) {
                            Text(
                                text = "-${(((product.regularPriceIqd - product.promoPriceIqd!!) * 100) / product.regularPriceIqd)}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Favorite Toggle Button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(OnyxDeepBlack.copy(alpha = 0.6f), CircleShape)
                        .testTag("fav_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) StatusError else OnyxCream,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Stock indicator at bottom
                if (product.stockQuantity < 10) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 8.dp),
                        color = StatusWarning.copy(alpha = 0.9f),
                        contentColor = OnyxDeepBlack,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Text(
                            text = "${strings.lowStock}: ${product.stockQuantity}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Card Body Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Category & Rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (strings.isArabic) product.category.labelAr else product.category.labelEn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnyxMutedText,
                            fontSize = 10.sp
                        )
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = OnyxGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = String.format("%.1f", product.rating),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxCream,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Product Name
                Text(
                    text = product.localizedName(language),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = OnyxCream,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Flavor Notes or Tech Spec Snippet
                Spacer(modifier = Modifier.height(6.dp))
                if (product.coffeeSpecs != null) {
                    val notes = if (strings.isArabic) product.coffeeSpecs.flavorProfileAr else product.coffeeSpecs.flavorProfile
                    Text(
                        text = notes.take(2).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnyxGoldLight,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (product.equipmentSpecs != null) {
                    Text(
                        text = "${product.equipmentSpecs.powerWatts}W • ${product.equipmentSpecs.boilerType}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OnyxGoldLight,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price and Add to Cart Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        if (product.hasDiscount) {
                            Text(
                                text = OnyxFormatter.formatIqd(product.regularPriceIqd, language),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnyxMutedText,
                                    textDecoration = TextDecoration.LineThrough,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        Text(
                            text = OnyxFormatter.formatIqd(product.activePriceIqd, language),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )
                        if (product.basePriceUsd != null) {
                            Text(
                                text = "≈ $${String.format("%.2f", product.basePriceUsd)} USD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnyxMutedText,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }

                    // Add to cart button
                    FilledIconButton(
                        onClick = onQuickAddToCart,
                        shape = RoundedCornerShape(10.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = OnyxGold,
                            contentColor = OnyxDeepBlack
                        ),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("add_cart_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add to cart",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
