package com.line98.game.data

import android.content.Context
import java.io.IOException
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.line98.game.core.GameMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("line98_preferences")

data class UserSettings(
    val classicHighScore: Int = 0,
    val powerUpHighScore: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val lastMode: GameMode = GameMode.Classic,
)

class UserPreferences(
    private val context: Context,
) {
    private object Keys {
        val ClassicHighScore = intPreferencesKey("classic_high_score")
        val PowerUpHighScore = intPreferencesKey("power_up_high_score")
        val SoundEnabled = booleanPreferencesKey("sound_enabled")
        val HapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val LastMode = stringPreferencesKey("last_mode")
    }

    val settings: Flow<UserSettings> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserSettings(
                    classicHighScore = preferences[Keys.ClassicHighScore] ?: 0,
                    powerUpHighScore = preferences[Keys.PowerUpHighScore] ?: 0,
                    soundEnabled = preferences[Keys.SoundEnabled] ?: true,
                    hapticsEnabled = preferences[Keys.HapticsEnabled] ?: true,
                    lastMode = preferences[Keys.LastMode]
                        ?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
                        ?: GameMode.Classic,
                )
            }

    suspend fun saveHighScore(mode: GameMode, score: Int) {
        context.dataStore.edit { preferences ->
            val key = when (mode) {
                GameMode.Classic -> Keys.ClassicHighScore
                GameMode.PowerUp -> Keys.PowerUpHighScore
            }
            val current = preferences[key] ?: 0
            if (score > current) {
                preferences[key] = score
            }
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SoundEnabled] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HapticsEnabled] = enabled
        }
    }

    suspend fun setLastMode(mode: GameMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LastMode] = mode.name
        }
    }
}
