package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object (DAO) interface for handling CRUD operations on the 'Product' entity.
 */
@Dao
interface ProductDao {

    // --- CREATE ---

    /**
     * Insert a single product entity into the database. Replaces existing product on ID conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    /**
     * Insert multiple product entities in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // --- READ ---

    /**
     * Observe all products, sorted by featured status and name.
     */
    @Query("SELECT * FROM products ORDER BY is_featured DESC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    /**
     * Query a single product by its unique primary key ID.
     */
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    /**
     * Observe products filtered by category (e.g. COFFEE, MACHINES, EQUIPMENT, ACCESSORIES).
     */
    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    /**
     * Search products by English or Arabic name, or brand.
     */
    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :query || '%' 
           OR name_ar LIKE '%' || :query || '%' 
           OR brand LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    /**
     * Observe featured products for the storefront.
     */
    @Query("SELECT * FROM products WHERE is_featured = 1")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    /**
     * Observe best-selling products.
     */
    @Query("SELECT * FROM products WHERE is_best_seller = 1")
    fun getBestSellers(): Flow<List<ProductEntity>>

    /**
     * Count total number of products in database.
     */
    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): Flow<Int>

    // --- UPDATE ---

    /**
     * Update product record details.
     */
    @Update
    suspend fun updateProduct(product: ProductEntity)

    /**
     * Update available stock quantity directly for a product.
     */
    @Query("UPDATE products SET stock_quantity = :newStock WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int)

    /**
     * Update regular and promotional prices in IQD.
     */
    @Query("UPDATE products SET price_iqd = :priceIqd, promo_price_iqd = :promoPriceIqd WHERE id = :productId")
    suspend fun updatePrice(productId: String, priceIqd: Long, promoPriceIqd: Long?)

    // --- DELETE ---

    /**
     * Delete a product entity by entity object.
     */
    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    /**
     * Delete a product entity by its unique ID.
     */
    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String)

    /**
     * Delete all products from the database (wipe table).
     */
    @Query("DELETE FROM products")
    suspend fun clearAllProducts()
}
