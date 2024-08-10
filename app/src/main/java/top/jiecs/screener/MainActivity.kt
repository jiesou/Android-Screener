package top.jiecs.screener

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

import rikka.shizuku.Shizuku

import top.jiecs.screener.databinding.ActivityMainBinding
import top.jiecs.screener.services.UserService
import top.jiecs.screener.services.UserServiceProvider
import top.jiecs.screener.ui.displaymode.DisplayModeFragment


class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()

    private val userServiceArgs: Shizuku.UserServiceArgs =
        Shizuku.UserServiceArgs(
            ComponentName(BuildConfig.APPLICATION_ID, UserService::class.java.name),
        )
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top (same) level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_resolution, R.id.nav_frame_rate, R.id.nav_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        Shizuku.addRequestPermissionResultListener { _, grantResult ->
            // Show message based on the result
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                mainViewModel.shizukuPermissionGranted.value = true
                Snackbar.make(
                    binding.root, R.string.shizuku_permission_granted,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        mainViewModel.shizukuPermissionGranted.observe(this) {
            if (it) bindUserServices()
        }
    }

    override fun onResume() {
        super.onResume()

        if (checkPermission()) {
            mainViewModel.shizukuPermissionGranted.value = true
        } else {
            Snackbar.make(
                binding.root, R.string.shizuku_not_available,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun bindUserServices() {
        Shizuku.bindUserService(userServiceArgs, UserServiceProvider.connection)
    }

    private fun checkPermission(): Boolean = runCatching {
        if (Shizuku.isPreV11()) return false
        // Pre-v11 is unsupported

        return when {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> true // Granted
            Shizuku.shouldShowRequestPermissionRationale() -> false // Users choose "Deny and don't ask again"
            else -> {
                // Request the permission
                Shizuku.requestPermission(0)
                false
            }
        }
    }.getOrElse {
        it.printStackTrace()
        false
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val directionId = when (pref.fragment) {
            DisplayModeFragment::class.java.name -> R.id.nav_display_modes
            else -> return false
        }
        caller.findNavController().navigate(directionId)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
