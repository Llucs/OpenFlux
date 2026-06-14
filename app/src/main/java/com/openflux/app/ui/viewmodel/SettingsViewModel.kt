package com.openflux.app.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openflux.app.OpenFluxApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppSettings(
    val apiKey: String = "public",
    val model: String = "deepseek-v4-flash-free",
    val baseUrl: String = "https://opencode.ai/zen/v1/chat/completions",
    val maxTokens: Int = 4096,
    val darkMode: Boolean = true,
    val autoCompact: Boolean = true,
    val compactThreshold: Int = 180000,
    val streamResponse: Boolean = true,
    val maxIterations: Int = 25,
    val sessionTimeout: Int = 300
)

class SettingsViewModel : ViewModel() {
    private val prefs: SharedPreferences =
        OpenFluxApp.instance.getSharedPreferences("openflux_settings", 0)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings = AppSettings(
        apiKey = prefs.getString("api_key", "public") ?: "public",
        model = prefs.getString("model", "deepseek-v4-flash-free") ?: "deepseek-v4-flash-free",
        baseUrl = prefs.getString("base_url", "https://opencode.ai/zen/v1/chat/completions")
            ?: "https://opencode.ai/zen/v1/chat/completions",
        maxTokens = prefs.getInt("max_tokens", 4096),
        darkMode = prefs.getBoolean("dark_mode", true),
        autoCompact = prefs.getBoolean("auto_compact", true),
        compactThreshold = prefs.getInt("compact_threshold", 180000),
        streamResponse = prefs.getBoolean("stream_response", true),
        maxIterations = prefs.getInt("max_iterations", 25),
        sessionTimeout = prefs.getInt("session_timeout", 300)
    )

    fun updateSettings(newSettings: AppSettings) {
        _settings.value = newSettings
        viewModelScope.launch {
            prefs.edit()
                .putString("api_key", newSettings.apiKey)
                .putString("model", newSettings.model)
                .putString("base_url", newSettings.baseUrl)
                .putInt("max_tokens", newSettings.maxTokens)
                .putBoolean("dark_mode", newSettings.darkMode)
                .putBoolean("auto_compact", newSettings.autoCompact)
                .putInt("compact_threshold", newSettings.compactThreshold)
                .putBoolean("stream_response", newSettings.streamResponse)
                .putInt("max_iterations", newSettings.maxIterations)
                .putInt("session_timeout", newSettings.sessionTimeout)
                .apply()
        }
    }
}
