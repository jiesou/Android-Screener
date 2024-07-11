import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.properties.Properties
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
}