package cvo.stundenplan.app.backend

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


// Erstellt eine Erweiterung für Context, die 'dataStore' heißt.
// "settings" ist der Dateiname der Speicherdatei.
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object UserPreferencesKeys {
    val FILLED_CARDS_ENABLED = booleanPreferencesKey("filled_cards_enabled")
    // Zuletzt Hinweise aktualisiert speichern
    val LAST_REFRESH_TIMESTAMP = longPreferencesKey("last_refresh_time")
    // Fügen Sie hier weitere Schlüssel für andere Einstellungen hinzu...
}

suspend fun ableFilledCards(context: Context, setting: Boolean) {
    context.dataStore.edit { preferences ->
        preferences[UserPreferencesKeys.FILLED_CARDS_ENABLED] = setting
    }
}

fun getFilledCardsEnabledFlow(context: Context): Flow<Boolean> {
    return context.dataStore.data.map { preferences ->
        preferences[UserPreferencesKeys.FILLED_CARDS_ENABLED] ?: false
    }
}

suspend fun setRefreshTime(context: Context) {
    val currentTimestamp = System.currentTimeMillis()
    context.dataStore.edit { preferences ->
        preferences[UserPreferencesKeys.LAST_REFRESH_TIMESTAMP] = currentTimestamp
    }
}

fun getLastRefreshTimestampFlow(context: Context): Flow<Long> {
    return context.dataStore.data.map { preferences ->
            preferences[UserPreferencesKeys.LAST_REFRESH_TIMESTAMP] ?: 0L
        }
}