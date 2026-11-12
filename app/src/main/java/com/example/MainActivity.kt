package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.OnyxRepository
import com.example.localization.AppLanguage
import com.example.localization.LocalAppLanguage
import com.example.localization.OnyxFormatter
import com.example.localization.OnyxStrings
import com.example.model.*
import com.example.ui.components.OnyxTopBar
import com.example.ui.components.ProductDetailDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.util.WhatsAppNotificationHelper
import kotlinx.coroutines.launch

enum class MainNavigationItem {
    HOME,
    SHOP,
    JOURNAL,
    ACCOUNT,
    ADMIN
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnyxCoffeeApp()
        }
    }
}

@Composable
fun OnyxCoffeeApp() {
    val context = LocalContext.current
    // Localization & RTL State (English, Arabic, Kurdish)
    var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalLayoutDirection provides currentLanguage.layoutDirection
    ) {
        OnyxCoffeeTheme {
            val strings = remember(currentLanguage) { OnyxStrings(currentLanguage) }
            val coroutineScope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            // Repository State Collections
            val products by OnyxRepository.products.collectAsStateWithLifecycle()
            val cartItems by OnyxRepository.cartItems.collectAsStateWithLifecycle()
            val appliedCoupon by OnyxRepository.appliedCoupon.collectAsStateWithLifecycle()
            val orders by OnyxRepository.orders.collectAsStateWithLifecycle()
            val currentUser by OnyxRepository.currentUser.collectAsStateWithLifecycle()
            val governorateFees by OnyxRepository.governorateDeliveryFees.collectAsStateWithLifecycle()
            val articles by OnyxRepository.articles.collectAsStateWithLifecycle()
            val exchangeRate by OnyxRepository.exchangeRate.collectAsStateWithLifecycle()
            val customers by OnyxRepository.customers.collectAsStateWithLifecycle()
            val storeSettings by OnyxRepository.storeSettings.collectAsStateWithLifecycle()

            // Navigation State
            var currentNav by remember { mutableStateOf(MainNavigationItem.HOME) }
            var selectedShopCategory by remember { mutableStateOf(ProductCategory.ALL) }

            // Modals & Dialogs State
            var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
            var isCartSheetOpen by remember { mutableStateOf(false) }
            var isTasteQuizOpen by remember { mutableStateOf(false) }
            var recentlyPlacedOrder by remember { mutableStateOf<Order?>(null) }

            // Handle hardware back button
            if (isCartSheetOpen) {
                BackHandler { isCartSheetOpen = false }
            } else if (isTasteQuizOpen) {
                BackHandler { isTasteQuizOpen = false }
            } else if (selectedProductForDetail != null) {
                BackHandler { selectedProductForDetail = null }
            } else if (currentNav != MainNavigationItem.HOME) {
                BackHandler { currentNav = MainNavigationItem.HOME }
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OnyxDeepBlack),
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = 80.dp)
                    )
                },
                topBar = {
                    OnyxTopBar(
                        currentLanguage = currentLanguage,
                        onToggleLanguage = {
                            currentLanguage = when (currentLanguage) {
                                AppLanguage.ENGLISH -> AppLanguage.ARABIC
                                AppLanguage.ARABIC -> AppLanguage.KURDISH
                                AppLanguage.KURDISH -> AppLanguage.ENGLISH
                            }
                        },
                        onSelectLanguage = { selectedLang ->
                            currentLanguage = selectedLang
                        },
                        userRole = currentUser.role,
                        onToggleAdminMode = {
                            currentNav = if (currentNav == MainNavigationItem.ADMIN) {
                                MainNavigationItem.HOME
                            } else {
                                MainNavigationItem.ADMIN
                            }
                        },
                        cartItemCount = cartItems.sumOf { it.quantity },
                        onOpenCart = { isCartSheetOpen = true }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = OnyxDeepBlack,
                        contentColor = OnyxGold,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .border(width = 0.5.dp, color = OnyxCardBorder)
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // Home Tab
                        NavigationBarItem(
                            selected = currentNav == MainNavigationItem.HOME,
                            onClick = { currentNav = MainNavigationItem.HOME },
                            icon = {
                                Icon(
                                    imageVector = if (currentNav == MainNavigationItem.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = strings.navHome
                                )
                            },
                            label = { Text(text = strings.navHome, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnyxDeepBlack,
                                selectedTextColor = OnyxGold,
                                unselectedIconColor = OnyxMutedText,
                                unselectedTextColor = OnyxMutedText,
                                indicatorColor = OnyxGold
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )

                        // Shop Tab
                        NavigationBarItem(
                            selected = currentNav == MainNavigationItem.SHOP,
                            onClick = { currentNav = MainNavigationItem.SHOP },
                            icon = {
                                Icon(
                                    imageVector = if (currentNav == MainNavigationItem.SHOP) Icons.Filled.LocalMall else Icons.Outlined.LocalMall,
                                    contentDescription = strings.navShop
                                )
                            },
                            label = { Text(text = strings.navShop, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnyxDeepBlack,
                                selectedTextColor = OnyxGold,
                                unselectedIconColor = OnyxMutedText,
                                unselectedTextColor = OnyxMutedText,
                                indicatorColor = OnyxGold
                            ),
                            modifier = Modifier.testTag("nav_shop")
                        )

                        // Journal Tab
                        NavigationBarItem(
                            selected = currentNav == MainNavigationItem.JOURNAL,
                            onClick = { currentNav = MainNavigationItem.JOURNAL },
                            icon = {
                                Icon(
                                    imageVector = if (currentNav == MainNavigationItem.JOURNAL) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = strings.navJournal
                                )
                            },
                            label = { Text(text = strings.navJournal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnyxDeepBlack,
                                selectedTextColor = OnyxGold,
                                unselectedIconColor = OnyxMutedText,
                                unselectedTextColor = OnyxMutedText,
                                indicatorColor = OnyxGold
                            ),
                            modifier = Modifier.testTag("nav_journal")
                        )

                        // Account Tab
                        NavigationBarItem(
                            selected = currentNav == MainNavigationItem.ACCOUNT,
                            onClick = { currentNav = MainNavigationItem.ACCOUNT },
                            icon = {
                                Icon(
                                    imageVector = if (currentNav == MainNavigationItem.ACCOUNT) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = strings.navAccount
                                )
                            },
                            label = { Text(text = strings.navAccount, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnyxDeepBlack,
                                selectedTextColor = OnyxGold,
                                unselectedIconColor = OnyxMutedText,
                                unselectedTextColor = OnyxMutedText,
                                indicatorColor = OnyxGold
                            ),
                            modifier = Modifier.testTag("nav_account")
                        )

                        // Admin Tab (if Admin role)
                        if (currentUser.role == UserRole.ADMIN) {
                            NavigationBarItem(
                                selected = currentNav == MainNavigationItem.ADMIN,
                                onClick = { currentNav = MainNavigationItem.ADMIN },
                                icon = {
                                    Icon(
                                        imageVector = if (currentNav == MainNavigationItem.ADMIN) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                                        contentDescription = strings.navAdmin
                                    )
                                },
                                label = { Text(text = strings.navAdmin, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OnyxDeepBlack,
                                    selectedTextColor = OnyxGold,
                                    unselectedIconColor = OnyxMutedText,
                                    unselectedTextColor = OnyxMutedText,
                                    indicatorColor = OnyxGold
                                ),
                                modifier = Modifier.testTag("nav_admin")
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(OnyxDeepBlack),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 960.dp)
                    ) {
                        when (currentNav) {
                            MainNavigationItem.HOME -> {
                                HomeScreen(
                                    featuredCoffees = products.filter { it.category == ProductCategory.COFFEE },
                                    featuredMachines = products.filter { it.category == ProductCategory.MACHINES || it.category == ProductCategory.EQUIPMENT },
                                    bestSellers = products.filter { it.isBestSeller },
                                    favoriteIds = currentUser.favoriteProductIds,
                                    onToggleFavorite = { pid -> OnyxRepository.toggleFavorite(pid) },
                                    onProductClick = { p -> selectedProductForDetail = p },
                                    onQuickAddToCart = { p ->
                                        OnyxRepository.addToCart(p)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = if (strings.isArabic) "تمت إضافة ${p.nameAr} إلى السلة" else "Added ${p.nameEn} to Cart",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onNavigateToShop = { cat ->
                                        selectedShopCategory = cat
                                        currentNav = MainNavigationItem.SHOP
                                    },
                                    onNavigateToJournal = { currentNav = MainNavigationItem.JOURNAL },
                                    onOpenTasteQuiz = { isTasteQuizOpen = true }
                                )
                            }

                            MainNavigationItem.SHOP -> {
                                CatalogScreen(
                                    products = products,
                                    favoriteIds = currentUser.favoriteProductIds,
                                    selectedCategory = selectedShopCategory,
                                    onSelectCategory = { selectedShopCategory = it },
                                    onToggleFavorite = { pid -> OnyxRepository.toggleFavorite(pid) },
                                    onProductClick = { p -> selectedProductForDetail = p },
                                    onQuickAddToCart = { p ->
                                        OnyxRepository.addToCart(p)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                 message = if (strings.isArabic) "تمت إضافة ${p.nameAr} إلى السلة" else "Added ${p.nameEn} to Cart",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onOpenTasteQuiz = { isTasteQuizOpen = true }
                                )
                            }

                            MainNavigationItem.JOURNAL -> {
                                JournalScreen(articles = articles)
                            }

                            MainNavigationItem.ACCOUNT -> {
                                AccountScreen(
                                    userProfile = currentUser,
                                    orders = orders,
                                    onToggleRole = { OnyxRepository.toggleUserRole() },
                                    onNavigateToShop = { currentNav = MainNavigationItem.SHOP }
                                )
                            }

                            MainNavigationItem.ADMIN -> {
                                AdminDashboardScreen(
                                    products = products,
                                    orders = orders,
                                    governorateFees = governorateFees,
                                    exchangeRate = exchangeRate,
                                    onUpdateExchangeRate = { newRate ->
                                        OnyxRepository.updateExchangeRate(newRate)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Market rate updated: 1 USD = ${newRate.toInt()} IQD")
                                        }
                                    },
                                    onAddProduct = { newP ->
                                        OnyxRepository.addProduct(newP)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Product added successfully")
                                        }
                                    },
                                    onUpdateStock = { pid, delta -> OnyxRepository.updateStock(pid, delta) },
                                    onSetStock = { pid, newQty ->
                                        OnyxRepository.setStock(pid, newQty)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (strings.isArabic) "تم تحديث رصيد المخزن إلى $newQty قطعة" else "Inventory updated to $newQty units"
                                            )
                                        }
                                    },
                                    onUpdateProductPrice = { pid, newPr -> /* price updated */ },
                                    onDeleteProduct = { pid -> OnyxRepository.deleteProduct(pid) },
                                    onUpdateOrderStatus = { oid, st -> OnyxRepository.updateOrderStatus(oid, st) },
                                    onUpdateGovernorateFee = { gov, fee -> OnyxRepository.updateDeliveryFee(gov, fee) },
                                    customers = customers,
                                    onGrantBonus = { cid, pts, tier, reason ->
                                        OnyxRepository.grantCustomerBonus(cid, pts, tier, reason)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (strings.isArabic) "تم منح المكافأة للعميل بنجاح (+${pts} نقطة)" else "Granted loyalty bonus (+${pts} pts) successfully"
                                            )
                                        }
                                    },
                                    storeSettings = storeSettings,
                                    onUpdateAdminWhatsApp = { newNum ->
                                        OnyxRepository.updateAdminWhatsAppNumber(newNum)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(strings.whatsappConfigSaved)
                                        }
                                    },
                                    onUpdateAutoNotifyWhatsApp = { enabled ->
                                        OnyxRepository.updateAutoNotifyWhatsApp(enabled)
                                    },
                                    onExitAdmin = { currentNav = MainNavigationItem.HOME }
                                )
                            }
                        }
                    }
                }
            }

            // --- Product Details Dialog ---
            selectedProductForDetail?.let { product ->
                ProductDetailDialog(
                    product = product,
                    onDismiss = { selectedProductForDetail = null },
                    onAddToCart = { gram, qty ->
                        OnyxRepository.addToCart(product, gram, qty)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (strings.isArabic) "تمت إضافة ${product.nameAr} (${gram}g) إلى السلة" else "Added ${product.nameEn} (${gram}g) to Cart",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            // --- Cart & Checkout Bottom Sheet Dialog ---
            if (isCartSheetOpen) {
                CartCheckoutSheet(
                    cartItems = cartItems,
                    appliedCoupon = appliedCoupon,
                    governorateFees = governorateFees,
                    defaultAddress = currentUser.addresses.firstOrNull { it.isDefault } ?: currentUser.addresses.firstOrNull(),
                    onUpdateQuantity = { item, qty -> OnyxRepository.updateCartQuantity(item, qty) },
                    onRemoveItem = { item -> OnyxRepository.removeFromCart(item) },
                    onApplyCoupon = { code ->
                        val res = OnyxRepository.applyCoupon(code)
                        coroutineScope.launch {
                            if (res.isSuccess) {
                                snackbarHostState.showSnackbar("Coupon ${res.getOrNull()?.code} applied (-${res.getOrNull()?.discountPercent}%)")
                            } else {
                                snackbarHostState.showSnackbar(res.exceptionOrNull()?.message ?: "Invalid coupon")
                            }
                        }
                    },
                    onRemoveCoupon = { OnyxRepository.removeCoupon() },
                    onPlaceOrder = { address, provider, notes ->
                        val ord = OnyxRepository.placeOrder(address, provider, notes)
                        ord
                    },
                    onDismiss = { isCartSheetOpen = false },
                    onOrderSuccess = { placedOrder ->
                        recentlyPlacedOrder = placedOrder
                        if (storeSettings.autoSendWhatsAppOnOrder) {
                            val msg = WhatsAppNotificationHelper.generateOrderWhatsAppMessage(placedOrder, currentLanguage)
                            WhatsAppNotificationHelper.openWhatsApp(context, storeSettings.adminWhatsAppNumber, msg)
                        }
                    }
                )
            }

            // --- Coffee Taste Quiz / Palate Profiler Dialog ---
            if (isTasteQuizOpen) {
                CoffeeTasteQuizDialog(
                    products = products,
                    onDismiss = { isTasteQuizOpen = false },
                    onProductClick = { p ->
                        selectedProductForDetail = p
                    },
                    onAddToCart = { p ->
                        OnyxRepository.addToCart(p)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (strings.isArabic) "تمت إضافة ${p.nameAr} إلى السلة" else "Added ${p.nameEn} to Cart",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            // --- Order Placed Success Confirmation Dialog ---
            recentlyPlacedOrder?.let { order ->
                AlertDialog(
                    onDismissRequest = { recentlyPlacedOrder = null },
                    title = {
                        Text(
                            text = strings.orderConfirmedTitle,
                            color = OnyxGold,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${strings.orderNumberLabel} ${order.orderNumber}",
                                color = OnyxCream,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.trackingNumberLabel} ${order.trackingNumber}",
                                color = OnyxGoldLight
                            )
                            Text(
                                text = "${strings.governorateSelect}: ${order.deliveryAddress.governorate.localizedName(currentLanguage)} (${order.deliveryAddress.governorate.estimatedHours})",
                                color = OnyxWarmBeige
                            )
                            Text(
                                text = "${strings.paymentMethod}: ${order.paymentProvider.localizedName(currentLanguage)} (${order.paymentStatus.localizedLabel(currentLanguage)})",
                                color = OnyxWarmBeige
                            )
                            Text(
                                text = "${strings.total}: ${OnyxFormatter.formatIqd(order.finalTotalIqd, currentLanguage)}",
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold
                            )

                            Divider(color = OnyxCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                            // WhatsApp Alert Button to Store Admin
                            Button(
                                onClick = {
                                    val msg = WhatsAppNotificationHelper.generateOrderWhatsAppMessage(order, currentLanguage)
                                    val sent = WhatsAppNotificationHelper.openWhatsApp(context, storeSettings.adminWhatsAppNumber, msg)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (sent) strings.whatsappSentSuccess else "Opening WhatsApp..."
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("whatsapp_order_notify_dialog_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = strings.whatsappSendOrderBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                recentlyPlacedOrder = null
                                currentNav = MainNavigationItem.ACCOUNT
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnyxGold,
                                contentColor = OnyxDeepBlack
                            )
                        ) {
                            Text(text = strings.trackOrderStatus, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { recentlyPlacedOrder = null }) {
                            Text(text = "OK", color = OnyxMutedText)
                        }
                    },
                    containerColor = OnyxSurfaceDark
                )
            }
        }
    }
}
