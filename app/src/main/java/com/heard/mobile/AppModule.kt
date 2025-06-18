package com.heard.mobile

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.heard.mobile.data.database.PathDatabase
import com.heard.mobile.data.repositories.PathRepository
import com.heard.mobile.ui.screens.addPath.AddPathViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("settings")

val appModule = module {
    single { get<Context>().dataStore }

    single {
        Room.databaseBuilder(
            get(),
            PathDatabase::class.java,
            "heard-db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    single {
        PathRepository(
            get<PathDatabase>().pathDAO(),
            get<Context>().contentResolver
        )
    }

    viewModel { AddPathViewModel() }
}
