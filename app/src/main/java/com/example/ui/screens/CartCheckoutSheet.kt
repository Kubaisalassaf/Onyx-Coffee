package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.LocalAppLanguage
import com.example.localization.OnyxFormatter
import com.example.localization.onyxStrings
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutSheet(
    cartItems: List<CartItem>,
    appliedCoupon: Coupon?,
    governorateFees: Map<IraqiGovernorate, Long>,
    defaultAddress: DeliveryAddress?,
    onUpdateQuantity: (CartItem, Int) -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onRemoveCoupon: () -> Unit,
    onPlaceOrder: (DeliveryAddress, PaymentProvider, String) -> Order,
    onDismiss: () -> Unit,
    onOrderSuccess: (Order) -> Unit
) {
    val strings = onyxStrings()
    val language = LocalAppLanguage.current

    var isCheckoutStep by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf("") }
    var couponErrorMessage by remember { mutableStateOf<String?>(null) }

    // Checkout Form State
    var selectedGovernorate by remember { mutableStateOf(defaultAddress?.governorate ?: IraqiGovernorate.BAGHDAD) }
    var recipientName by remember { mutableStateOf(defaultAddress?.recipientName ?: "Haider Al-Baghdadi") }
    var recipientPhone by remember { mutableStateOf(defaultAddress?.phone ?: "+964 770 123 4567") }
    var addressDetails by remember { mutableStateOf(defaultAddress?.streetAddress ?: "Al-Mansour, District 603, Street 14") }
    var deliveryNotes by remember { mutableStateOf(defaultAddress?.deliveryNotes ?: "") }
    var selectedPaymentProvider by remember { mutableStateOf(PaymentProvider.FIB) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Calculations
    val subtotal = remember(cartItems) { cartItems.sumOf { it.totalPriceIqd } }
    val deliveryFee = remember(subtotal, selectedGovernorate, governorateFees) {
        if (subtotal >= 100000) 0L else (governorateFees[selectedGovernorate] ?: selectedGovernorate.standardDeliveryFeeIqd)
    }
    val discount = remember(subtotal, appliedCoupon) {
        if (appliedCoupon != null && subtotal >= appliedCoupon.minOrderIqd) {
            (subtotal * (appliedCoupon.discountPercent / 100.0)).toLong()
        } else 0L
    }
    val finalTotal = remember(subtotal, deliveryFee, discount) {
        (subtotal + deliveryFee - discount).coerceAtLeast(0L)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isCheckoutStep) isCheckoutStep = false else onDismiss()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(OnyxCardDark, CircleShape)
                            .testTag("cart_back_btn")
                    ) {
                        Icon(
                            imageVector = if (isCheckoutStep) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = "Back",
                            tint = OnyxCream,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (isCheckoutStep) strings.checkoutTitle else strings.shoppingCart,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = OnyxGold,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Box(modifier = Modifier.size(36.dp))
                }

                Divider(color = OnyxCardBorder)

                if (cartItems.isEmpty() && !isCheckoutStep) {
                    // Empty Cart State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = OnyxGold.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = strings.cartEmpty,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = OnyxCream,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Text(
                                text = strings.cartEmptyTip,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = OnyxMutedText,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnyxGold,
                                    contentColor = OnyxDeepBlack
                                )
                            ) {
                                Text(text = strings.heroShopBtn, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (!isCheckoutStep) {
                    // --- STEP 1: CART ITEMS VIEW ---
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(cartItems, key = { "${it.product.id}_${it.selectedPackageGram}" }) { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Mini icon box
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .background(OnyxCoffeeBrownDark, RoundedCornerShape(10.dp))
                                            .border(1.dp, OnyxGoldDark, RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Coffee,
                                            contentDescription = null,
                                            tint = OnyxGold,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }

                                    // Title & details
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product.localizedName(language),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = OnyxCream,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${item.product.brand} • ${if (item.selectedPackageGram >= 1000) "1 KG" else "${item.selectedPackageGram}g"}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = OnyxFormatter.formatIqd(item.totalPriceIqd, language),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = OnyxGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    // Quantity Stepper
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(OnyxDeepBlack, RoundedCornerShape(8.dp))
                                            .border(1.dp, OnyxCardBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        IconButton(
                                            onClick = { onUpdateQuantity(item, item.quantity - 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = OnyxCream,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Text(
                                            text = item.quantity.toString(),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = OnyxCream,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { onUpdateQuantity(item, item.quantity + 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = OnyxCream,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Coupon Code Section
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (appliedCoupon != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocalOffer,
                                                    contentDescription = null,
                                                    tint = StatusSuccess,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "${appliedCoupon.code} (-${appliedCoupon.discountPercent}%)",
                                                    color = StatusSuccess,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            IconButton(
                                                onClick = onRemoveCoupon,
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove coupon",
                                                    tint = OnyxMutedText,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = couponInput,
                                                onValueChange = {
                                                    couponInput = it
                                                    couponErrorMessage = null
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("coupon_input_field"),
                                                placeholder = {
                                                    Text(text = strings.promoPlaceholder, fontSize = 11.sp, color = OnyxMutedText)
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = OnyxDeepBlack,
                                                    unfocusedContainerColor = OnyxDeepBlack,
                                                    focusedBorderColor = OnyxGold,
                                                    unfocusedBorderColor = OnyxCardBorder,
                                                    focusedTextColor = OnyxCream,
                                                    unfocusedTextColor = OnyxCream
                                                )
                                            )

                                            Button(
                                                onClick = {
                                                    if (couponInput.isNotBlank()) {
                                                        onApplyCoupon(couponInput)
                                                    }
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = OnyxGold,
                                                    contentColor = OnyxDeepBlack
                                                ),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                                modifier = Modifier.testTag("apply_coupon_btn")
                                            ) {
                                                Text(text = strings.applyCoupon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Order Summary & Proceed Button
                    Surface(
                        color = OnyxSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Subtotal row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = strings.subtotal, style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText))
                                Text(text = OnyxFormatter.formatIqd(subtotal, language), style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream))
                            }

                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = strings.discount, style = MaterialTheme.typography.bodySmall.copy(color = StatusSuccess))
                                    Text(text = "-${OnyxFormatter.formatIqd(discount, language)}", style = MaterialTheme.typography.bodyMedium.copy(color = StatusSuccess, fontWeight = FontWeight.Bold))
                                }
                            }

                            Divider(color = OnyxCardBorder)

                            // Total row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.total,
                                    style = MaterialTheme.typography.titleMedium.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = OnyxFormatter.formatIqd(finalTotal, language),
                                    style = MaterialTheme.typography.titleLarge.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { isCheckoutStep = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnyxGold,
                                    contentColor = OnyxDeepBlack
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("proceed_to_checkout_btn"),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = strings.proceedToCheckout, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    // --- STEP 2: CHECKOUT & IRAQI PAYMENT GATEWAYS ---
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section: Governorate & Delivery
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
                                    text = if (strings.isArabic) "عنوان الشحن ومحافظة التوصيل" else "Shipping Address & Governorate",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = OnyxGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                // Governorate Selector Dropdown
                                var expandedGov by remember { mutableStateOf(false) }
                                Box {
                                    OutlinedCard(
                                        onClick = { expandedGov = true },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.outlinedCardColors(containerColor = OnyxDeepBlack),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                                        modifier = Modifier.fillMaxWidth().testTag("gov_dropdown_trigger")
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
                                                    text = strings.governorateSelect,
                                                    style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                                                )
                                                Text(
                                                    text = selectedGovernorate.localizedName(language),
                                                    style = MaterialTheme.typography.bodyMedium.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = OnyxGold
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = expandedGov,
                                        onDismissRequest = { expandedGov = false },
                                        modifier = Modifier.background(OnyxCardDark)
                                    ) {
                                        IraqiGovernorate.values().forEach { gov ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = gov.localizedName(language),
                                                            color = OnyxCream
                                                        )
                                                        Text(
                                                            text = OnyxFormatter.formatIqd(gov.standardDeliveryFeeIqd, language),
                                                            color = OnyxGold,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedGovernorate = gov
                                                    expandedGov = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Recipient Name
                                OutlinedTextField(
                                    value = recipientName,
                                    onValueChange = { recipientName = it },
                                    label = { Text(text = strings.recipientName, color = OnyxMutedText) },
                                    modifier = Modifier.fillMaxWidth().testTag("recipient_name_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = OnyxDeepBlack,
                                        unfocusedContainerColor = OnyxDeepBlack,
                                        focusedBorderColor = OnyxGold,
                                        unfocusedBorderColor = OnyxCardBorder,
                                        focusedTextColor = OnyxCream,
                                        unfocusedTextColor = OnyxCream
                                    )
                                )

                                // Phone number
                                OutlinedTextField(
                                    value = recipientPhone,
                                    onValueChange = { recipientPhone = it },
                                    label = { Text(text = strings.phonePlaceholder, color = OnyxMutedText) },
                                    modifier = Modifier.fillMaxWidth().testTag("recipient_phone_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = OnyxDeepBlack,
                                        unfocusedContainerColor = OnyxDeepBlack,
                                        focusedBorderColor = OnyxGold,
                                        unfocusedBorderColor = OnyxCardBorder,
                                        focusedTextColor = OnyxCream,
                                        unfocusedTextColor = OnyxCream
                                    )
                                )

                                // Address
                                OutlinedTextField(
                                    value = addressDetails,
                                    onValueChange = { addressDetails = it },
                                    label = { Text(text = strings.cityStreet, color = OnyxMutedText) },
                                    modifier = Modifier.fillMaxWidth().testTag("recipient_address_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = OnyxDeepBlack,
                                        unfocusedContainerColor = OnyxDeepBlack,
                                        focusedBorderColor = OnyxGold,
                                        unfocusedBorderColor = OnyxCardBorder,
                                        focusedTextColor = OnyxCream,
                                        unfocusedTextColor = OnyxCream
                                    )
                                )
                            }
                        }

                        // Section: Iraqi Payment Gateways
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
                                    text = if (strings.isArabic) "طريقة الدفع (العراق)" else "Payment Gateways (Iraq)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        color = OnyxGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )

                                PaymentProvider.values().forEach { provider ->
                                    val isSelected = provider == selectedPaymentProvider
                                    Card(
                                        onClick = { selectedPaymentProvider = provider },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) OnyxCardDark else OnyxDeepBlack
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.5.dp,
                                            if (isSelected) OnyxGold else OnyxCardBorder
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("payment_opt_${provider.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPaymentProvider = provider },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = OnyxGold,
                                                    unselectedColor = OnyxMutedText
                                                )
                                            )
                                             Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = provider.localizedName(language),
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        color = OnyxCream,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                                Text(
                                                    text = provider.localizedDescription(language),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = OnyxMutedText,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    color = OnyxCoffeeBrownDark,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = OnyxGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = strings.sandboxNotice,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = OnyxWarmBeige,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Final Checkout Confirm Bar
                    Surface(
                        color = OnyxSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${strings.deliveryFee} (${selectedGovernorate.localizedName(language)}):",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnyxMutedText)
                                )
                                Text(
                                    text = if (deliveryFee == 0L) strings.freeDelivery else OnyxFormatter.formatIqd(deliveryFee, language),
                                    style = MaterialTheme.typography.bodySmall.copy(color = if (deliveryFee == 0L) StatusSuccess else OnyxCream)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.total,
                                    style = MaterialTheme.typography.titleMedium.copy(color = OnyxCream, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = OnyxFormatter.formatIqd(finalTotal, language),
                                    style = MaterialTheme.typography.headlineSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    isProcessingPayment = true
                                    val address = DeliveryAddress(
                                        id = "addr_${System.currentTimeMillis()}",
                                        title = "Delivery",
                                        governorate = selectedGovernorate,
                                        cityOrDistrict = addressDetails,
                                        streetAddress = addressDetails,
                                        recipientName = recipientName,
                                        phone = recipientPhone,
                                        deliveryNotes = deliveryNotes
                                    )
                                    val createdOrder = onPlaceOrder(address, selectedPaymentProvider, deliveryNotes)
                                    isProcessingPayment = false
                                    onOrderSuccess(createdOrder)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OnyxGold,
                                    contentColor = OnyxDeepBlack
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_order_placement_btn"),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                enabled = !isProcessingPayment && recipientName.isNotBlank() && recipientPhone.isNotBlank()
                            ) {
                                if (isProcessingPayment) {
                                    CircularProgressIndicator(
                                        color = OnyxDeepBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text(text = strings.placeOrder, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
