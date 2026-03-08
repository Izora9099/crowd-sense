package com.example.crowdsensenet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.crowdsensenet.ui.DashboardActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var percentageLabel: TextView
    private var progressStatus = 0
    private val handler = Handler(Looper.getMainLooper())

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        android.util.Log.d("MainActivity", "Permission results: $permissions, allGranted: $allGranted")
        if (allGranted) {
            continueInitialization()
        } else {
            val deniedPermissions = permissions.filterValues { !it }.keys
            android.util.Log.d("MainActivity", "Denied permissions: $deniedPermissions")
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.splash_progress)
        percentageLabel = findViewById(R.id.percentage_label)

        checkPermissionsAndInitialize()
    }

    private fun checkPermissionsAndInitialize() {
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            continueInitialization()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app requires all permissions to function properly. Without them, the app cannot collect network measurements. Please grant permissions in Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Exit") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun continueInitialization() {
        android.util.Log.d("MainActivity", "Starting initialization...")
        startLoading()
    }

    private fun startLoading() {
        android.util.Log.d("MainActivity", "Starting loading thread...")
        // Reset progress to 0%
        progressStatus = 0
        handler.post {
            progressBar.progress = 0
            percentageLabel.text = "0%"
        }
        
        Thread {
            try {
                // Initialize Firebase in background thread
                android.util.Log.d("MainActivity", "Initializing Firebase...")
                FirebaseApp.initializeApp(this)
                val db = Firebase.firestore
                android.util.Log.d("MainActivity", "Firebase initialized successfully")
                
                // Simulate initialization progress
                while (progressStatus < 100) {
                    progressStatus += 2
                    handler.post {
                        progressBar.progress = progressStatus
                        percentageLabel.text = "$progressStatus%"
                        android.util.Log.d("MainActivity", "Progress: $progressStatus%")
                    }
                    try {
                        Thread.sleep(50) // Slightly slower for visibility
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        break
                    }
                }
                
                if (progressStatus >= 100) {
                    android.util.Log.d("MainActivity", "Progress complete, starting DashboardActivity...")
                    handler.post {
                        startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                        finish()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Initialization failed", e)
                e.printStackTrace()
                handler.post {
                    Toast.makeText(this@MainActivity, "Initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}