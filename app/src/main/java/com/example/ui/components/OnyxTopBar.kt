package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingBag
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
import com.example.localization.AppLanguage
import com.example.localization.onyxStrings
import com.example.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnyxTopBar(
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    userRole: UserRole,
    onToggleAdminMode: () -> Unit,
    cartItemCount: Int,
    onOpenCart: () -> Unit,
    onSelectLanguage: ((AppLanguage) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = onyxStrings()
    var isLanguageMenuOpen by remember { mutableStateOf(false) }

    Surface(
        color = OnyxDeepBlack,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = OnyxCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Name & Luxury Monogram
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gold Emblem badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(OnyxCoffeeBrownDark)
                        .border(1.dp, OnyxGold, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "O",
                        color = OnyxGold,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp
                    )
                }

                Column {
                    Text(
                        text = "ONYX",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = OnyxGold,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.KURDISH -> "عێراق • کوردستان • بەغدا"
                            AppLanguage.ARABIC -> "العراق • بغداد • أربيل"
                            AppLanguage.ENGLISH -> "IRAQ • BAGHDAD • ERBIL"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnyxMutedText,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Action Buttons: Admin Toggle, Language Switcher, Cart Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Dropdown Selector (EN / عربي / کوردی)
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = OnyxCardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { isLanguageMenuOpen = true }
                            .testTag("language_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Change Language",
                                tint = OnyxGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentLanguage.shortName,
                                color = OnyxGold,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = OnyxMutedText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isLanguageMenuOpen,
                        onDismissRequest = { isLanguageMenuOpen = false },
                        modifier = Modifier
                            .background(OnyxSurfaceDark)
                            .border(1.dp, OnyxCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = lang.displayName,
                                            color = if (currentLanguage == lang) OnyxGold else OnyxCream,
                                            fontWeight = if (currentLanguage == lang) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (currentLanguage == lang) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = OnyxGold,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSelectLanguage?.invoke(lang) ?: onToggleLanguage()
                                    isLanguageMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Admin Toggle Quick Access
                IconButton(
                    onClick = onToggleAdminMode,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (userRole == UserRole.ADMIN) OnyxGold.copy(alpha = 0.2f) else OnyxCardDark)
                        .border(1.dp, if (userRole == UserRole.ADMIN) OnyxGold else OnyxCardBorder, CircleShape)
                        .testTag("admin_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Mode",
                        tint = if (userRole == UserRole.ADMIN) OnyxGold else OnyxMutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Cart Icon with Badge
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onOpenCart,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(OnyxCardDark)
                            .border(1.dp, OnyxCardBorder, CircleShape)
                            .testTag("cart_open_button")
                    ) {
                        Icon(
                            imageVector = if (cartItemCount > 0) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                            contentDescription = "Shopping Cart",
                            tint = if (cartItemCount > 0) OnyxGold else OnyxCream,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (cartItemCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-2).dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(OnyxGold)
                                .border(1.dp, OnyxDeepBlack, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartItemCount.toString(),
                                color = OnyxDeepBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
