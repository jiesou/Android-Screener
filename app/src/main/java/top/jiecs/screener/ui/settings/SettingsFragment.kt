package top.jiecs.screener.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import top.jiecs.screener.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}