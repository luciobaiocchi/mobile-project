package com.heard.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.heard.mobile.datastore.ThemeOption
import com.heard.mobile.ui.ApplicationGraph
import com.heard.mobile.ui.theme.MobileTheme
import com.heard.mobile.viewmodel.ThemeViewModel

class MainActivity : ComponentActivity() {

    // ViewModel con factory personalizzata
    private val themeViewModel: ThemeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ThemeViewModel(application) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by themeViewModel.theme.collectAsState()

            val darkTheme = when (theme) {
                ThemeOption.DARK -> true
                ThemeOption.LIGHT -> false
                ThemeOption.SYSTEM -> isSystemInDarkTheme()
            }

            MobileTheme(useDarkTheme = darkTheme) {
                val navController = rememberNavController()
                ApplicationGraph(navController, themeViewModel)
            }
        }
    }
}
