package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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

@Composable
fun AccountScreen(
    userProfile: UserProfile,
    orders: List<Order>,
    onToggleRole: () -> Unit,
    onNavigateToShop: () -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    var selectedOrderForTracking by remember { mutableStateOf<Order?>(null) }
    var showRedeemSuccessDialog by remember { mutableStateOf(false) }

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
                                    text = if (strings.isArabic) userProfile.loyaltyTier.badgeLabelAr else userProfile.loyaltyTier.badgeLabelEn,
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
            Text(
                text = if (strings.isArabic) "سجل الطلبات والتتبع المباشر" else "Order History & Live Courier Tracking",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = OnyxCream,
                    fontWeight = FontWeight.Bold
                )
            )
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
                                    text = if (strings.isArabic) order.orderStatus.labelAr else order.orderStatus.labelEn,
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
                                text = "• ${if (strings.isArabic) item.product.nameAr else item.product.nameEn} (x${item.quantity})",
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
                                    text = "${order.deliveryAddress.governorate.nameEn} • ${order.paymentProvider.nameEn}",
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
                                text = if (strings.isArabic) "خدمة العملاء عبر واتساب" else "WhatsApp Concierge Support",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = OnyxCream,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = if (strings.isArabic) "استشارات باريستا ودعم فني على مدار الساعة" else "24/7 Barista consultations & machine support",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val uri = Uri.parse("https://wa.me/9647700000000?text=Hello%20ONYX%20Coffee%20Iraq")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = OnyxDeepBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Chat", fontWeight = FontWeight.Bold)
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
                                text = if (strings.isArabic) step.labelAr else step.labelEn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isCompleted) OnyxCream else OnyxMutedText,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
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
