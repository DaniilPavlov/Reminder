package com.example.reminder

import android.app.AlarmManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// TODO: change design, add state management

class MainActivity : ComponentActivity() {
    // Запрос на разрешение отправки уведомлений
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, R.string.permission_warning, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Скрываем статус бар. Системные окна не будут перекрывать контент приложения
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(
                this
            ).areNotificationsEnabled()
        ) {
            // Запрашиваем разрешение на отправку уведомлений, если стоит запрет
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Если в приложении нельзя устанавливать точные будильники, отправляем пользователя в настройки
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
        setContent {
            // Скрываем системные бары
            val systemUiController: SystemUiController = rememberSystemUiController()
            systemUiController.isSystemBarsVisible = false
            systemUiController.isNavigationBarVisible = false

            val viewModel: RemindersViewModel = viewModel()

            viewModel.dbHelper = DatabaseHelper(LocalContext.current)
            viewModel.alarmManager = alarmManager

            // Главный контейнер приложения
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(id = R.color.black), colorResource(id = R.color.navy)
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppTitle()
                Form(viewModel)
                List(viewModel)
            }
        }
    }
}

@Composable
fun AppTitle() {
    Text(
        text = stringResource(id = R.string.app_title), style = TextStyle(
            color = colorResource(id = R.color.white),
            fontSize = 26.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Thin
        )
    )
}












