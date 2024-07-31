package top.jiecs.screener.ui.displaymode

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.jiecs.screener.MyApplication
import top.jiecs.screener.ui.displaymode.DisplayModeViewModel.DisplayMode

val Context.dataStore by preferencesDataStore("display_modes")
val displayModesKey = stringPreferencesKey("display_modes")

class DataStoreManager(private val context: Context = MyApplication.context) {

    suspend fun save(modes: MutableList<DisplayMode>) {
        context.dataStore.edit { preferences ->
            preferences[displayModesKey] = Json.encodeToString(modes)
        }
    }

    fun read(): Flow<MutableList<DisplayMode>> {
        return context.dataStore.data.map { preferences ->
            preferences[displayModesKey]?.let {
                Json.decodeFromString<MutableList<DisplayMode>>(it)
            } ?: mutableListOf()
        }
    }
}
