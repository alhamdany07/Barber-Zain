package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shop_settings")

data class ShopConfig(
    val shopName: String = "Pangkas Rambut Zain",
    val shopLogoUri: String? = null,
    val shopAddress: String = "Jl. Merdeka No. 45, Kota",
    val shopPhone: String = "0812-3456-7890",
    val receiptFooter: String = "Terima kasih atas kunjungan Anda! Tetap ganteng dan rapi.",
    val enableKapster: Boolean = true,
    val enableStock: Boolean = true,
    val enableQueue: Boolean = true,
    val taxPercentage: Double = 0.0,
    val enableTts: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val themeColor: AppThemeColor = AppThemeColor.NAVY,
    val isDarkMode: Boolean = false,
    val tvServerEnabled: Boolean = true,
    val tvServerPort: Int = 8080
)

class ShopPreferences(private val context: Context) {
    companion object {
        val KEY_SHOP_NAME = stringPreferencesKey("shop_name")
        val KEY_SHOP_LOGO = stringPreferencesKey("shop_logo_uri")
        val KEY_SHOP_ADDRESS = stringPreferencesKey("shop_address")
        val KEY_SHOP_PHONE = stringPreferencesKey("shop_phone")
        val KEY_RECEIPT_FOOTER = stringPreferencesKey("receipt_footer")
        val KEY_ENABLE_KAPSTER = booleanPreferencesKey("enable_kapster")
        val KEY_ENABLE_STOCK = booleanPreferencesKey("enable_stock")
        val KEY_ENABLE_QUEUE = booleanPreferencesKey("enable_queue")
        val KEY_TAX_PERCENTAGE = doublePreferencesKey("tax_percentage")
        val KEY_ENABLE_TTS = booleanPreferencesKey("enable_tts")
        val KEY_TTS_SPEED = floatPreferencesKey("tts_speed")
        val KEY_THEME_COLOR = stringPreferencesKey("theme_color")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_TV_SERVER_ENABLED = booleanPreferencesKey("tv_server_enabled")
        val KEY_TV_SERVER_PORT = intPreferencesKey("tv_server_port")
    }

    val shopConfigFlow: Flow<ShopConfig> = context.dataStore.data.map { prefs ->
        ShopConfig(
            shopName = prefs[KEY_SHOP_NAME] ?: "Pangkas Rambut Zain",
            shopLogoUri = prefs[KEY_SHOP_LOGO],
            shopAddress = prefs[KEY_SHOP_ADDRESS] ?: "Jl. Merdeka No. 45, Kota",
            shopPhone = prefs[KEY_SHOP_PHONE] ?: "0812-3456-7890",
            receiptFooter = prefs[KEY_RECEIPT_FOOTER] ?: "Terima kasih atas kunjungan Anda! Tetap ganteng dan rapi.",
            enableKapster = prefs[KEY_ENABLE_KAPSTER] ?: true,
            enableStock = prefs[KEY_ENABLE_STOCK] ?: true,
            enableQueue = prefs[KEY_ENABLE_QUEUE] ?: true,
            taxPercentage = prefs[KEY_TAX_PERCENTAGE] ?: 0.0,
            enableTts = prefs[KEY_ENABLE_TTS] ?: true,
            ttsSpeed = prefs[KEY_TTS_SPEED] ?: 1.0f,
            themeColor = prefs[KEY_THEME_COLOR]?.let { name ->
                runCatching { AppThemeColor.valueOf(name) }.getOrNull()
            } ?: AppThemeColor.NAVY,
            isDarkMode = prefs[KEY_DARK_MODE] ?: false,
            tvServerEnabled = prefs[KEY_TV_SERVER_ENABLED] ?: true,
            tvServerPort = prefs[KEY_TV_SERVER_PORT] ?: 8080
        )
    }

    suspend fun updateShopProfile(name: String, address: String, phone: String, footer: String, logoUri: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOP_NAME] = name
            prefs[KEY_SHOP_ADDRESS] = address
            prefs[KEY_SHOP_PHONE] = phone
            prefs[KEY_RECEIPT_FOOTER] = footer
            if (logoUri != null) {
                prefs[KEY_SHOP_LOGO] = logoUri
            }
        }
    }

    suspend fun updateLogo(logoUri: String?) {
        context.dataStore.edit { prefs ->
            if (logoUri != null) {
                prefs[KEY_SHOP_LOGO] = logoUri
            } else {
                prefs.remove(KEY_SHOP_LOGO)
            }
        }
    }

    suspend fun updateToggles(enableKapster: Boolean, enableStock: Boolean, enableQueue: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ENABLE_KAPSTER] = enableKapster
            prefs[KEY_ENABLE_STOCK] = enableStock
            prefs[KEY_ENABLE_QUEUE] = enableQueue
        }
    }

    suspend fun updateTax(tax: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TAX_PERCENTAGE] = tax
        }
    }

    suspend fun updateTts(enabled: Boolean, speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ENABLE_TTS] = enabled
            prefs[KEY_TTS_SPEED] = speed
        }
    }

    suspend fun updateTheme(themeColor: AppThemeColor, isDarkMode: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_COLOR] = themeColor.name
            prefs[KEY_DARK_MODE] = isDarkMode
        }
    }

    suspend fun updateTvServer(enabled: Boolean, port: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TV_SERVER_ENABLED] = enabled
            prefs[KEY_TV_SERVER_PORT] = port
        }
    }
}
