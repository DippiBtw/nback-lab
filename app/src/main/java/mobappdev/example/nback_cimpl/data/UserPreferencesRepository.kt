package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Data model class to hold all user preferences
data class UserPreferences(
    val highscore: Int = 0,
    val nBackLevel: Int = 1,
    val gridSize: Int = 5,
    val numEvents: Int = 10,
    val eventInterval: Long = 2000L
)

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val N_BACK_LEVEL = intPreferencesKey("nBackLevel")
        val GRID_SIZE = intPreferencesKey("gridSize")
        val NUM_EVENTS = intPreferencesKey("numberOfEvents")
        val EVENT_INTERVAL = longPreferencesKey("eventInterval")
        const val TAG = "UserPreferencesRepo"
    }

    // Single Flow to collect all preferences
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Map each preference to a property in UserPreferences
            UserPreferences(
                highscore = preferences[HIGHSCORE] ?: 0,
                nBackLevel = preferences[N_BACK_LEVEL] ?: 1,
                gridSize = preferences[GRID_SIZE] ?: 5,
                numEvents = preferences[NUM_EVENTS] ?: 10,
                eventInterval = preferences[EVENT_INTERVAL] ?: 2000L
            )
        }

    // Functions to save each setting, similar to before

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }

    suspend fun saveNBackLevel(level: Int) {
        dataStore.edit { preferences ->
            preferences[N_BACK_LEVEL] = level
        }
    }

    suspend fun saveGridSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[GRID_SIZE] = size
        }
    }

    suspend fun saveNumEvents(events: Int) {
        dataStore.edit { preferences ->
            preferences[NUM_EVENTS] = events
        }
    }

    suspend fun saveEventInterval(interval: Long) {
        dataStore.edit { preferences ->
            preferences[EVENT_INTERVAL] = interval
        }
    }
}
