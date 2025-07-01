package com.heard.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.heard.mobile.datastore.ThemeOption
import com.heard.mobile.datastore.ThemePreferenceDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val themeStore = ThemePreferenceDataStore(application)

    val theme: StateFlow<ThemeOption> = themeStore.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeOption.SYSTEM)

    fun setTheme(option: com.heard.mobile.ui.screens.settings.ThemeOption) {
        viewModelScope.launch {
            themeStore.setTheme(option)
        }
    }
}
