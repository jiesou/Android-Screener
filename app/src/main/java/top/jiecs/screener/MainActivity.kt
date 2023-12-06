package top.jiecs.screener

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.google.android.material.snackbar.Snackbar

import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuSystemProperties
import android.content.pm.PackageManager


import com.google.android.material.bottomnavigation.BottomNavigationView

import top.jiecs.screener.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_resolution, R.id.navigation_frame_rate, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
		
		Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }

    private val REQUEST_PERMISSION_RESULT_LISTENER =
      Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
      // Show message based on the result
      if (grantResult != PackageManager.PERMISSION_GRANTED) {
        Snackbar.make(binding.root, R.string.shizuku_not_available,
          Snackbar.LENGTH_SHORT).show()
      }
    }
    
    override fun onResume() {
        super.onResume()
        checkPermission(0)
    }

    private fun checkPermission(code: Int): Boolean {
      if (Shizuku.isPreV11()) {
          // Pre-v11 is unsupported
          return false
      }

      return when {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
            // Granted
            true
        }
        Shizuku.shouldShowRequestPermissionRationale() -> {
            // Users choose "Deny and don't ask again"
            false
        }
        else -> {
            // Request the permission
            Shizuku.requestPermission(code)
            false
        }
      }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
	}
    
    
}
