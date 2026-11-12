package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.localization.AppLanguage
import com.example.localization.OnyxFormatter
import com.example.model.Order
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppNotificationHelper {

    private const val TAG = "WhatsAppNotifier"

    /**
     * Sanitizes phone number to international format for WhatsApp API.
     * Handles Iraqi local numbers: e.g., "0750 123 4567" -> "9647501234567"
     */
    fun sanitizePhoneNumber(raw: String): String {
        var digits = raw.filter { it.isDigit() }
        if (digits.startsWith("07") && digits.length == 11) {
            digits = "964" + digits.substring(1)
        } else if (digits.startsWith("7") && digits.length == 10) {
            digits = "964$digits"
        }
        return digits
    }

    /**
     * Formats a complete, professional order notification for WhatsApp.
     */
    fun generateOrderWhatsAppMessage(order: Order, language: AppLanguage, storeName: String = "ONYX Coffee Iraq"): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val formattedDate = dateFormat.format(Date(order.createdAtEpoch))

        val header = when (language) {
            AppLanguage.KURDISH -> "☕ *ئۆنیکس کۆفی - ئاگاداری داواکاری نوێ*\n" +
                    "☕ *ONYX Coffee Iraq - New Order Alert*"
            AppLanguage.ARABIC -> "☕ *أونيكس كوفي - إشعار طلب جديد في العراق*\n" +
                    "☕ *ONYX Coffee Iraq - New Order Alert*"
            AppLanguage.ENGLISH -> "☕ *ONYX COFFEE & EQUIPMENT - NEW ORDER ALERT*"
        }

        val itemsText = order.items.joinToString("\n") { item ->
            val name = when (language) {
                AppLanguage.KURDISH -> if (item.product.nameKu.isNotBlank()) item.product.nameKu else item.product.nameAr
                AppLanguage.ARABIC -> item.product.nameAr
                AppLanguage.ENGLISH -> item.product.nameEn
            }
            "  • *$name* (${item.selectedPackageGram}g) × ${item.quantity} = ${OnyxFormatter.formatIqd(item.totalPriceIqd, language)}"
        }

        val govName = order.deliveryAddress.governorate.localizedName(language)
        val providerName = order.paymentProvider.localizedName(language)

        val labels = when (language) {
            AppLanguage.KURDISH -> mapOf(
                "order" to "ژمارەی داواکاری",
                "tracking" to "کۆدی بەدواداچوون",
                "customer" to "ناوی کڕیار",
                "phone" to "مۆبایلی کڕیار",
                "destination" to "شوێنی گەیاندن",
                "address" to "ناونیشان",
                "notes" to "تێبینی",
                "items" to "کەلوپەلەکان",
                "subtotal" to "کۆی بەشەکی",
                "delivery" to "تێچووی گەیاندن",
                "discount" to "داشکاندن",
                "total" to "کۆی گشتی بۆ پارەدان",
                "payment" to "شێوازی پارەدان",
                "status" to "دۆخی پارەدان",
                "date" to "بەروار"
            )
            AppLanguage.ARABIC -> mapOf(
                "order" to "رقم الطلب",
                "tracking" to "رمز التتبع",
                "customer" to "اسم العميل",
                "phone" to "هاتف العميل",
                "destination" to "وجهة التوصيل",
                "address" to "العنوان بالتفصيل",
                "notes" to "ملاحظات",
                "items" to "المنتجات المطلوبة",
                "subtotal" to "المجموع الفرعي",
                "delivery" to "أجور التوصيل",
                "discount" to "الخصم",
                "total" to "المبلغ الإجمالي للدفع",
                "payment" to "طريقة الدفع",
                "status" to "حالة الدفع",
                "date" to "التاريخ والوقت"
            )
            AppLanguage.ENGLISH -> mapOf(
                "order" to "Order Number",
                "tracking" to "Tracking Code",
                "customer" to "Customer Name",
                "phone" to "Customer Phone",
                "destination" to "Destination",
                "address" to "Address",
                "notes" to "Notes",
                "items" to "Items Ordered",
                "subtotal" to "Subtotal",
                "delivery" to "Delivery Fee",
                "discount" to "Discount",
                "total" to "FINAL TOTAL",
                "payment" to "Payment Method",
                "status" to "Payment Status",
                "date" to "Order Time"
            )
        }

        val discountLine = if (order.discountIqd > 0) {
            "🏷 *${labels["discount"]}:* -${OnyxFormatter.formatIqd(order.discountIqd, language)}\n"
        } else ""

        val notesLine = if (order.notes.isNotBlank() || order.deliveryAddress.deliveryNotes.isNotBlank()) {
            "📝 *${labels["notes"]}:* ${order.notes.ifBlank { order.deliveryAddress.deliveryNotes }}\n"
        } else ""

        return buildString {
            append(header).append("\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("📋 *${labels["order"]}:* ${order.orderNumber}\n")
            append("🏷 *${labels["tracking"]}:* ${order.trackingNumber}\n")
            append("👤 *${labels["customer"]}:* ${order.customerName}\n")
            append("📞 *${labels["phone"]}:* ${order.customerPhone}\n")
            append("📍 *${labels["destination"]}:* $govName\n")
            append("🏠 *${labels["address"]}:* ${order.deliveryAddress.streetAddress}\n")
            append(notesLine)
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("🛒 *${labels["items"]}:*\n")
            append(itemsText).append("\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("💵 *${labels["subtotal"]}:* ${OnyxFormatter.formatIqd(order.subtotalIqd, language)}\n")
            append("🚚 *${labels["delivery"]}:* ${OnyxFormatter.formatIqd(order.deliveryFeeIqd, language)}\n")
            append(discountLine)
            append("✨ *${labels["total"]}:* ${OnyxFormatter.formatIqd(order.finalTotalIqd, language)}\n")
            append("💳 *${labels["payment"]}:* $providerName\n")
            append("📊 *${labels["status"]}:* ${order.paymentStatus.localizedLabel(language)}\n")
            append("🕒 *${labels["date"]}:* $formattedDate\n")
            append("━━━━━━━━━━━━━━━━━━━━━━━━━\n")
            append("_ONYX Coffee & Equipment Iraq Dispatch Center_")
        }
    }

    /**
     * Opens WhatsApp application or WhatsApp Web with the specified phone number and pre-filled message.
     */
    fun openWhatsApp(context: Context, phoneNumber: String, message: String): Boolean {
        val sanitized = sanitizePhoneNumber(phoneNumber)
        return try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$sanitized&text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp intent: ${e.message}")
            false
        }
    }
}
