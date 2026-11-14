package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.LocalAppLanguage
import com.example.localization.OnyxFormatter
import com.example.localization.onyxStrings
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.WhatsAppNotificationHelper
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    products: List<Product>,
    orders: List<Order>,
    governorateFees: Map<IraqiGovernorate, Long>,
    exchangeRate: ExchangeRateConfig,
    onUpdateExchangeRate: (Double) -> Unit,
    onAddProduct: (Product) -> Unit,
    onUpdateStock: (productId: String, delta: Int) -> Unit,
    onSetStock: (productId: String, exactQuantity: Int) -> Unit = { _, _ -> },
    onUpdateProductPrice: (productId: String, newPrice: Long) -> Unit,
    onDeleteProduct: (productId: String) -> Unit,
    onUpdateOrderStatus: (orderId: String, newStatus: OrderStatus) -> Unit,
    onUpdateGovernorateFee: (IraqiGovernorate, Long) -> Unit,
    customers: List<UserProfile> = emptyList(),
    onGrantBonus: (customerId: String, points: Int, newTier: LoyaltyTier?, reason: String) -> Unit = { _, _, _, _ -> },
    storeSettings: StoreSettings = StoreSettings(),
    onUpdateAdminWhatsApp: (String) -> Unit = {},
    onUpdateAutoNotifyWhatsApp: (Boolean) -> Unit = {},
    websiteConfig: WebsiteIntegrationConfig = WebsiteIntegrationConfig(),
    websiteSyncLogs: List<String> = emptyList(),
    onUpdateWebsiteConfig: (WebsiteIntegrationConfig) -> Unit = {},
    onTestWebsiteConnection: () -> Unit = {},
    onSyncProductsFromWebsite: () -> Unit = {},
    onExitAdmin: () -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Metrics calculations
    val totalRevenue = remember(orders) {
        orders.filter { it.paymentStatus == PaymentStatus.PAID }.sumOf { it.finalTotalIqd }
    }
    val pendingOrdersCount = remember(orders) {
        orders.count { it.orderStatus == OrderStatus.PENDING || it.orderStatus == OrderStatus.PROCESSING || it.orderStatus == OrderStatus.CONFIRMED }
    }
    val lowStockCount = remember(products) {
        products.count { it.stockQuantity <= 10 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Dashboard Top Header
        Surface(
            color = OnyxDeepBlack,
            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(OnyxGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .border(1.dp, OnyxGold, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = OnyxGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = strings.adminTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnyxGold,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Button(
                    onClick = onExitAdmin,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnyxCardDark,
                        contentColor = OnyxCream
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("exit_admin_btn")
                ) {
                    Text(
                        text = if (strings.isArabic) "العودة للمتجر" else "Exit Admin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Tab Navigation
        val tabs = listOf(
            strings.tabOverview,
            strings.tabProducts,
            strings.tabOrders,
            strings.tabCustomers,
            strings.tabLogistics,
            strings.tabWebsite
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = OnyxSurfaceDark,
            contentColor = OnyxGold,
            edgePadding = 16.dp,
            divider = { HorizontalDivider(color = OnyxCardBorder) }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_$index")
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> AdminOverviewTab(
                totalRevenue = totalRevenue,
                totalOrders = orders.size,
                pendingOrders = pendingOrdersCount,
                lowStockCount = lowStockCount,
                recentOrders = orders.take(5),
                lowStockProducts = products.filter { it.stockQuantity <= 15 },
                exchangeRate = exchangeRate,
                onUpdateExchangeRate = onUpdateExchangeRate
            )
            1 -> AdminProductsTab(
                products = products,
                exchangeRate = exchangeRate,
                onAddProductClick = { showAddProductDialog = true },
                onUpdateStock = onUpdateStock,
                onSetStock = onSetStock,
                onDeleteProduct = onDeleteProduct
            )
            2 -> AdminOrdersTab(
                orders = orders,
                onUpdateStatus = onUpdateOrderStatus,
                storeSettings = storeSettings,
                onUpdateAdminWhatsApp = onUpdateAdminWhatsApp,
                onUpdateAutoNotifyWhatsApp = onUpdateAutoNotifyWhatsApp
            )
            3 -> AdminCustomersTab(
                customers = customers,
                orders = orders,
                onGrantBonus = onGrantBonus
            )
            4 -> AdminLogisticsTab(
                governorateFees = governorateFees,
                onUpdateFee = onUpdateGovernorateFee
            )
            5 -> AdminWebsiteTab(
                websiteConfig = websiteConfig,
                syncLogs = websiteSyncLogs,
                onUpdateConfig = onUpdateWebsiteConfig,
                onTestConnection = onTestWebsiteConnection,
                onSyncCatalog = onSyncProductsFromWebsite
            )
        }
    }

    // Add Product Modal Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            exchangeRate = exchangeRate,
            onDismiss = { showAddProductDialog = false },
            onConfirmAdd = { newProd ->
                onAddProduct(newProd)
                showAddProductDialog = false
            }
        )
    }
}

@Composable
private fun AdminOverviewTab(
    totalRevenue: Long,
    totalOrders: Int,
    pendingOrders: Int,
    lowStockCount: Int,
    recentOrders: List<Order>,
    lowStockProducts: List<Product>,
    exchangeRate: ExchangeRateConfig,
    onUpdateExchangeRate: (Double) -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- IRAQI MARKET EXCHANGE RATE CONTROL CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxCardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OnyxGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Default.CurrencyExchange,
                                contentDescription = null,
                                tint = OnyxGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = if (strings.isArabic) "سعر صرف الدولار في السوق العراقي (USD / IQD)" else "Iraqi Market Exchange Rate (USD / IQD)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = OnyxGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (strings.isArabic) "تتحدث أسعار المنتجات المربوطة بالدولار فورياً بالسوق" else "Products with base USD prices sync immediately",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val formatted100 = NumberFormat.getNumberInstance(Locale.US).format((exchangeRate.rateIqdPerUsd * 100).toInt())
                            val formatted1 = NumberFormat.getNumberInstance(Locale.US).format(exchangeRate.rateIqdPerUsd.toInt())
                            Text(
                                text = "$100 USD = $formatted100 IQD",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "1 USD = $formatted1 IQD",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxGoldLight)
                            )
                        }

                        // Quick rate adjusters (-10, +10, -50, +50)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onUpdateExchangeRate(exchangeRate.rateIqdPerUsd - 10) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                            ) {
                                Text("-10", color = OnyxCream, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onUpdateExchangeRate(exchangeRate.rateIqdPerUsd + 10) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                            ) {
                                Text("+10", color = OnyxGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // KPI Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = strings.totalRevenue,
                    value = OnyxFormatter.formatIqd(totalRevenue, language),
                    icon = Icons.Default.MonetizationOn,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = strings.totalOrders,
                    value = totalOrders.toString(),
                    icon = Icons.Default.ShoppingBag,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = strings.pendingOrders,
                    value = pendingOrders.toString(),
                    icon = Icons.Default.PendingActions,
                    accentColor = StatusWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = strings.lowStock,
                    value = "$lowStockCount items",
                    icon = Icons.Default.Inventory2,
                    accentColor = if (lowStockCount > 0) StatusError else StatusSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Orders Section
        item {
            Text(
                text = if (strings.isArabic) "آخر الطلبات الواردة" else "Recent Incoming Orders",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = OnyxGold,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        items(recentOrders) { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${order.orderNumber} • ${order.customerName}",
                            style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${order.deliveryAddress.governorate.nameEn} • ${order.paymentProvider.nameEn}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                        )
                    }
                    Text(
                        text = OnyxFormatter.formatIqd(order.finalTotalIqd, language),
                        style = MaterialTheme.typography.titleMedium.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color = OnyxGold,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = OnyxCream,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun AdminProductsTab(
    products: List<Product>,
    exchangeRate: ExchangeRateConfig,
    onAddProductClick: () -> Unit,
    onUpdateStock: (String, Int) -> Unit,
    onSetStock: (String, Int) -> Unit,
    onDeleteProduct: (String) -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current

    var searchQuery by remember { mutableStateOf("") }
    var availabilityFilter by remember { mutableStateOf("ALL") } // ALL, IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    var selectedProductForRestock by remember { mutableStateOf<Product?>(null) }

    // Inventory metrics
    val totalUnits = remember(products) { products.sumOf { it.stockQuantity } }
    val totalValueIqd = remember(products) { products.sumOf { it.activePriceIqd * it.stockQuantity } }
    val outOfStockCount = remember(products) { products.count { it.stockQuantity == 0 } }
    val lowStockCount = remember(products) { products.count { it.stockQuantity in 1..10 } }
    val inStockCount = remember(products) { products.count { it.stockQuantity > 10 } }

    val filteredProducts = remember(products, searchQuery, availabilityFilter) {
        products.filter { p ->
            val matchQuery = searchQuery.isBlank() ||
                    p.nameEn.contains(searchQuery, ignoreCase = true) ||
                    p.nameAr.contains(searchQuery, ignoreCase = true) ||
                    p.sku.contains(searchQuery, ignoreCase = true) ||
                    p.brand.contains(searchQuery, ignoreCase = true)

            val matchAvailability = when (availabilityFilter) {
                "IN_STOCK" -> p.stockQuantity > 10
                "LOW_STOCK" -> p.stockQuantity in 1..10
                "OUT_OF_STOCK" -> p.stockQuantity == 0
                else -> true
            }

            matchQuery && matchAvailability
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. ADD PRODUCT BUTTON ---
        item {
            Button(
                onClick = onAddProductClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnyxGold,
                    contentColor = OnyxDeepBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_product_admin_btn"),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = strings.addNewProduct, fontWeight = FontWeight.Bold)
            }
        }

        // --- 2. STORE INVENTORY HEALTH & VALUATION CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Icon(
                                imageVector = Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = OnyxGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.inventoryHeader,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnyxGold,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OnyxDeepBlack,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                        ) {
                            Text(
                                text = "${products.size} ${if (strings.isArabic) "منتجات فريدة" else "SKUs"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxCream),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Key Inventory Figures
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Units in Stock
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = OnyxDeepBlack,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, OnyxCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = strings.totalInventoryUnits,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                )
                                Text(
                                    text = "$totalUnits ${if (strings.isArabic) "قطعة" else "units"}",
                                    style = MaterialTheme.typography.titleMedium.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Total Inventory Value
                        Surface(
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            color = OnyxDeepBlack,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, OnyxGoldDark)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = strings.inventoryValuation,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxGoldLight, fontSize = 10.sp)
                                )
                                Text(
                                    text = OnyxFormatter.formatIqd(totalValueIqd, language),
                                    style = MaterialTheme.typography.titleMedium.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Stock Health Status Summary Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusSuccess.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, StatusSuccess)
                        ) {
                            Text(
                                text = "🟢 ${if (strings.isArabic) "متوفر: " else "In Stock: "}$inStockCount",
                                style = MaterialTheme.typography.labelSmall.copy(color = StatusSuccess, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = StatusWarning.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, StatusWarning)
                        ) {
                            Text(
                                text = "🟡 ${if (strings.isArabic) "حرجة: " else "Low: "}$lowStockCount",
                                style = MaterialTheme.typography.labelSmall.copy(color = StatusWarning, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (outOfStockCount > 0) StatusError.copy(alpha = 0.15f) else OnyxDeepBlack,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, if (outOfStockCount > 0) StatusError else OnyxCardBorder)
                        ) {
                            Text(
                                text = "🔴 ${if (strings.isArabic) "نافد: " else "Out: "}$outOfStockCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (outOfStockCount > 0) StatusError else OnyxMutedText,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. SEARCH & AVAILABILITY FILTER ROW ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.searchInventoryHint, fontSize = 12.sp, color = OnyxMutedText) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OnyxGold, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = OnyxMutedText, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OnyxDeepBlack,
                        unfocusedContainerColor = OnyxDeepBlack,
                        focusedBorderColor = OnyxGold,
                        unfocusedBorderColor = OnyxCardBorder,
                        focusedTextColor = OnyxCream,
                        unfocusedTextColor = OnyxCream
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Availability Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterOptions = listOf(
                        Triple("ALL", strings.filterAll, products.size),
                        Triple("IN_STOCK", strings.filterInStock, inStockCount),
                        Triple("LOW_STOCK", strings.filterLowStockAlert, lowStockCount),
                        Triple("OUT_OF_STOCK", strings.filterOutOfStockAlert, outOfStockCount)
                    )

                    filterOptions.forEach { (key, label, count) ->
                        val isSelected = availabilityFilter == key
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { availabilityFilter = key },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) OnyxGold else OnyxDeepBlack,
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) OnyxGold else OnyxCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) OnyxDeepBlack else OnyxCream,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "($count)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) OnyxDeepBlack else OnyxMutedText,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. PRODUCT INVENTORY LIST ---
        if (filteredProducts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OnyxDeepBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = OnyxMutedText, modifier = Modifier.size(36.dp))
                        Text(
                            text = if (strings.isArabic) "لا توجد منتجات تطابق شروط التصفية" else "No products found matching filters",
                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxMutedText)
                        )
                    }
                }
            }
        }

        items(filteredProducts, key = { it.id }) { product ->
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = when {
                        product.stockQuantity == 0 -> StatusError.copy(alpha = 0.6f)
                        product.stockQuantity <= 10 -> StatusWarning.copy(alpha = 0.6f)
                        else -> OnyxCardBorder
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Top Info Row: Photo + Titles + Availability Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Product photo thumbnail
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OnyxDeepBlack)
                                .border(1.dp, OnyxCardBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (product.imageResId != null) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = product.imageResId),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Coffee,
                                    contentDescription = null,
                                    tint = OnyxGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.isArabic) product.nameAr else product.nameEn,
                                style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "${product.brand} • SKU: ${product.sku}",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = OnyxFormatter.formatIqd(product.activePriceIqd, language),
                                    style = MaterialTheme.typography.labelMedium.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                                )
                                if (product.basePriceUsd != null) {
                                    Text(
                                        text = "($${String.format("%.2f", product.basePriceUsd)} USD)",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                    )
                                }
                            }
                        }

                        // Availability Status Pill
                        val (statusText, statusBg, statusColor) = when {
                            product.stockQuantity == 0 -> Triple(
                                if (strings.isArabic) "نفد من المخزن" else "Out of Stock",
                                StatusError.copy(alpha = 0.2f),
                                StatusError
                            )
                            product.stockQuantity <= 10 -> Triple(
                                if (strings.isArabic) "كمية حرجة (${product.stockQuantity})" else "Low (${product.stockQuantity})",
                                StatusWarning.copy(alpha = 0.2f),
                                StatusWarning
                            )
                            else -> Triple(
                                if (strings.isArabic) "متوفر (${product.stockQuantity})" else "In Stock (${product.stockQuantity})",
                                OnyxCoffeeBrownDark,
                                OnyxGold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = OnyxCardBorder.copy(alpha = 0.5f))

                    // Store Management Actions Row: Quick +/- steppers, "Set Stock" button, and Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stepper (-1 / +1)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(OnyxDeepBlack, RoundedCornerShape(8.dp))
                                .border(0.5.dp, OnyxCardBorder, RoundedCornerShape(8.dp))
                        ) {
                            IconButton(
                                onClick = { onUpdateStock(product.id, -1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Stock", tint = OnyxCream, modifier = Modifier.size(16.dp))
                            }
                            Text(
                                text = "${product.stockQuantity}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (product.stockQuantity == 0) StatusError else if (product.stockQuantity <= 10) StatusWarning else OnyxCream,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { onUpdateStock(product.id, 1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Stock", tint = OnyxGold, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Direct Restock / Set Stock Quantity Button
                        Button(
                            onClick = { selectedProductForRestock = product },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnyxCardDark,
                                contentColor = OnyxGold
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("restock_btn_${product.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.manageStockBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { onDeleteProduct(product.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusError, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Interactive Manage Stock & Restock Dialog
    selectedProductForRestock?.let { product ->
        ManageStockDialog(
            product = product,
            onDismiss = { selectedProductForRestock = null },
            onConfirmSetStock = { newTotal ->
                onSetStock(product.id, newTotal)
            }
        )
    }
}

@Composable
private fun ManageStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirmSetStock: (Int) -> Unit
) {
    var quantityText by remember { mutableStateOf(product.stockQuantity.toString()) }
    var shipmentRef by remember { mutableStateOf("") }
    val strings = onyxStrings()
    val parsedQuantity = quantityText.toIntOrNull() ?: product.stockQuantity
    val delta = parsedQuantity - product.stockQuantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = OnyxGold)
                Text(
                    text = strings.restockDialogTitle,
                    color = OnyxGold,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Product Summary Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OnyxDeepBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (product.imageResId != null) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = product.imageResId),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(OnyxSurfaceDark, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Coffee, contentDescription = null, tint = OnyxGold)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (strings.isArabic) product.nameAr else product.nameEn,
                                style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "SKU: ${product.sku} • ${product.brand}",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                            Text(
                                text = "${strings.currentStockLabel} ${product.stockQuantity} ${if (strings.isArabic) "قطع" else "units"}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (product.stockQuantity == 0) StatusError else if (product.stockQuantity <= 10) StatusWarning else OnyxGoldLight,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                // Quick Add Presets (+5, +10, +25, +50, +100)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.quickAddHeader,
                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 25, 50, 100).forEach { batch ->
                            OutlinedButton(
                                onClick = {
                                    val current = quantityText.toIntOrNull() ?: product.stockQuantity
                                    quantityText = (current + batch).toString()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$batch", color = OnyxGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Total Quantity Text Field with +/- Steppers
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.enterNewStock,
                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxWarmBeige, fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val current = quantityText.toIntOrNull() ?: product.stockQuantity
                                if (current > 0) quantityText = (current - 1).toString()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(OnyxDeepBlack, RoundedCornerShape(8.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = null, tint = OnyxCream)
                        }

                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it.filter { char -> char.isDigit() } },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = OnyxCream,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OnyxGold,
                                unfocusedBorderColor = OnyxCardBorder,
                                focusedContainerColor = OnyxDeepBlack,
                                unfocusedContainerColor = OnyxDeepBlack
                            )
                        )

                        IconButton(
                            onClick = {
                                val current = quantityText.toIntOrNull() ?: product.stockQuantity
                                quantityText = (current + 1).toString()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(OnyxDeepBlack, RoundedCornerShape(8.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = OnyxGold)
                        }
                    }

                    // Delta indicator
                    if (delta != 0) {
                        Text(
                            text = if (delta > 0) {
                                if (strings.isArabic) "زيادة توريد: +$delta قطع ستضاف للمخزن" else "Restock shipment: +$delta units will be added"
                            } else {
                                if (strings.isArabic) "تسوية خصم: $delta قطع ستخصم من المخزن" else "Stock adjustment: $delta units will be deducted"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (delta > 0) StatusSuccess else StatusWarning,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Optional Shipment Note
                OutlinedTextField(
                    value = shipmentRef,
                    onValueChange = { shipmentRef = it },
                    label = { Text(strings.restockBatchNote, fontSize = 11.sp) },
                    placeholder = { Text("e.g. WH-BAGHDAD-08", fontSize = 11.sp, color = OnyxMutedText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnyxGoldDark,
                        unfocusedBorderColor = OnyxCardBorder,
                        focusedTextColor = OnyxCream,
                        unfocusedTextColor = OnyxCream
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalQty = quantityText.toIntOrNull() ?: product.stockQuantity
                    onConfirmSetStock(finalQty)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnyxGold,
                    contentColor = OnyxDeepBlack
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.saveStockBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (strings.isArabic) "إلغاء" else "Cancel", color = OnyxMutedText)
            }
        },
        containerColor = OnyxSurfaceDark
    )
}

@Composable
private fun AdminOrdersTab(
    orders: List<Order>,
    onUpdateStatus: (String, OrderStatus) -> Unit,
    storeSettings: StoreSettings = StoreSettings(),
    onUpdateAdminWhatsApp: (String) -> Unit = {},
    onUpdateAutoNotifyWhatsApp: (Boolean) -> Unit = {}
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    var whatsAppInput by remember(storeSettings.adminWhatsAppNumber) {
        mutableStateOf(storeSettings.adminWhatsAppNumber)
    }
    var showWhatsAppSavedConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- WhatsApp Notification Settings Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_whatsapp_settings_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF25D366)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.whatsappSectionTitle,
                                style = MaterialTheme.typography.titleSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = strings.whatsappAdminNumberLabel,
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = whatsAppInput,
                            onValueChange = { whatsAppInput = it },
                            placeholder = { Text(strings.whatsappNumberHint, color = OnyxMutedText, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("whatsapp_number_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OnyxGold,
                                unfocusedBorderColor = OnyxCardBorder,
                                focusedTextColor = OnyxCream,
                                unfocusedTextColor = OnyxCream,
                                focusedContainerColor = OnyxSurfaceDark,
                                unfocusedContainerColor = OnyxSurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                onUpdateAdminWhatsApp(whatsAppInput)
                                showWhatsAppSavedConfirmation = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_whatsapp_btn")
                        ) {
                            Text(strings.whatsappSaveBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (showWhatsAppSavedConfirmation) {
                        Text(
                            text = "✓ ${strings.whatsappConfigSaved}",
                            color = Color(0xFF25D366),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable {
                            onUpdateAutoNotifyWhatsApp(!storeSettings.autoSendWhatsAppOnOrder)
                        }
                    ) {
                        Checkbox(
                            checked = storeSettings.autoSendWhatsAppOnOrder,
                            onCheckedChange = { onUpdateAutoNotifyWhatsApp(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = OnyxGold,
                                checkmarkColor = OnyxDeepBlack
                            )
                        )
                        Text(
                            text = strings.autoWhatsAppToggle,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige, fontSize = 12.sp)
                        )
                    }
                }
            }
        }

        items(orders, key = { it.id }) { order ->
            var expandedStatusMenu by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${order.orderNumber} - ${order.customerName}",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tel: ${order.customerPhone} • ${order.deliveryAddress.governorate.localizedName(language)}",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                            )
                        }

                        // Order Status Dropdown Menu
                        Box {
                            OutlinedButton(
                                onClick = { expandedStatusMenu = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxGold),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                            ) {
                                Text(
                                    text = order.orderStatus.localizedLabel(language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = expandedStatusMenu,
                                onDismissRequest = { expandedStatusMenu = false },
                                modifier = Modifier.background(OnyxCardDark)
                            ) {
                                OrderStatus.values().forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(text = st.localizedLabel(language), color = OnyxCream) },
                                        onClick = {
                                            onUpdateStatus(order.id, st)
                                            expandedStatusMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Order items summary
                    order.items.forEach { item ->
                        Text(
                            text = "• ${item.product.localizedName(language)} (${item.selectedPackageGram}g) x ${item.quantity} = ${OnyxFormatter.formatIqd(item.totalPriceIqd, language)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                        )
                    }

                    Divider(color = OnyxCardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payment: ${order.paymentProvider.localizedName(language)} (${order.paymentStatus.localizedLabel(language)})",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                        )
                        Text(
                            text = "Total: ${OnyxFormatter.formatIqd(order.finalTotalIqd, language)}",
                            style = MaterialTheme.typography.titleSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                        )
                    }

                    // WhatsApp Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Send Order Alert to Admin WhatsApp
                        OutlinedButton(
                            onClick = {
                                val message = WhatsAppNotificationHelper.generateOrderWhatsAppMessage(order, language)
                                WhatsAppNotificationHelper.openWhatsApp(context, storeSettings.adminWhatsAppNumber, message)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("order_whatsapp_notify_btn_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF25D366))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = strings.whatsappSendOrderBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Direct WhatsApp Message to Customer
                        OutlinedButton(
                            onClick = {
                                val msg = if (strings.isKurdish) {
                                    "سڵاو ${order.customerName}، لە ئۆنیکس کۆفی پەیوەندیت پێوە دەکەین دەربارەی داواکارییەکەت ${order.orderNumber}."
                                } else if (strings.isArabic) {
                                    "مرحباً ${order.customerName}، نتواصل معك من أونيكس كوفي بخصوص طلبك ${order.orderNumber}."
                                } else {
                                    "Hello ${order.customerName}, contacting you from ONYX Coffee regarding your order ${order.orderNumber}."
                                }
                                WhatsAppNotificationHelper.openWhatsApp(context, order.customerPhone, msg)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("order_whatsapp_customer_btn_${order.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = OnyxGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = strings.whatsappNotifyCustomerBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCustomersTab(
    customers: List<UserProfile>,
    orders: List<Order>,
    onGrantBonus: (customerId: String, points: Int, newTier: LoyaltyTier?, reason: String) -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomerForHistory by remember { mutableStateOf<UserProfile?>(null) }
    var selectedCustomerForBonus by remember { mutableStateOf<UserProfile?>(null) }

    val filteredCustomers = remember(customers, searchQuery) {
        customers.filter { c ->
            searchQuery.isBlank() ||
                    c.fullName.contains(searchQuery, ignoreCase = true) ||
                    c.phone.contains(searchQuery, ignoreCase = true) ||
                    c.email.contains(searchQuery, ignoreCase = true) ||
                    c.addresses.any {
                        it.governorate.nameEn.contains(searchQuery, ignoreCase = true) ||
                                it.governorate.nameAr.contains(searchQuery, ignoreCase = true)
                    }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // CRM Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = OnyxGold, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = strings.customersHeader,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = OnyxGold,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${customers.size} ${if (strings.isArabic) "عملاء مسجلين بنادي مكافآت أونيكس" else "Registered ONYX Loyalty Clients"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchClientsHint, fontSize = 12.sp, color = OnyxMutedText) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OnyxGold, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = OnyxMutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = OnyxDeepBlack,
                    unfocusedContainerColor = OnyxDeepBlack,
                    focusedBorderColor = OnyxGold,
                    unfocusedBorderColor = OnyxCardBorder,
                    focusedTextColor = OnyxCream,
                    unfocusedTextColor = OnyxCream
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        items(filteredCustomers, key = { it.id }) { customer ->
            val customerOrders = orders.filter { it.customerId == customer.id }
            val customerSpend = customerOrders.sumOf { it.finalTotalIqd }
            val defaultGov = customer.addresses.firstOrNull()?.governorate

            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(OnyxDeepBlack)
                                .border(1.dp, OnyxGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = customer.fullName.take(1),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnyxGold,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = customer.fullName,
                                    style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                )
                                if (customer.role == UserRole.ADMIN) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = OnyxGold.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "ADMIN",
                                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxGold, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${customer.phone} • ${customer.email}",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                        }

                        // Loyalty Tier Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OnyxCoffeeBrownDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
                        ) {
                            Text(
                                text = if (strings.isArabic) customer.loyaltyTier.badgeLabelAr else customer.loyaltyTier.badgeLabelEn,
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = OnyxCardBorder.copy(alpha = 0.5f))

                    // Metrics: Points & Spending
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = strings.yourPoints,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                )
                                Text(
                                    text = "${customer.loyaltyPoints} pts",
                                    style = MaterialTheme.typography.titleSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text(
                                    text = strings.totalLifetimeSpent,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                )
                                Text(
                                    text = OnyxFormatter.formatIqd(customerSpend, language),
                                    style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        if (defaultGov != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OnyxDeepBlack,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, OnyxCardBorder)
                            ) {
                                Text(
                                    text = defaultGov.nameEn,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxWarmBeige, fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Action Buttons Row: View History & Grant Bonus
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedCustomerForHistory = customer },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxCream),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.clientOrderHistory, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { selectedCustomerForBonus = customer },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnyxGold, contentColor = OnyxDeepBlack),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.grantBonusBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Customer Order History Modal Dialog
    selectedCustomerForHistory?.let { cust ->
        ClientOrderHistoryDialog(
            customer = cust,
            orders = orders.filter { it.customerId == cust.id },
            onDismiss = { selectedCustomerForHistory = null }
        )
    }

    // Grant Bonus Points Modal Dialog
    selectedCustomerForBonus?.let { cust ->
        GrantLoyaltyBonusDialog(
            customer = cust,
            onDismiss = { selectedCustomerForBonus = null },
            onConfirmGrantBonus = { points, newTier, reason ->
                onGrantBonus(cust.id, points, newTier, reason)
            }
        )
    }
}

@Composable
private fun ClientOrderHistoryDialog(
    customer: UserProfile,
    orders: List<Order>,
    onDismiss: () -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    val totalSpent = orders.sumOf { it.finalTotalIqd }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = OnyxGold)
                    Text(
                        text = strings.clientOrderHistory,
                        color = OnyxGold,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "${customer.fullName} • ${customer.phone}",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OnyxDeepBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.clientOrdersCount,
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                            )
                            Text(
                                text = "${orders.size} ${if (strings.isArabic) "طلبات" else "orders"}",
                                style = MaterialTheme.typography.titleMedium.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = strings.totalLifetimeSpent,
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                            )
                            Text(
                                text = OnyxFormatter.formatIqd(totalSpent, language),
                                style = MaterialTheme.typography.titleMedium.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.noOrdersYet,
                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxMutedText)
                        )
                    }
                } else {
                    orders.forEach { order ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = OnyxDeepBlack),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = order.orderNumber,
                                        style = MaterialTheme.typography.titleSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (order.orderStatus) {
                                            OrderStatus.DELIVERED -> StatusSuccess.copy(alpha = 0.2f)
                                            OrderStatus.CANCELLED -> StatusError.copy(alpha = 0.2f)
                                            else -> OnyxCoffeeBrownDark
                                        }
                                    ) {
                                        Text(
                                            text = if (strings.isArabic) order.orderStatus.labelAr else order.orderStatus.labelEn,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = when (order.orderStatus) {
                                                    OrderStatus.DELIVERED -> StatusSuccess
                                                    OrderStatus.CANCELLED -> StatusError
                                                    else -> OnyxGoldLight
                                                },
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                order.items.forEach { itm ->
                                    Text(
                                        text = "• ${if (strings.isArabic) itm.product.nameAr else itm.product.nameEn} (${itm.selectedPackageGram}g) × ${itm.quantity}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${order.deliveryAddress.governorate.nameEn} • ${order.paymentProvider.nameEn}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                                    )
                                    Text(
                                        text = OnyxFormatter.formatIqd(order.finalTotalIqd, language),
                                        style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = OnyxGold, contentColor = OnyxDeepBlack)
            ) {
                Text(if (strings.isArabic) "إغلاق" else "Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = OnyxSurfaceDark
    )
}

@Composable
private fun GrantLoyaltyBonusDialog(
    customer: UserProfile,
    onDismiss: () -> Unit,
    onConfirmGrantBonus: (bonusPoints: Int, newTier: LoyaltyTier?, reason: String) -> Unit
) {
    val strings = onyxStrings()
    var pointsText by remember { mutableStateOf("100") }
    var selectedTier by remember { mutableStateOf(customer.loyaltyTier) }
    var bonusReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = OnyxGold)
                Text(
                    text = strings.bonusDialogTitle,
                    color = OnyxGold,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Client current loyalty summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OnyxDeepBlack,
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = customer.fullName,
                            style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.yourPoints}: ${customer.loyaltyPoints} pts",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OnyxCoffeeBrownDark
                            ) {
                                Text(
                                    text = if (strings.isArabic) customer.loyaltyTier.badgeLabelAr else customer.loyaltyTier.badgeLabelEn,
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxGoldLight, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Preset Bonus Chips (+100, +250, +500, +1000)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.bonusPointsLabel,
                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxWarmBeige, fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100, 250, 500, 1000).forEach { preset ->
                            OutlinedButton(
                                onClick = { pointsText = preset.toString() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (pointsText == preset.toString()) OnyxGold else OnyxCardBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$preset", color = if (pointsText == preset.toString()) OnyxGold else OnyxCream, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Custom points input
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = { pointsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Points amount (نقاط المكافأة)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnyxGold,
                        unfocusedBorderColor = OnyxCardBorder,
                        focusedTextColor = OnyxCream,
                        unfocusedTextColor = OnyxCream
                    )
                )

                // Loyalty Tier Upgrade Option
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Assign Loyalty Tier (ترقية مستوى العضوية)",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxWarmBeige, fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LoyaltyTier.values().forEach { tier ->
                            val isSelected = selectedTier == tier
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTier = tier },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) OnyxGold else OnyxDeepBlack,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) OnyxGold else OnyxCardBorder)
                            ) {
                                Text(
                                    text = if (strings.isArabic) tier.badgeLabelAr else tier.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) OnyxDeepBlack else OnyxCream,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Reason / Note
                OutlinedTextField(
                    value = bonusReason,
                    onValueChange = { bonusReason = it },
                    label = { Text(strings.bonusReasonLabel, fontSize = 11.sp) },
                    placeholder = { Text("e.g. VIP bulk order bonus", fontSize = 11.sp, color = OnyxMutedText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OnyxGoldDark,
                        unfocusedBorderColor = OnyxCardBorder,
                        focusedTextColor = OnyxCream,
                        unfocusedTextColor = OnyxCream
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pts = pointsText.toIntOrNull() ?: 100
                    onConfirmGrantBonus(pts, selectedTier, bonusReason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OnyxGold, contentColor = OnyxDeepBlack),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.confirmBonusBtn, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (strings.isArabic) "إلغاء" else "Cancel", color = OnyxMutedText)
            }
        },
        containerColor = OnyxSurfaceDark
    )
}

@Composable
private fun AdminLogisticsTab(
    governorateFees: Map<IraqiGovernorate, Long>,
    onUpdateFee: (IraqiGovernorate, Long) -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = if (strings.isArabic) "إعدادات أجور التوصيل حسب المحافظة (بالدينار العراقي)" else "Governorate Delivery Fees Configuration (IQD)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = OnyxGold,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        items(IraqiGovernorate.values()) { gov ->
            val currentFee = governorateFees[gov] ?: gov.standardDeliveryFeeIqd
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${gov.nameEn} (${gov.nameAr})",
                            style = MaterialTheme.typography.titleSmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ETA: ${gov.estimatedHours}",
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onUpdateFee(gov, (currentFee - 500).coerceAtLeast(0)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "-500", tint = OnyxCream, modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = OnyxFormatter.formatIqd(currentFee, language),
                            style = MaterialTheme.typography.labelMedium.copy(color = OnyxGold, fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = { onUpdateFee(gov, currentFee + 500) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+500", tint = OnyxCream, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddProductDialog(
    exchangeRate: ExchangeRateConfig,
    onDismiss: () -> Unit,
    onConfirmAdd: (Product) -> Unit
) {
    var nameEn by remember { mutableStateOf("") }
    var nameAr by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("ONYX Reserve") }
    var priceUsdInput by remember { mutableStateOf("18.0") }
    var regularPrice by remember { mutableStateOf("27000") }
    var stock by remember { mutableStateOf("50") }
    var imageUrlInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ProductCategory.COFFEE) }

    // When USD price changes, auto-calculate IQD price
    LaunchedEffect(priceUsdInput, exchangeRate.rateIqdPerUsd) {
        val usd = priceUsdInput.toDoubleOrNull()
        if (usd != null) {
            val converted = exchangeRate.convertUsdToIqd(usd)
            regularPrice = converted.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Product", color = OnyxGold, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("English Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = nameAr,
                    onValueChange = { nameAr = it },
                    label = { Text("Arabic Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    singleLine = true
                )

                // USD Base Price
                OutlinedTextField(
                    value = priceUsdInput,
                    onValueChange = { priceUsdInput = it },
                    label = { Text("Base Price in USD ($)") },
                    supportingText = {
                        Text(
                            text = "Auto-converts @ 1 USD = ${exchangeRate.rateIqdPerUsd.toInt()} IQD",
                            color = OnyxGold,
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = regularPrice,
                    onValueChange = { regularPrice = it },
                    label = { Text("Calculated Price in IQD") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = imageUrlInput,
                    onValueChange = { imageUrlInput = it },
                    label = { Text("Product Image URL (Optional)") },
                    placeholder = { Text("https://...", fontSize = 11.sp) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Initial Stock") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isNotBlank()) {
                        val usd = priceUsdInput.toDoubleOrNull()
                        val iqd = regularPrice.toLongOrNull() ?: 25000L
                        val product = Product(
                            id = "p_${System.currentTimeMillis()}",
                            sku = "COF-GEN-${(100..999).random()}",
                            nameEn = nameEn,
                            nameAr = if (nameAr.isNotBlank()) nameAr else nameEn,
                            descriptionEn = "Premium specialty selection newly added to ONYX catalog.",
                            descriptionAr = "محصول مميز جديد تمت إضافته إلى تشكيلة أونيكس كوفي.",
                            category = selectedCategory,
                            subCategory = "Specialty Coffee",
                            brand = brand,
                            basePriceUsd = usd,
                            regularPriceIqd = iqd,
                            imageUrl = if (imageUrlInput.isNotBlank()) imageUrlInput else null,
                            stockQuantity = stock.toIntOrNull() ?: 30,
                            isNewArrival = true
                        )
                        onConfirmAdd(product)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OnyxGold, contentColor = OnyxDeepBlack)
            ) {
                Text("Add Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = OnyxMutedText) }
        },
        containerColor = OnyxSurfaceDark
    )
}

@Composable
private fun AdminWebsiteTab(
    websiteConfig: WebsiteIntegrationConfig,
    syncLogs: List<String>,
    onUpdateConfig: (WebsiteIntegrationConfig) -> Unit,
    onTestConnection: () -> Unit,
    onSyncCatalog: () -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    var urlInput by remember(websiteConfig.websiteUrl) { mutableStateOf(websiteConfig.websiteUrl) }
    var selectedPlatform by remember(websiteConfig.platformType) { mutableStateOf(websiteConfig.platformType) }
    var apiKeyInput by remember(websiteConfig.apiKey) { mutableStateOf(websiteConfig.apiKey) }
    var apiSecretInput by remember(websiteConfig.apiSecret) { mutableStateOf(websiteConfig.apiSecret) }
    var autoSyncOrders by remember(websiteConfig.autoSyncOrdersToWebsite) { mutableStateOf(websiteConfig.autoSyncOrdersToWebsite) }
    var autoSyncCatalog by remember(websiteConfig.autoSyncCatalogOnLaunch) { mutableStateOf(websiteConfig.autoSyncCatalogOnLaunch) }
    var copiedAssetLinks by remember { mutableStateOf(false) }

    val assetLinksSnippet = remember {
        com.example.data.remote.OnyxWebsiteSyncService.buildAssetLinksJson()
    }

    val statusColor = when (websiteConfig.connectionStatus) {
        WebsiteConnectionStatus.CONNECTED -> StatusSuccess
        WebsiteConnectionStatus.TESTING -> OnyxGold
        WebsiteConnectionStatus.ERROR -> StatusError
        WebsiteConnectionStatus.OFFLINE_OR_PENDING -> OnyxGoldDark
        WebsiteConnectionStatus.NOT_TESTED -> OnyxCream
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxCardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OnyxGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = OnyxGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.websiteSectionTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OnyxGold,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                        ) {
                            Text(
                                text = websiteConfig.connectionStatus.localizedLabel(language),
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = websiteConfig.statusDetail,
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                    )

                    if (websiteConfig.connectionStatus == WebsiteConnectionStatus.TESTING) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = OnyxGold,
                            trackColor = OnyxSurfaceDark
                        )
                    }
                }
            }
        }

        // Configuration Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.websiteDomainLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://onyxcoffeeiraq.com", color = OnyxMutedText) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_website_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OnyxGold,
                            unfocusedBorderColor = OnyxCardBorder,
                            focusedTextColor = OnyxCream,
                            unfocusedTextColor = OnyxCream
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = strings.websitePlatformLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        WebsitePlatformType.values().forEach { platform ->
                            val isSelected = selectedPlatform == platform
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) OnyxGold.copy(alpha = 0.16f) else OnyxCardDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) OnyxGold else OnyxCardBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPlatform = platform }
                                    .testTag("platform_${platform.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = platform.localizedLabel(language),
                                        color = if (isSelected) OnyxGold else OnyxCream,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = OnyxGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Key / Consumer Key / Bearer Token (Optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_website_api_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OnyxGold,
                            unfocusedBorderColor = OnyxCardBorder,
                            focusedTextColor = OnyxCream,
                            unfocusedTextColor = OnyxCream
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (selectedPlatform == WebsitePlatformType.WOOCOMMERCE) {
                        OutlinedTextField(
                            value = apiSecretInput,
                            onValueChange = { apiSecretInput = it },
                            label = { Text("WooCommerce Consumer Secret (cs_...)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_website_api_secret_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OnyxGold,
                                unfocusedBorderColor = OnyxCardBorder,
                                focusedTextColor = OnyxCream,
                                unfocusedTextColor = OnyxCream
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.websiteAutoSyncOrdersLabel,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = autoSyncOrders,
                            onCheckedChange = { autoSyncOrders = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnyxDeepBlack,
                                checkedTrackColor = OnyxGold
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.websiteAutoSyncCatalogLabel,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = autoSyncCatalog,
                            onCheckedChange = { autoSyncCatalog = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OnyxDeepBlack,
                                checkedTrackColor = OnyxGold
                            )
                        )
                    }

                    val saveCurrentConfig = {
                        onUpdateConfig(
                            websiteConfig.copy(
                                websiteUrl = urlInput,
                                platformType = selectedPlatform,
                                apiKey = apiKeyInput,
                                apiSecret = apiSecretInput,
                                autoSyncOrdersToWebsite = autoSyncOrders,
                                autoSyncCatalogOnLaunch = autoSyncCatalog
                            )
                        )
                    }

                    Button(
                        onClick = saveCurrentConfig,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnyxGold,
                            contentColor = OnyxDeepBlack
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("admin_website_save_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Save Website Bridge Settings", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                saveCurrentConfig()
                                onTestConnection()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("admin_website_test_btn")
                        ) {
                            Icon(imageVector = Icons.Default.WifiTethering, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.websiteTestConnectionBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                saveCurrentConfig()
                                onSyncCatalog()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxCream),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("admin_website_sync_catalog_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.websiteSyncProductsBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            com.example.data.remote.OnyxWebsiteSyncService.openWebsiteUrl(context, urlInput)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxWarmBeige),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("admin_website_visit_btn")
                    ) {
                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = strings.websiteVisitBtn, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Server Setup Blueprint & App Links Verification Card
        item {
            val normalizedBase = com.example.data.remote.OnyxWebsiteSyncService.normalizeBaseUrl(urlInput)
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Server & App Links Setup Blueprint",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = OnyxGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "1. REST API Endpoints expected on your domain:",
                        style = MaterialTheme.typography.labelMedium.copy(color = OnyxCream, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "• GET ${normalizedBase}${selectedPlatform.productsEndpoint}\n• POST ${normalizedBase}${selectedPlatform.ordersEndpoint}\n• GET ${normalizedBase}api/v1/orders/track/{orderNumber}",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Upload to ${normalizedBase}.well-known/assetlinks.json for verified Android deep links:",
                        style = MaterialTheme.typography.labelMedium.copy(color = OnyxCream, fontWeight = FontWeight.SemiBold)
                    )
                    Surface(
                        color = OnyxDeepBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = assetLinksSnippet,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnyxGoldLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(assetLinksSnippet))
                            copiedAssetLinks = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("admin_copy_assetlinks_btn")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (copiedAssetLinks) "Copied assetlinks.json!" else "Copy assetlinks.json for Server",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Sync Activity Logs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Sync Activity Log",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = OnyxGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    syncLogs.forEach { logEntry ->
                        Text(
                            text = logEntry,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = OnyxMutedText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

