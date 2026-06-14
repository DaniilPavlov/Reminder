package com.example.reminder.presentation.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reminder.R
import com.example.reminder.domain.model.Product
import com.example.reminder.ui.theme.AppDimens

/**
 * UI каталога товаров — реализует [ProductContract.View] (MVP).
 *
 * DisposableEffect: attach/detach Presenter при появлении/исчезновении composable.
 */

@Composable
fun ProductCatalogScreen(
    presenter: ProductPresenter,
    onProductChosen: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isLoading by remember { mutableStateOf(false) }
    var products by remember { mutableStateOf(emptyList<Product>()) }
    var error by remember { mutableStateOf<String?>(null) }

    // Анонимный объект View — Presenter вызывает эти методы, UI обновляется
    val view = remember(onProductChosen) {
        object : ProductContract.View {
            override fun showLoading() {
                isLoading = true
                error = null
            }

            override fun hideLoading() {
                isLoading = false
            }

            override fun showProducts(items: List<Product>) {
                products = items
            }

            override fun showError(message: String) {
                error = message
            }

            override fun navigateToRemindersWithProduct(product: Product) {
                onProductChosen(product)
            }
        }
    }

    DisposableEffect(presenter) {
        presenter.attach(view)
        presenter.loadProducts(category = "jewelery") // e-commerce demo: категория с FakeStore
        onDispose { presenter.detach() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.catalog_title),
            color = colorResource(R.color.white),
            fontWeight = FontWeight.Thin,
            modifier = Modifier.padding(
                horizontal = AppDimens.screenHorizontal,
                vertical = AppDimens.sectionSpacing,
            ),
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.blue))
                }
            }

            error != null -> {
                Text(
                    text = error.orEmpty(),
                    color = colorResource(R.color.white),
                    modifier = Modifier.padding(16.dp),
                )
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = AppDimens.screenHorizontal,
                        vertical = AppDimens.sectionSpacing,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppDimens.fieldSpacing),
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductRow(product = product) {
                            presenter.onProductSelected(product)
                        }
                    }
                }
            }
        }
    }
}

/** Одна строка товара: название и цена. */
@Composable
private fun ProductRow(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(AppDimens.cardInner),
    ) {
        Text(
            text = product.title,
            color = colorResource(R.color.white),
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.catalog_price_format, product.price, product.category),
            color = colorResource(R.color.blue),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
