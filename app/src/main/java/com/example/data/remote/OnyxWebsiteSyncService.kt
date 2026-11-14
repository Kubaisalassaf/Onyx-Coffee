package com.example.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.model.*
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Types
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// --- DTOs for Website Sync (onyxcoffeeiraq.com) ---

data class WebsiteOrderItemPayload(
    @Json(name = "product_id") val productId: String,
    @Json(name = "sku") val sku: String,
    @Json(name = "name") val name: String,
    @Json(name = "package_grams") val packageGrams: Int,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "unit_price_iqd") val unitPriceIqd: Long,
    @Json(name = "total_iqd") val totalIqd: Long
)

data class WebsiteOrderPayload(
    @Json(name = "order_number") val orderNumber: String,
    @Json(name = "tracking_number") val trackingNumber: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_phone") val customerPhone: String,
    @Json(name = "governorate") val governorate: String,
    @Json(name = "street_address") val streetAddress: String,
    @Json(name = "payment_provider") val paymentProvider: String,
    @Json(name = "payment_status") val paymentStatus: String,
    @Json(name = "order_status") val orderStatus: String,
    @Json(name = "subtotal_iqd") val subtotalIqd: Long,
    @Json(name = "delivery_fee_iqd") val deliveryFeeIqd: Long,
    @Json(name = "discount_iqd") val discountIqd: Long,
    @Json(name = "final_total_iqd") val finalTotalIqd: Long,
    @Json(name = "notes") val notes: String,
    @Json(name = "source") val source: String = "android_app",
    @Json(name = "items") val items: List<WebsiteOrderItemPayload>
)

data class WebsiteOrderResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "order_number") val orderNumber: String? = null,
    @Json(name = "order_status") val orderStatus: String? = null,
    @Json(name = "message") val message: String? = null
)

interface OnyxWebsiteRetrofitApi {
    @GET
    suspend fun getRawJson(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<String>

    @POST
    suspend fun pushOrder(
        @Url url: String,
        @Body payload: WebsiteOrderPayload,
        @Header("Authorization") authHeader: String? = null
    ): Response<WebsiteOrderResponse>

    @GET("api/v1/orders/track/{orderNumber}")
    suspend fun trackOrder(
        @Path("orderNumber") orderNumber: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<WebsiteOrderResponse>
}

sealed class OnyxDeepLinkAction {
    data object OpenHome : OnyxDeepLinkAction()
    data class OpenShop(val category: ProductCategory = ProductCategory.ALL) : OnyxDeepLinkAction()
    data class OpenProduct(val productId: String) : OnyxDeepLinkAction()
    data class OpenTrackOrder(val orderOrTrackingQuery: String) : OnyxDeepLinkAction()
    data object OpenJournal : OnyxDeepLinkAction()
}

data class WebsiteConnectionCheckResult(
    val status: WebsiteConnectionStatus,
    val httpCode: Int? = null,
    val latencyMs: Long = 0L,
    val detail: String
)

data class WebsiteProductSyncResult(
    val success: Boolean,
    val products: List<Product> = emptyList(),
    val status: WebsiteConnectionStatus,
    val detail: String
)

data class WebsiteOrderSyncResult(
    val success: Boolean,
    val status: WebsiteConnectionStatus,
    val detail: String,
    val remoteOrderStatus: OrderStatus? = null
)

object OnyxWebsiteSyncService {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun normalizeBaseUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim().ifBlank { "https://onyxcoffeeiraq.com" }
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    fun buildProductShareUrl(websiteUrl: String, productId: String): String {
        val base = normalizeBaseUrl(websiteUrl).trimEnd('/')
        return "$base/product/$productId"
    }

    fun buildTrackOrderUrl(websiteUrl: String, orderNumber: String): String {
        val base = normalizeBaseUrl(websiteUrl).trimEnd('/')
        return "$base/track?order=$orderNumber"
    }

    fun buildAssetLinksJson(
        packageName: String = "com.aistudio.onyxcoffee.iqmkt",
        sha256Fingerprint: String = "REPLACE_WITH_YOUR_PLAY_APP_SIGNING_SHA256"
    ): String = """
        [
          {
            "relation": ["delegate_permission/common.handle_all_urls"],
            "target": {
              "namespace": "android_app",
              "package_name": "$packageName",
              "sha256_cert_fingerprints": [
                "$sha256Fingerprint"
              ]
            }
          }
        ]
    """.trimIndent()

    fun parseDeepLink(rawUri: String?): OnyxDeepLinkAction? {
        if (rawUri.isNullOrBlank()) return null
        return try {
            val normalized = rawUri.trim()
            val afterHost = normalized
                .substringAfter("://", "")
                .substringAfter("/", "")
            val pathPart = afterHost.substringBefore("?").trim('/')
            val queryPart = normalized.substringAfter("?", "")
            val segments = if (pathPart.isEmpty()) emptyList() else pathPart.split("/")

            when {
                segments.isEmpty() -> OnyxDeepLinkAction.OpenHome
                segments[0].equals("shop", ignoreCase = true) ||
                    segments[0].equals("products", ignoreCase = true) -> {
                    val cat = when (segments.getOrNull(1)?.lowercase()) {
                        "coffee" -> ProductCategory.COFFEE
                        "machines" -> ProductCategory.MACHINES
                        "equipment" -> ProductCategory.EQUIPMENT
                        "accessories" -> ProductCategory.ACCESSORIES
                        else -> ProductCategory.ALL
                    }
                    OnyxDeepLinkAction.OpenShop(cat)
                }
                segments[0].equals("product", ignoreCase = true) && segments.size >= 2 -> {
                    OnyxDeepLinkAction.OpenProduct(segments[1])
                }
                segments[0].equals("track", ignoreCase = true) -> {
                    val queryOrder = queryPart.split("&")
                        .firstOrNull { it.startsWith("order=", ignoreCase = true) || it.startsWith("code=", ignoreCase = true) }
                        ?.substringAfter("=")
                        ?.trim()
                    val ref = queryOrder?.takeIf { it.isNotEmpty() } ?: segments.getOrNull(1).orEmpty()
                    OnyxDeepLinkAction.OpenTrackOrder(ref)
                }
                segments[0].equals("journal", ignoreCase = true) -> OnyxDeepLinkAction.OpenJournal
                else -> OnyxDeepLinkAction.OpenHome
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun buildAuthHeader(config: WebsiteIntegrationConfig): String? {
        val key = config.apiKey.trim()
        val secret = config.apiSecret.trim()
        if (key.isEmpty()) return null
        return when (config.platformType) {
            WebsitePlatformType.WOOCOMMERCE -> {
                if (secret.isNotEmpty()) Credentials.basic(key, secret) else "Bearer $key"
            }
            WebsitePlatformType.CUSTOM_REST -> "Bearer $key"
            WebsitePlatformType.SHOPIFY -> key
        }
    }

    private fun createRetrofit(baseUrl: String): OnyxWebsiteRetrofitApi {
        return Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(baseUrl))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OnyxWebsiteRetrofitApi::class.java)
    }

    suspend fun testConnection(config: WebsiteIntegrationConfig): WebsiteConnectionCheckResult =
        withContext(Dispatchers.IO) {
            val base = normalizeBaseUrl(config.websiteUrl)
            val startTime = System.currentTimeMillis()
            try {
                val request = Request.Builder()
                    .url(base)
                    .get()
                    .addHeader("Accept", "application/json, text/html")
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    val latency = System.currentTimeMillis() - startTime
                    when {
                        response.isSuccessful -> WebsiteConnectionCheckResult(
                            status = WebsiteConnectionStatus.CONNECTED,
                            httpCode = response.code,
                            latencyMs = latency,
                            detail = "HTTP ${response.code} OK from $base (${latency}ms)"
                        )
                        response.code in 401..403 -> WebsiteConnectionCheckResult(
                            status = WebsiteConnectionStatus.ERROR,
                            httpCode = response.code,
                            latencyMs = latency,
                            detail = "Site reachable, API credentials required (HTTP ${response.code})"
                        )
                        else -> WebsiteConnectionCheckResult(
                            status = WebsiteConnectionStatus.ERROR,
                            httpCode = response.code,
                            latencyMs = latency,
                            detail = "Website responded with HTTP ${response.code}"
                        )
                    }
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                WebsiteConnectionCheckResult(
                    status = WebsiteConnectionStatus.OFFLINE_OR_PENDING,
                    httpCode = null,
                    latencyMs = latency,
                    detail = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "Domain unreachable / DNS pending"}"
                )
            }
        }

    suspend fun fetchProducts(config: WebsiteIntegrationConfig): WebsiteProductSyncResult =
        withContext(Dispatchers.IO) {
            val base = normalizeBaseUrl(config.websiteUrl)
            val endpoint = base + config.platformType.productsEndpoint
            try {
                val reqBuilder = Request.Builder()
                    .url(endpoint)
                    .get()
                    .addHeader("Accept", "application/json")
                buildAuthHeader(config)?.let { auth ->
                    if (config.platformType == WebsitePlatformType.SHOPIFY) {
                        reqBuilder.addHeader("X-Shopify-Access-Token", auth)
                    } else {
                        reqBuilder.addHeader("Authorization", auth)
                    }
                }

                okHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                    val bodyStr = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext WebsiteProductSyncResult(
                            success = false,
                            status = if (response.code in 400..499) WebsiteConnectionStatus.ERROR else WebsiteConnectionStatus.OFFLINE_OR_PENDING,
                            detail = "GET $endpoint returned HTTP ${response.code}"
                        )
                    }

                    val parsed = parseProductsJson(bodyStr, config.websiteUrl)
                    WebsiteProductSyncResult(
                        success = true,
                        products = parsed,
                        status = WebsiteConnectionStatus.CONNECTED,
                        detail = "Synced ${parsed.size} products from ${config.websiteUrl}"
                    )
                }
            } catch (e: Exception) {
                WebsiteProductSyncResult(
                    success = false,
                    status = WebsiteConnectionStatus.OFFLINE_OR_PENDING,
                    detail = "Could not sync $endpoint: ${e.localizedMessage ?: e.javaClass.simpleName}"
                )
            }
        }

    suspend fun pushOrder(order: Order, config: WebsiteIntegrationConfig): WebsiteOrderSyncResult =
        withContext(Dispatchers.IO) {
            val base = normalizeBaseUrl(config.websiteUrl)
            val endpoint = base + config.platformType.ordersEndpoint
            val payload = WebsiteOrderPayload(
                orderNumber = order.orderNumber,
                trackingNumber = order.trackingNumber,
                customerName = order.customerName,
                customerPhone = order.customerPhone,
                governorate = order.deliveryAddress.governorate.nameEn,
                streetAddress = order.deliveryAddress.streetAddress,
                paymentProvider = order.paymentProvider.id,
                paymentStatus = order.paymentStatus.name,
                orderStatus = order.orderStatus.code,
                subtotalIqd = order.subtotalIqd,
                deliveryFeeIqd = order.deliveryFeeIqd,
                discountIqd = order.discountIqd,
                finalTotalIqd = order.finalTotalIqd,
                notes = order.notes,
                items = order.items.map { item ->
                    WebsiteOrderItemPayload(
                        productId = item.product.id,
                        sku = item.product.sku,
                        name = item.product.nameEn,
                        packageGrams = item.selectedPackageGram,
                        quantity = item.quantity,
                        unitPriceIqd = item.unitPriceIqd,
                        totalIqd = item.totalPriceIqd
                    )
                }
            )

            try {
                val jsonBody = moshi.adapter(WebsiteOrderPayload::class.java).toJson(payload)
                val reqBuilder = Request.Builder()
                    .url(endpoint)
                    .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .addHeader("Accept", "application/json")
                buildAuthHeader(config)?.let { reqBuilder.addHeader("Authorization", it) }

                okHttpClient.newCall(reqBuilder.build()).execute().use { response ->
                    if (response.isSuccessful) {
                        WebsiteOrderSyncResult(
                            success = true,
                            status = WebsiteConnectionStatus.CONNECTED,
                            detail = "Order ${order.orderNumber} pushed to ${config.websiteUrl} (HTTP ${response.code})"
                        )
                    } else {
                        WebsiteOrderSyncResult(
                            success = false,
                            status = WebsiteConnectionStatus.ERROR,
                            detail = "Website rejected order push: HTTP ${response.code}"
                        )
                    }
                }
            } catch (e: Exception) {
                WebsiteOrderSyncResult(
                    success = false,
                    status = WebsiteConnectionStatus.OFFLINE_OR_PENDING,
                    detail = "Queued locally (website offline: ${e.localizedMessage ?: "unreachable"})"
                )
            }
        }

    suspend fun fetchOrderTrackingStatus(
        orderNumber: String,
        config: WebsiteIntegrationConfig
    ): WebsiteOrderSyncResult = withContext(Dispatchers.IO) {
        try {
            val api = createRetrofit(config.websiteUrl)
            val response = api.trackOrder(orderNumber, buildAuthHeader(config))
            if (response.isSuccessful) {
                val remoteCode = response.body()?.orderStatus
                val mappedStatus = OrderStatus.values().firstOrNull {
                    it.code.equals(remoteCode, ignoreCase = true) || it.name.equals(remoteCode, ignoreCase = true)
                }
                WebsiteOrderSyncResult(
                    success = true,
                    status = WebsiteConnectionStatus.CONNECTED,
                    detail = "Live status synced from ${config.websiteUrl}",
                    remoteOrderStatus = mappedStatus
                )
            } else {
                WebsiteOrderSyncResult(
                    success = false,
                    status = WebsiteConnectionStatus.ERROR,
                    detail = "Website tracking endpoint returned HTTP ${response.code()}"
                )
            }
        } catch (e: Exception) {
            WebsiteOrderSyncResult(
                success = false,
                status = WebsiteConnectionStatus.OFFLINE_OR_PENDING,
                detail = "Using local tracking cache (${e.localizedMessage ?: "website unreachable"})"
            )
        }
    }

    /**
     * Parses product JSON arrays from Custom REST API, WooCommerce, or Shopify JSON responses
     * using Moshi (works identically on Android and pure JVM unit tests).
     */
    @Suppress("UNCHECKED_CAST")
    fun parseProductsJson(rawJson: String, websiteUrl: String): List<Product> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()
        val base = normalizeBaseUrl(websiteUrl).trimEnd('/')

        val mapType = Types.newParameterizedType(
            Map::class.java,
            String::class.java,
            Any::class.java
        )
        val listType = Types.newParameterizedType(List::class.java, mapType)

        val itemMaps: List<Map<String, Any?>> = try {
            when {
                trimmed.startsWith("[") -> {
                    val adapter = moshi.adapter<List<Map<String, Any?>>>(listType)
                    adapter.fromJson(trimmed).orEmpty()
                }
                trimmed.startsWith("{") -> {
                    val adapter = moshi.adapter<Map<String, Any?>>(mapType)
                    val root = adapter.fromJson(trimmed).orEmpty()
                    val nested = (root["products"] as? List<*>) ?: (root["data"] as? List<*>)
                    nested?.mapNotNull { it as? Map<String, Any?> }.orEmpty()
                }
                else -> emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }

        fun Map<String, Any?>.str(key: String): String =
            when (val v = this[key]) {
                is String -> v
                is Number -> v.toString()
                else -> ""
            }

        fun Map<String, Any?>.num(key: String): Double? =
            when (val v = this[key]) {
                is Number -> v.toDouble()
                is String -> v.toDoubleOrNull()
                else -> null
            }

        val result = mutableListOf<Product>()
        itemMaps.forEachIndexed { i, item ->
            val id = item.str("id").ifBlank { "web_$i" }
            val nameEn = item.str("nameEn").ifBlank {
                item.str("name").ifBlank { item.str("title").ifBlank { "ONYX Website Product" } }
            }
            val nameAr = item.str("nameAr").ifBlank { nameEn }
            val nameKu = item.str("nameKu").ifBlank { nameAr }
            val descEn = item.str("descriptionEn").ifBlank {
                item.str("description").ifBlank { item.str("body_html") }
                    .replace(Regex("<[^>]*>"), "").trim()
            }
            val descAr = item.str("descriptionAr").ifBlank { descEn }
            val descKu = item.str("descriptionKu").ifBlank { descAr }
            val sku = item.str("sku").ifBlank { "WEB-${id.uppercase()}" }
            val brand = item.str("brand").ifBlank { item.str("vendor").ifBlank { "ONYX Coffee" } }

            val rawPrice = item.num("priceIqd")
                ?: item.num("price")
                ?: item.num("regular_price")
                ?: 25000.0
            val priceIqd = rawPrice.toLong().coerceAtLeast(1000L)

            val rawCategory = item.str("category").uppercase()
            val mappedCategory = when {
                rawCategory.contains("MACHINE") -> ProductCategory.MACHINES
                rawCategory.contains("EQUIP") || rawCategory.contains("GRIND") -> ProductCategory.EQUIPMENT
                rawCategory.contains("ACCESS") -> ProductCategory.ACCESSORIES
                else -> ProductCategory.COFFEE
            }

            val firstImagesSrc = (item["images"] as? List<*>)
                ?.firstOrNull()
                ?.let { it as? Map<*, *> }
                ?.get("src") as? String

            val rawImageUrl = item.str("imageUrl").ifBlank { firstImagesSrc.orEmpty() }
                .let { url ->
                    when {
                        url.isBlank() -> null
                        url.startsWith("http") -> url
                        else -> "$base/${url.trimStart('/')}"
                    }
                }

            val stock = (item.num("stockQuantity") ?: item.num("stock_quantity"))?.toInt() ?: 25
            val isFeatured = (item["isFeatured"] as? Boolean) ?: false

            result.add(
                Product(
                    id = id,
                    sku = sku,
                    nameEn = nameEn,
                    nameAr = nameAr,
                    nameKu = nameKu,
                    descriptionEn = descEn,
                    descriptionAr = descAr,
                    descriptionKu = descKu,
                    category = mappedCategory,
                    subCategory = item.str("subCategory").ifBlank { "Website Catalog" },
                    brand = brand,
                    regularPriceIqd = priceIqd,
                    stockQuantity = stock,
                    imageUrl = rawImageUrl,
                    isFeatured = isFeatured
                )
            )
        }
        return result
    }

    fun openWebsiteUrl(context: Context, url: String = "https://onyxcoffeeiraq.com"): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalizeBaseUrl(url).trimEnd('/'))).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareProductLink(context: Context, websiteUrl: String, productName: String, productId: String) {
        val link = buildProductShareUrl(websiteUrl, productId)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, productName)
            putExtra(Intent.EXTRA_TEXT, "$productName — ONYX Coffee Iraq\n$link")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}
