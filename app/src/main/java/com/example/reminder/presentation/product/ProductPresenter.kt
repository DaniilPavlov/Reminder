package com.example.reminder.presentation.product

import com.example.reminder.analytics.AnalyticsTracker
import com.example.reminder.data.repository.ProductRepository
import com.example.reminder.domain.model.Product
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

/**
 * Presenter каталога (MVP).
 *
 * Загружает товары через Retrofit → ProductRepository → RxJava Observable.
 * Не хранит Context и не знает про Compose — только про ProductContract.View.
 */
class ProductPresenter @Inject constructor(
    private val productRepository: ProductRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ProductContract.Presenter {

    private var view: ProductContract.View? = null
    private val disposables = CompositeDisposable()

    /** Привязка View при появлении экрана. */
    override fun attach(view: ProductContract.View) {
        this.view = view
    }

    /** Отписка при уходе с экрана — важно для предотвращения утечек. */
    override fun detach() {
        disposables.clear()
        view = null
    }

    /**
     * Запрос товаров с сервера FakeStore API.
     * @param category фильтр категории или null для всех товаров
     */
    override fun loadProducts(category: String?) {
        view?.showLoading()
        disposables.add(
            productRepository.getProducts(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ products ->
                    view?.hideLoading()
                    view?.showProducts(products)
                }, { error ->
                    view?.hideLoading()
                    view?.showError(error.localizedMessage ?: "Failed to load products")
                }),
        )
    }

    /** Пользователь тапнул товар — логируем и просим View перейти к форме напоминания. */
    override fun onProductSelected(product: Product) {
        analyticsTracker.logProductSelected(product.id, product.category)
        view?.navigateToRemindersWithProduct(product)
    }
}
