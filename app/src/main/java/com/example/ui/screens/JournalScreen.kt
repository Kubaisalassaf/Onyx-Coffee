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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.localization.onyxStrings
import com.example.model.JournalArticle
import com.example.ui.theme.*

@Composable
fun JournalScreen(
    articles: List<JournalArticle>
) {
    val strings = onyxStrings()
    var selectedArticle by remember { mutableStateOf<JournalArticle?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (strings.isArabic) "مجلة أونيكس كوفي" else "The ONYX Coffee Journal",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = OnyxGold,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (strings.isArabic) "دليل احترافي لثقافة القهوة، أساليب التحضير، ومعايرة الإسبريسو في العراق." else "Professional guides on brewing craft, origin terroirs, and barista techniques.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = OnyxMutedText)
                )
            }
        }

        // Article Cards
        items(articles, key = { it.id }) { article ->
            Card(
                colors = CardDefaults.cardColors(containerColor = OnyxSurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, OnyxCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { selectedArticle = article }
                    .testTag("journal_article_${article.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = OnyxCoffeeBrownDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, OnyxGoldDark)
                        ) {
                            Text(
                                text = article.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = OnyxGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = OnyxMutedText,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${article.readMinutes} min read",
                                style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText, fontSize = 11.sp)
                            )
                        }
                    }

                    Text(
                        text = if (strings.isArabic) article.titleAr else article.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            color = OnyxCream,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = if (strings.isArabic) article.excerptAr else article.excerptEn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OnyxWarmBeige,
                            lineHeight = 20.sp
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = article.author,
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxMutedText)
                        )
                        Text(
                            text = if (strings.isArabic) "اقرأ المقال الكامل ←" else "Read Full Guide →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = OnyxGold,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    // Article Full Reader Dialog
    selectedArticle?.let { article ->
        Dialog(
            onDismissRequest = { selectedArticle = null },
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedArticle = null },
                            modifier = Modifier
                                .size(36.dp)
                                .background(OnyxCardDark, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = OnyxCream)
                        }
                        Text(
                            text = article.category,
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxGold, fontWeight = FontWeight.Bold)
                        )
                        Box(modifier = Modifier.size(36.dp))
                    }

                    Divider(color = OnyxCardBorder)

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (strings.isArabic) article.titleAr else article.titleEn,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                color = OnyxCream,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "${article.author} • ${article.dateString}",
                            style = MaterialTheme.typography.labelSmall.copy(color = OnyxGold)
                        )

                        Divider(color = OnyxCardBorder)

                        Text(
                            text = if (strings.isArabic) article.contentAr else article.contentEn,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = OnyxWarmBeige,
                                lineHeight = 26.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
