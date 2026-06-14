package com.example.reminder.presentation.product

import com.example.reminder.domain.model.Product

/**
 * Контракт MVP для экрана каталога товаров.
 *
 * View — UI (Compose), Presenter — бизнес-логика, Model — Product + Repository.
 * Presenter не знает про Compose: он вызывает методы View-интерфейса.
 */
interface ProductContract {

    /** Что UI умеет показывать (реализует ProductCatalogScreen). */
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showProducts(products: List<Product>)
        fun showError(message: String)
        fun navigateToRemindersWithProduct(product: Product)
    }

    /** Команды от UI к логике (реализует ProductPresenter). */
    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun loadProducts(category: String? = null)
        fun onProductSelected(product: Product)
    }
}
