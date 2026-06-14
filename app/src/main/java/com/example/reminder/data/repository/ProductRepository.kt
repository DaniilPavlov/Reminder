package com.example.reminder.data.repository

import com.example.reminder.data.mapper.DataMappers.toDomain
import com.example.reminder.data.remote.ProductApi
import com.example.reminder.domain.model.Product
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository каталога товаров — только remote (FakeStore API).
 *
 * Кэширования нет: каждый loadProducts() = новый HTTP-запрос.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
) {

    /**
     * @param category null = все товары, иначе фильтр /products/category/{category}
     */
    fun getProducts(category: String? = null): Observable<List<Product>> {
        val source = if (category.isNullOrBlank()) {
            productApi.getProducts()
        } else {
            productApi.getProductsByCategory(category)
        }

        return source
            .subscribeOn(Schedulers.io())
            .map { products -> products.map { it.toDomain() } }
            .map { products -> products.sortedBy { it.price } }
    }
}
