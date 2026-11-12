package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.onyxStrings
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.FilterBar
import com.example.ui.components.ProductCard
import com.example.ui.theme.*

@Composable
fun CatalogScreen(
    products: List<Product>,
    favoriteIds: Set<String>,
    selectedCategory: ProductCategory,
    onSelectCategory: (ProductCategory) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onQuickAddToCart: (Product) -> Unit,
    onOpenTasteQuiz: () -> Unit = {}
) {
    val strings = onyxStrings()
    var searchQuery by remember { mutableStateOf("") }
    var inStockOnly by remember { mutableStateOf(false) }

    val filteredProducts = remember(products, selectedCategory, searchQuery, inStockOnly) {
        products.filter { p ->
            val matchCategory = selectedCategory == ProductCategory.ALL || p.category == selectedCategory
            val matchQuery = searchQuery.isBlank() ||
                    p.nameEn.contains(searchQuery, ignoreCase = true) ||
                    p.nameAr.contains(searchQuery, ignoreCase = true) ||
                    p.brand.contains(searchQuery, ignoreCase = true) ||
                    p.subCategory.contains(searchQuery, ignoreCase = true) ||
                    (p.coffeeSpecs?.originCountry?.contains(searchQuery, ignoreCase = true) ?: false)
            val matchStock = !inStockOnly || p.stockQuantity > 0

            matchCategory && matchQuery && matchStock
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Search & Category Filters
        FilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedCategory = selectedCategory,
            onSelectCategory = onSelectCategory
        )

        // Count and Quick Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${filteredProducts.size} ${if (strings.isArabic) "منتج" else "Items"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OnyxMutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Surface(
                    modifier = Modifier.clickable(onClick = onOpenTasteQuiz),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = OnyxCoffeeBrownDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = OnyxGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (strings.isArabic) "مستشار التذوق" else "Taste Matcher",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (strings.isArabic) "المتوفر فقط" else "In Stock",
                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxCream),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = inStockOnly,
                    onCheckedChange = { inStockOnly = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnyxDeepBlack,
                        checkedTrackColor = OnyxGold,
                        uncheckedThumbColor = OnyxMutedText,
                        uncheckedTrackColor = OnyxSurfaceDark
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Product Grid
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = OnyxGold.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (strings.isArabic) "لا توجد نتائج مطابقة لبحثك" else "No matching products found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnyxCream,
                            fontFamily = FontFamily.Serif
                        )
                    )
                    Text(
                        text = if (strings.isArabic) "جرب البحث باسم محصول آخر أو تصفح كل الأقسام" else "Try clearing your search query or selecting a different category.",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = favoriteIds.contains(product.id),
                        onToggleFavorite = { onToggleFavorite(product.id) },
                        onProductClick = { onProductClick(product) },
                        onQuickAddToCart = { onQuickAddToCart(product) }
                    )
                }
            }
        }
    }
}
