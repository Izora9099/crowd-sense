package com.example.crowdsensenet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crowdsensenet.R
import com.example.crowdsensenet.data.local.AppDatabase
import com.example.crowdsensenet.utils.LocationUtils
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.config.Configuration
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MetricsActivity : AppCompatActivity() {
    
    private lateinit var rsrpValueText: TextView
    private lateinit var rsrqValueText: TextView
    private lateinit var cellIdText: TextView
    private lateinit var pciText: TextView
    private lateinit var networkTechnologyText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    
    private lateinit var database: AppDatabase
    private lateinit var map: MapView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure OpenStreetMap
        Configuration.getInstance().userAgentValue = packageName
        
        setContentView(R.layout.activity_metrics)
        
        database = AppDatabase.getDatabase(this)
        initializeViews()
        setupNavigation()
        setupMap()
        startMetricsUpdates()
    }
    
    private fun initializeViews() {
        rsrpValueText = findViewById(R.id.rsrp_value)
        rsrqValueText = findViewById(R.id.rsrq_value)
        cellIdText = findViewById(R.id.cell_id)
        pciText = findViewById(R.id.pci)
        networkTechnologyText = findViewById(R.id.network_technology)
        latitudeText = findViewById(R.id.latitude)
        longitudeText = findViewById(R.id.longitude)
    }
    
    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.navigation_metrics
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_metrics -> true
                R.id.navigation_uploads -> {
                    startActivity(Intent(this, UploadsActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupMap() {
        map = findViewById(R.id.long_lat_map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        
        // Set default zoom
        val mapController = map.controller
        mapController.setZoom(15.0)
        
        updateMapWithLatestData()
    }
    
    private fun startMetricsUpdates() {
        lifecycleScope.launch {
            while (true) {
                try {
                    updateMetricsDisplay()
                    delay(3000) // Update every 3 seconds
                } catch (e: Exception) {
                    e.printStackTrace()
                    delay(5000)
                }
            }
        }
    }
    
    private fun updateMetricsDisplay() {
        lifecycleScope.launch {
            try {
                val latestMeasurement = database.measurementDao().getLatestMeasurement()
                
                if (latestMeasurement != null) {
                    runOnUiThread {
                        rsrpValueText.text = "${latestMeasurement.rsrp} dBm"
                        rsrqValueText.text = "${latestMeasurement.rsrq} dB"
                        cellIdText.text = latestMeasurement.cellId
                        pciText.text = latestMeasurement.pci.toString()
                        networkTechnologyText.text = latestMeasurement.networkTechnology
                        latitudeText.text = "%.6f".format(latestMeasurement.latitude)
                        longitudeText.text = "%.6f".format(latestMeasurement.longitude)
                    }
                    
                    updateMapWithMeasurement(latestMeasurement)
                } else {
                    runOnUiThread {
                        rsrpValueText.text = "No Data"
                        rsrqValueText.text = "No Data"
                        cellIdText.text = "No Data"
                        pciText.text = "No Data"
                        networkTechnologyText.text = "No Data"
                        latitudeText.text = "No Data"
                        longitudeText.text = "No Data"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateMapWithLatestData() {
        lifecycleScope.launch {
            try {
                val latestMeasurement = database.measurementDao().getLatestMeasurement()
                latestMeasurement?.let { measurement ->
                    updateMapWithMeasurement(measurement)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateMapWithMeasurement(measurement: com.example.crowdsensenet.data.local.MeasurementEntity) {
        runOnUiThread {
            try {
                val location = GeoPoint(measurement.latitude, measurement.longitude)
                
                // Clear existing markers
                map.overlays.clear()
                
                // Add new marker
                val marker = Marker(map)
                marker.position = location
                marker.title = "Latest Measurement"
                marker.subDescription = "RSRP: ${measurement.rsrp} dBm\nRSRQ: ${measurement.rsrq} dB\nNetwork: ${measurement.networkTechnology}"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(marker)
                
                // Center map on location
                map.controller.setCenter(location)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        map.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        map.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        map.onDetach()
    }
}
