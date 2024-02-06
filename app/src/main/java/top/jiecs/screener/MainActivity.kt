package top.jiecs.screener

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import top.jiecs.screener.databinding.ActivityMainBinding

import com.google.android.material.snackbar.Snackbar

import rikka.shizuku.Shizuku
import android.content.pm.PackageManager
import androidx.activity.enableEdgeToEdge
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_resolution, R.id.nav_frame_rate, R.id.nav_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener)
    }

    private val onRequestPermissionResultListener =
      Shizuku.OnRequestPermissionResultListener { _, grantResult ->
      // Show message based on the result
      if (grantResult != PackageManager.PERMISSION_GRANTED) {
        Snackbar.make(binding.root, R.string.shizuku_not_available,
          Snackbar.LENGTH_SHORT).show()
      }
    }
    
    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission(): Boolean {
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
            Shizuku.requestPermission(0)
            false
        }
      }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener)
	}
    
    
}
