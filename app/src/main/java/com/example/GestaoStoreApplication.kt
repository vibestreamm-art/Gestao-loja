package com.example

import android.app.Application
import com.example.data.StorePreferencesManager
import com.example.database.AppDatabase
import com.example.repository.StoreRepository

class GestaoStoreApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferencesManager: StorePreferencesManager by lazy { StorePreferencesManager(this) }
    val repository: StoreRepository by lazy { StoreRepository(database, preferencesManager) }
}
