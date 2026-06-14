package com.example.reminder

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reminder.presentation.product.ProductCatalogScreen
import com.example.reminder.presentation.product.ProductPresenter
import com.example.reminder.presentation.reminder.RemindersViewModel
import com.example.reminder.presentation.sdui.SduiBanner
import com.example.reminder.ui.theme.AppDimens
import com.example.reminder.util.LocaleManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Главный экран приложения (точка входа после запуска).
 *
 * Отвечает за:
 * - локализацию (attachBaseContext + кнопка RU/EN);
 * - системные отступы (statusBarsPadding), чтобы контент не перекрывался;
 * - разрешения (уведомления, точные будильники);
 * - вкладки «Напоминания» (MVVM) и «Каталог» (MVP).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Presenter каталога товаров (паттерн MVP). Создаётся через Dagger Hilt. */
    @Inject
    lateinit var productPresenter: ProductPresenter

    /** Лаунчер запроса POST_NOTIFICATIONS (Android 13+). */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, R.string.permission_warning, Toast.LENGTH_LONG).show()
            }
        }

    /** Применяет сохранённый язык до создания Activity. */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Контент рисуется под системными барами, отступы добавляем через statusBarsPadding()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        requestRuntimePermissions(alarmManager)

        setContent {
            val remindersViewModel: RemindersViewModel = hiltViewModel()
            remindersViewModel.alarmManager = alarmManager

            // recreate() после смены языка пересобирает UI с новыми strings.xml
            ReminderApp(
                remindersViewModel = remindersViewModel,
                productPresenter = productPresenter,
                onToggleLanguage = { recreate() },
            )
        }
    }

    /** Запрашивает разрешения для push-уведомлений и exact alarm. */
    private fun requestRuntimePermissions(alarmManager: AlarmManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationManagerCompat.from(this).areNotificationsEnabled()
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }
}

/** Корневой Compose-контейнер: шапка, SDUI-баннер, вкладки, контент. */
@Composable
private fun ReminderApp(
    remindersViewModel: RemindersViewModel,
    productPresenter: ProductPresenter,
    onToggleLanguage: () -> Unit,
) {
    // 0 = напоминания, 1 = каталог товаров
    var selectedTab by remember { mutableIntStateOf(0) }
    val uiState by remindersViewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val language = LocaleManager.getLanguage(context)

    LaunchedEffect(language) {
        remindersViewModel.reloadSduiBanner()
    }

    LaunchedEffect(uiState.message) {
        // Toast для одноразовых сообщений из ViewModel
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            remindersViewModel.consumeMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorResource(id = R.color.black),
                        colorResource(id = R.color.navy),
                    ),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppHeader(onToggleLanguage = onToggleLanguage)

        uiState.sduiBanner?.let { banner ->
            SduiBanner(
                block = banner,
                onActionClick = { selectedTab = 1 },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppDimens.screenHorizontal),
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.screenHorizontal,
                    vertical = AppDimens.sectionSpacing,
                ),
            containerColor = colorResource(id = R.color.dark_navy),
            contentColor = colorResource(id = R.color.white),
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = colorResource(id = R.color.button_gradient_purple),
                    )
                }
            },
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.tab_reminders)) },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.tab_catalog)) },
            )
        }

        when (selectedTab) {
            0 -> RemindersTabContent(
                viewModel = remindersViewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            1 -> ProductCatalogScreen(
                presenter = productPresenter,
                onProductChosen = { product ->
                    remindersViewModel.applyProductSelection(product)
                    selectedTab = 0
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }
    }
}

/** Заголовок + кнопка переключения языка RU/EN. */
@Composable
private fun AppHeader(onToggleLanguage: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val switchLabel = if (LocaleManager.isRussian(context)) {
        stringResource(R.string.language_switch_to_english)
    } else {
        stringResource(R.string.language_switch_to_russian)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppDimens.screenHorizontal,
                vertical = AppDimens.sectionSpacing,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.app_title),
            style = TextStyle(
                color = colorResource(id = R.color.white),
                fontSize = 24.sp,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Thin,
            ),
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(
            onClick = {
                LocaleManager.toggleLanguage(context)
                LocaleManager.applyAppLocale(context.applicationContext)
                onToggleLanguage()
            },
        ) {
            Text(text = switchLabel)
        }
    }
}
