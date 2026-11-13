package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.StorePreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gestao_store_prefs")

class StorePreferencesManager(private val context: Context) {

    private object Keys {
        val STORE_NAME = stringPreferencesKey("store_name")
        val BUSINESS_INFO = stringPreferencesKey("business_info")
        val DEFAULT_MIN_STOCK = intPreferencesKey("default_min_stock")
        val SHOW_LOW_STOCK_ALERTS = booleanPreferencesKey("show_low_stock_alerts")
        val CONFIRM_BEFORE_SALE = booleanPreferencesKey("confirm_before_sale")
    }

    val preferencesFlow: Flow<StorePreferences> = context.dataStore.data.map { prefs ->
        StorePreferences(
            storeName = prefs[Keys.STORE_NAME] ?: "Gestão Store",
            businessInfo = prefs[Keys.BUSINESS_INFO] ?: "Moda Masculina & Feminina",
            defaultMinStock = (prefs[Keys.DEFAULT_MIN_STOCK] ?: 3).coerceAtLeast(1),
            showLowStockAlerts = prefs[Keys.SHOW_LOW_STOCK_ALERTS] ?: true,
            confirmBeforeCompletingSale = prefs[Keys.CONFIRM_BEFORE_SALE] ?: true
        )
    }

    suspend fun updateStoreIdentity(storeName: String, businessInfo: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STORE_NAME] = storeName.trim().ifEmpty { "Gestão Store" }
            prefs[Keys.BUSINESS_INFO] = businessInfo.trim()
        }
    }

    suspend fun updatePreferences(
        defaultMinStock: Int,
        showLowStockAlerts: Boolean,
        confirmBeforeCompletingSale: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_MIN_STOCK] = defaultMinStock.coerceAtLeast(1)
            prefs[Keys.SHOW_LOW_STOCK_ALERTS] = showLowStockAlerts
            prefs[Keys.CONFIRM_BEFORE_SALE] = confirmBeforeCompletingSale
        }
    }

    suspend fun restoreFromBackup(preferences: StorePreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STORE_NAME] = preferences.storeName
            prefs[Keys.BUSINESS_INFO] = preferences.businessInfo
            prefs[Keys.DEFAULT_MIN_STOCK] = preferences.defaultMinStock.coerceAtLeast(1)
            prefs[Keys.SHOW_LOW_STOCK_ALERTS] = preferences.showLowStockAlerts
            prefs[Keys.CONFIRM_BEFORE_SALE] = preferences.confirmBeforeCompletingSale
        }
    }
}
