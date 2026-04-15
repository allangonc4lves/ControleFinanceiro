package br.dev.allan.controlefinanceiro.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "settings")

private val CURRENCY_CODE = stringPreferencesKey("currency_code")
class SettingsManager(private val context: Context) {

    private val IS_BALANCE_VISIBLE = booleanPreferencesKey("is_balance_visible")

    val isBalanceVisible: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_BALANCE_VISIBLE] ?: true }

    suspend fun setBalanceVisible(isVisible: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_BALANCE_VISIBLE] = isVisible
        }
    }

    val currencyCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENCY_CODE] ?: getDefaultCurrencyBySystem()
        }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENCY_CODE] = code
        }
    }

    private fun getDefaultCurrencyBySystem(): String {
        val systemLocale = Locale.getDefault().language

        return when (systemLocale) {
            "pt" -> "BRL"
            "en" -> "USD"
            "es" -> "ARS"
            else -> "BRL"
        }
    }
}