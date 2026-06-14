package com.example.reminder.data.remote

import com.example.reminder.data.remote.dto.ProductDto
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit-интерфейс e-commerce API (FakeStore).
 *
 * Retrofit по аннотациям @GET генерирует HTTP-запросы.
 * Observable — результат сразу в RxJava-поток (через RxJava3CallAdapterFactory).
 */
interface ProductApi {

    @GET("products")
    fun getProducts(): Observable<List<ProductDto>>

    @GET("products/category/{category}")
    fun getProductsByCategory(@Path("category") category: String): Observable<List<ProductDto>>
}
