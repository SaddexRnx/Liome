package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.UserPreferencesRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.LocalMindTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = UserPreferencesRepository(applicationContext)

        setContent {
            val userSettings by userPrefs.settings.collectAsState()

            LocalMindTheme(
                themeMode = userSettings.themeMode,
                themeStyle = userSettings.themeStyle
            ) {
                AppNavigation(
                    userPrefs = userPrefs,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
