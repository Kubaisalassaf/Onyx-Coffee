package com.example.data.local

import kotlinx.coroutines.flow.Flow

/**
 * Repository layer abstracting Room ProductDao operations from ViewModel and UI.
 */
class ProductLocalRepository(private val productDao: ProductDao) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val featuredProducts: Flow<List<ProductEntity>> = productDao.getFeaturedProducts()
    val bestSellers: Flow<List<ProductEntity>> = productDao.getBestSellers()
    val productCount: Flow<Int> = productDao.getProductCount()

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(category)
    }

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query)
    }

    suspend fun getProductById(id: String): ProductEntity? = productDao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity) = productDao.insertProduct(product)

    suspend fun insertProducts(products: List<ProductEntity>) = productDao.insertProducts(products)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun updateStock(productId: String, newStock: Int) = productDao.updateStock(productId, newStock)

    suspend fun updatePrice(productId: String, priceIqd: Long, promoPriceIqd: Long? = null) =
        productDao.updatePrice(productId, priceIqd, promoPriceIqd)

    suspend fun deleteProduct(product: ProductEntity) = productDao.deleteProduct(product)

    suspend fun deleteProductById(id: String) = productDao.deleteProductById(id)

    suspend fun clearAll() = productDao.clearAllProducts()
}
