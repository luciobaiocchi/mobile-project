package com.heard.mobile.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeOption { LIGHT, DARK, SYSTEM }

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class ThemePreferenceDataStore(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_option")
    }

    val themeFlow: Flow<ThemeOption> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            ThemeOption.LIGHT.name -> ThemeOption.LIGHT
            ThemeOption.DARK.name -> ThemeOption.DARK
            else -> ThemeOption.SYSTEM
        }
    }

    suspend fun setTheme(option: com.heard.mobile.ui.screens.settings.ThemeOption) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = option.name
        }
    }
}
