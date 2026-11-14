package com.example.ui.screens

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@Composable
fun AccountScreen(
    userProfile: UserProfile,
    orders: List<Order>,
    onToggleRole: () -> Unit,
    onNavigateToShop: () -> Unit,
    adminWhatsAppNumber: String = "+9647723058251",
    websiteUrl: String = "https://onyxcoffeeiraq.com",
    initialTrackingOrderQuery: String? = null,
    onClearInitialTrackingQuery: () -> Unit = {},
    onRefreshOrderFromWebsite: ((String) -> Unit)? = null
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    var selectedOrderForTracking by remember { mutableStateOf<Order?>(null) }
    var showRedeemSuccessDialog by remember { mutableStateOf(false) }
    var trackingSearchInput by remember { mutableStateOf("") }
    var trackingSearchNotFound by remember { mutableStateOf(false) }

    LaunchedEffect(initialTrackingOrderQuery, orders) {
        val q = initialTrackingOrderQuery?.trim()
        if (!q.isNullOrEmpty()) {
            trackingSearchInput = q
            val matched = orders.firstOrNull {
                it.orderNumber.equals(q, ignoreCase = true) ||
                    it.trackingNumber.equals(q, ignoreCase = true) ||
                    it.orderNumber.contains(q, ignoreCase = true) ||
                    it.trackingNumber.contains(q, ignoreCase = true)
            }
            if (matched != null) {
                selectedOrderForTracking = matched
                trackingSearchNotFound = false
            } else {
                trackingSearchNotFound = true
            }
            onClearInitialTrackingQuery()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. USER PROFILE HEADER CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(OnyxGold, OnyxCoffeeBrownDark)
                                )
                            )
                            .border(2.dp, OnyxGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userProfile.fullName.take(1),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = OnyxDeepBlack,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = userProfile.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (userProfile.role == UserRole.ADMIN) OnyxGold else OnyxCardDark,
                                contentColor = if (userProfile.role == UserRole.ADMIN) OnyxDeepBlack else OnyxWarmBeige
                            ) {
                                Text(
                                    text = if (userProfile.role == UserRole.ADMIN) "ADMIN" else "MEMBER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = userProfile.phone,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                        )
                        Text(
                            text = userProfile.email,
                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                        )
                    }

                    // Role switch button for testing
                    IconButton(
                        onClick = onToggleRole,
                        modifier = Modifier
                            .size(36.dp)
                            .background(OnyxCardDark, CircleShape)
                            .testTag("account_role_toggle_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Toggle Role",
                            tint = OnyxGold
                        )
                    }
                }
            }
        }

        // --- 2. ONYX REWARDS & LOYALTY CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxDeepBlack),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, OnyxGoldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(OnyxCoffeeBrownDark.copy(alpha = 0.5f), OnyxDeepBlack)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = strings.onyxRewards,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnyxGold,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = userProfile.loyaltyTier.localizedBadge(language),
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxCream)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OnyxGold)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${userProfile.loyaltyPoints} PTS",
                                    color = OnyxDeepBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Progress to next tier
                        Column {
                            val maxTierPoints = 3000f
                            val progress = (userProfile.loyaltyPoints / maxTierPoints).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = OnyxGold,
                                trackColor = OnyxCardDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.nextTierTip,
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                        }

                        // Redeem points action
                        Button(
                            onClick = { showRedeemSuccessDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OnyxCardDark,
                                contentColor = OnyxGold
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark),
                            modifier = Modifier.fillMaxWidth().testTag("redeem_points_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (strings.isArabic) "استبدال 500 نقطة بقسيمة 10,000 د.ع" else "Redeem 500 Pts for 10,000 IQD Voucher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // --- 3. CUSTOMER ORDERS & TRACKING ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = when (language) {
                        com.example.localization.AppLanguage.KURDISH -> "مێژووی داواکاری و بەدواداچوونی ڕاستەوخۆ"
                        com.example.localization.AppLanguage.ARABIC -> "سجل الطلبات والتتبع المباشر"
                        com.example.localization.AppLanguage.ENGLISH -> "Order History & Live Courier Tracking"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = OnyxCream,
                        fontWeight = FontWeight.Bold
                    )
                )

                // Quick Track Order by Order Number or Tracking Code
                Card(
                    colors = CardDefaults.cardColors(containerColor = OnyxCardDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("order_tracking_lookup_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (language) {
                                com.example.localization.AppLanguage.KURDISH -> "بەدواداچوونی خێرا بە ژمارەی داواکاری یان کۆدی بارکردن"
                                com.example.localization.AppLanguage.ARABIC -> "تتبع فوري برقم الطلب أو رمز الشحنة"
                                com.example.localization.AppLanguage.ENGLISH -> "Quick Track by Order # or Tracking Code"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = trackingSearchInput,
                                onValueChange = {
                                    trackingSearchInput = it
                                    trackingSearchNotFound = false
                                },
                                placeholder = {
                                    Text(
                                        text = "ONYX-IQ-... / IQ-EXP-...",
                                        color = OnyxMutedText,
                                        fontSize = 12.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tracking_lookup_input"),
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
                                    val query = trackingSearchInput.trim()
                                    val matched = if (query.isNotEmpty()) {
                                        orders.firstOrNull {
                                            it.orderNumber.equals(query, ignoreCase = true) ||
                                                it.trackingNumber.equals(query, ignoreCase = true) ||
                                                it.orderNumber.contains(query, ignoreCase = true) ||
                                                it.trackingNumber.contains(query, ignoreCase = true)
                                        }
                                    } else null
                                    if (matched != null) {
                                        trackingSearchNotFound = false
                                        selectedOrderForTracking = matched
                                    } else {
                                        trackingSearchNotFound = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnyxGold,
                                    contentColor = OnyxDeepBlack
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("tracking_lookup_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = strings.trackOrderStatus,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (language) {
                                        com.example.localization.AppLanguage.KURDISH -> "بەدواداچوون"
                                        com.example.localization.AppLanguage.ARABIC -> "تتبع"
                                        com.example.localization.AppLanguage.ENGLISH -> "Track"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (trackingSearchNotFound) {
                            Text(
                                text = when (language) {
                                    com.example.localization.AppLanguage.KURDISH -> "هیچ داواکارییەک بەم ژمارەیە نەدۆزرایەوە. دڵنیابە لە ژمارەی داواکارییەکە."
                                    com.example.localization.AppLanguage.ARABIC -> "لم يتم العثور على طلب بهذا الرقم. يرجى التحقق من رمز الطلب أو التتبع."
                                    com.example.localization.AppLanguage.ENGLISH -> "No matching order found. Please check your Order # or Tracking Ref."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = StatusError, fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (strings.isArabic) "لم تقم بطلب أي منتجات بعد" else "No orders placed yet",
                            style = MaterialTheme.typography.bodyMedium.copy(color = OnyxMutedText)
                        )
                    }
                }
            }
        } else {
            items(orders, key = { it.id }) { order ->
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
                        // Order header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = order.orderNumber,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = OnyxCream,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${strings.trackingNumberLabel} ${order.trackingNumber}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                )
                            }

                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (order.orderStatus) {
                                    OrderStatus.DELIVERED -> StatusSuccess.copy(alpha = 0.2f)
                                    OrderStatus.SHIPPED, OrderStatus.PROCESSING -> OnyxGold.copy(alpha = 0.2f)
                                    else -> OnyxCardDark
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when (order.orderStatus) {
                                        OrderStatus.DELIVERED -> StatusSuccess
                                        OrderStatus.SHIPPED, OrderStatus.PROCESSING -> OnyxGold
                                        else -> OnyxCardBorder
                                    }
                                )
                            ) {
                                Text(
                                    text = order.orderStatus.localizedLabel(language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = when (order.orderStatus) {
                                            OrderStatus.DELIVERED -> StatusSuccess
                                            OrderStatus.SHIPPED, OrderStatus.PROCESSING -> OnyxGold
                                            else -> OnyxCream
                                        },
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Order Items preview
                        order.items.forEach { item ->
                            Text(
                                text = "• ${item.product.localizedName(language)} (x${item.quantity})",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                            )
                        }

                        Divider(color = OnyxCardBorder)

                        // Order Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${order.deliveryAddress.governorate.localizedName(language)} • ${order.paymentProvider.localizedName(language)}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 10.sp)
                                )
                                Text(
                                    text = OnyxFormatter.formatIqd(order.finalTotalIqd, language),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = OnyxGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = { selectedOrderForTracking = order },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxCream),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("track_order_${order.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timeline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = OnyxGold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.trackOrderStatus,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 4. WHATSAPP & CUSTOMER SUPPORT ---
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF25D366).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(0xFF25D366), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = when (language) {
                                    com.example.localization.AppLanguage.KURDISH -> "پشتگیری کڕیاران لە واتسئاپ"
                                    com.example.localization.AppLanguage.ARABIC -> "خدمة العملاء عبر واتساب"
                                    com.example.localization.AppLanguage.ENGLISH -> "WhatsApp Concierge Support"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = when (language) {
                                    com.example.localization.AppLanguage.KURDISH -> "ڕاوێژکاری باریستا و پشتگیری ٢٤ کاتژمێری ($adminWhatsAppNumber)"
                                    com.example.localization.AppLanguage.ARABIC -> "استشارات باريستا ودعم فني على مدار الساعة ($adminWhatsAppNumber)"
                                    com.example.localization.AppLanguage.ENGLISH -> "24/7 Barista consultations & support ($adminWhatsAppNumber)"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val greeting = when (language) {
                                com.example.localization.AppLanguage.KURDISH -> "سڵاو ئۆنیکس کۆفی عێراق، پێویستم بە یارمەتییە."
                                com.example.localization.AppLanguage.ARABIC -> "مرحباً أونيكس كوفي العراق، أحتاج إلى مساعدة."
                                com.example.localization.AppLanguage.ENGLISH -> "Hello ONYX Coffee Iraq, I would like customer support."
                            }
                            WhatsAppNotificationHelper.openWhatsApp(context, adminWhatsAppNumber, greeting)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = OnyxDeepBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("whatsapp_concierge_support_btn")
                    ) {
                        Text(text = "WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- 5. OFFICIAL WEBSITE (onyxcoffeeiraq.com) ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_official_website_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(OnyxGold.copy(alpha = 0.16f), CircleShape)
                                .border(1.dp, OnyxGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = OnyxGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = strings.websiteOfficialCardTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = strings.websiteOfficialCardSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = {
                            com.example.data.remote.OnyxWebsiteSyncService.openWebsiteUrl(context, websiteUrl)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("account_open_website_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = strings.websiteVisitBtn,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Open",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // --- Order Tracking Modal Dialog ---
    selectedOrderForTracking?.let { order ->
        AlertDialog(
            onDismissRequest = { selectedOrderForTracking = null },
            title = {
                Text(
                    text = "${strings.trackOrderStatus} - ${order.orderNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = OnyxGold,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "${strings.trackingNumberLabel} ${order.trackingNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                    )
                    Text(
                        text = "${strings.governorateSelect}: ${order.deliveryAddress.governorate.localizedName(language)} • ${order.deliveryAddress.streetAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxWarmBeige)
                    )
                    Text(
                        text = "${strings.estimatedArrival} ${order.deliveryAddress.governorate.estimatedHours}",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                    )

                    Divider(color = OnyxCardBorder)

                    // Timeline Steps
                    val steps = listOf(
                        OrderStatus.CONFIRMED,
                        OrderStatus.PROCESSING,
                        OrderStatus.READY_FOR_DISPATCH,
                        OrderStatus.SHIPPED,
                        OrderStatus.DELIVERED
                    )

                    steps.forEach { step ->
                        val isCompleted = order.orderStatus.stepIndex >= step.stepIndex
                        val isCurrent = order.orderStatus == step
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isCompleted) (if (isCurrent) OnyxGold else StatusSuccess) else OnyxSubtle,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = step.localizedLabel(language),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isCompleted) OnyxCream else OnyxMutedText,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }

                    Divider(color = OnyxCardBorder)

                    if (onRefreshOrderFromWebsite != null) {
                        OutlinedButton(
                            onClick = { onRefreshOrderFromWebsite(order.orderNumber) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = OnyxGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("track_order_refresh_website_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OnyxGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.websiteRefreshTrackingBtn,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Ask about order on WhatsApp
                    Button(
                        onClick = {
                            val inquiry = when (language) {
                                com.example.localization.AppLanguage.KURDISH -> "سڵاو ئۆنیکس کۆفی، دەمەوێت پرسیار بکەم دەربارەی دۆخی داواکارییەکەم ${order.orderNumber} (کۆدی بەدواداچوون: ${order.trackingNumber})."
                                com.example.localization.AppLanguage.ARABIC -> "مرحباً أونيكس كوفي، أود الاستفسار عن حالة طلبي رقم ${order.orderNumber} (رمز التتبع: ${order.trackingNumber})."
                                com.example.localization.AppLanguage.ENGLISH -> "Hello ONYX Coffee, I would like an update on my order ${order.orderNumber} (Tracking: ${order.trackingNumber})."
                            }
                            WhatsAppNotificationHelper.openWhatsApp(context, adminWhatsAppNumber, inquiry)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("track_order_whatsapp_inquiry_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (language) {
                                com.example.localization.AppLanguage.KURDISH -> "پرسیارکردن لە واتسئاپ دەربارەی ئەم داواکارییە"
                                com.example.localization.AppLanguage.ARABIC -> "استفسار عن الطلب عبر واتساب"
                                com.example.localization.AppLanguage.ENGLISH -> "Track / Inquire on WhatsApp"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedOrderForTracking = null }) {
                    Text(text = "OK", color = OnyxGold)
                }
            },
            containerColor = OnyxSurfaceDark
        )
    }

    // --- Redeem Points Voucher Dialog ---
    if (showRedeemSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showRedeemSuccessDialog = false },
            title = {
                Text(
                    text = if (strings.isArabic) "تم إنشاء قسيمة الخصم!" else "Voucher Generated Successfully!",
                    color = OnyxGold,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (strings.isArabic) "تم خصم 500 نقطة من رصيدك. استخدم هذا الرمز عند إتمام طلبك:" else "500 points redeemed. Use this coupon code at checkout for 10,000 IQD off:",
                        color = OnyxCream
                    )
                    Surface(
                        color = OnyxDeepBlack,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGold)
                    ) {
                        Text(
                            text = "ONYX-REWARD-10K",
                            color = OnyxGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRedeemSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OnyxGold, contentColor = OnyxDeepBlack)
                ) {
                    Text(text = "Done", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = OnyxSurfaceDark
        )
    }
}
