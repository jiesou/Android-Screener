import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.decodeFromStringMap
import kotlinx.serialization.properties.encodeToStringMap
import top.jiecs.screener.ui.displaymode.DisplayModeContent.DisplayMode

val Context.dataStore by preferencesDataStore(name = "display_modes")

class DataStoreManager(private val context: Context) {

    @OptIn(ExperimentalSerializationApi::class)
    fun saveDisplayModes(modes: List<DisplayMode>) {
        runBlocking {
            context.dataStore.edit { preferences ->
                val map = Properties.encodeToStringMap(modes)
                map.forEach { (strKey, strValue) ->
                    val key = stringPreferencesKey(strKey)
                    preferences[key] = strValue
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun removeDisplayMode(displayMode: DisplayMode) {
        runBlocking {
            context.dataStore.edit { preferences ->
                // Serialize the displayMode to get its key-value representation
                val mapToRemove = Properties.encodeToStringMap(displayMode)

                // Iterate over the keys of the mapToRemove and remove them from preferences
                mapToRemove.keys.forEach { strKey ->
                    val key = stringPreferencesKey(strKey)
                    preferences.remove(key)
                }
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun readDisplayModes(): List<DisplayMode> {
        return context.dataStore.data.map { preferences ->
            val stringMap = preferences.asMap().entries.associate {
                it.key.name to it.value.toString()
            }
            Properties.decodeFromStringMap<List<DisplayMode>>(stringMap)
        }.first()
    }
}
