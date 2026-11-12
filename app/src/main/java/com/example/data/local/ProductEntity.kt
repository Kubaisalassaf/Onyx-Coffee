package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Product
import com.example.model.ProductCategory

/**
 * Room database entity for 'Product' in ONYX Coffee & Equipment Marketplace.
 * Stores core catalog fields: name, description, price in Iraqi Dinars (IQD), category, and image URL.
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "name_ar")
    val nameAr: String = "",

    @ColumnInfo(name = "name_ku")
    val nameKu: String = "",

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "description_ar")
    val descriptionAr: String = "",

    @ColumnInfo(name = "description_ku")
    val descriptionKu: String = "",

    @ColumnInfo(name = "price_iqd")
    val priceIqd: Long,

    @ColumnInfo(name = "promo_price_iqd")
    val promoPriceIqd: Long? = null,

    @ColumnInfo(name = "base_price_usd")
    val basePriceUsd: Double? = null,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,

    @ColumnInfo(name = "sku")
    val sku: String = "",

    @ColumnInfo(name = "brand")
    val brand: String = "ONYX",

    @ColumnInfo(name = "sub_category")
    val subCategory: String = "",

    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int = 0,

    @ColumnInfo(name = "is_featured")
    val isFeatured: Boolean = false,

    @ColumnInfo(name = "is_best_seller")
    val isBestSeller: Boolean = false,

    @ColumnInfo(name = "is_new_arrival")
    val isNewArrival: Boolean = false,

    @ColumnInfo(name = "rating")
    val rating: Double = 5.0,

    @ColumnInfo(name = "review_count")
    val reviewCount: Int = 0
) {
    /**
     * Map Room entity to domain Product model
     */
    fun toDomainModel(): Product {
        val mappedCategory = try {
            ProductCategory.valueOf(category.uppercase())
        } catch (_: Exception) {
            ProductCategory.ALL
        }

        return Product(
            id = id,
            sku = sku.ifBlank { "SKU-$id" },
            nameEn = name,
            nameAr = nameAr.ifBlank { name },
            nameKu = nameKu.ifBlank { nameAr.ifBlank { name } },
            descriptionEn = description,
            descriptionAr = descriptionAr.ifBlank { description },
            descriptionKu = descriptionKu.ifBlank { descriptionAr.ifBlank { description } },
            category = mappedCategory,
            subCategory = subCategory,
            brand = brand,
            regularPriceIqd = priceIqd,
            promoPriceIqd = promoPriceIqd,
            basePriceUsd = basePriceUsd,
            stockQuantity = stockQuantity,
            imageUrl = imageUrl,
            isFeatured = isFeatured,
            isBestSeller = isBestSeller,
            isNewArrival = isNewArrival,
            rating = rating,
            reviewCount = reviewCount
        )
    }

    companion object {
        /**
         * Create ProductEntity from domain Product model
         */
        fun fromDomainModel(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.nameEn,
                nameAr = product.nameAr,
                nameKu = product.nameKu,
                description = product.descriptionEn,
                descriptionAr = product.descriptionAr,
                descriptionKu = product.descriptionKu,
                priceIqd = product.regularPriceIqd,
                promoPriceIqd = product.promoPriceIqd,
                basePriceUsd = product.basePriceUsd,
                category = product.category.name,
                imageUrl = product.imageUrl,
                sku = product.sku,
                brand = product.brand,
                subCategory = product.subCategory,
                stockQuantity = product.stockQuantity,
                isFeatured = product.isFeatured,
                isBestSeller = product.isBestSeller,
                isNewArrival = product.isNewArrival,
                rating = product.rating,
                reviewCount = product.reviewCount
            )
        }
    }
}
