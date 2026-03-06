package com.example.crowdsensenet.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.crowdsensenet.R
import com.example.crowdsensenet.data.local.AppDatabase
import com.example.crowdsensenet.data.local.MeasurementEntity
import com.example.crowdsensenet.utils.LocationUtils
import com.example.crowdsensenet.utils.NetworkUtils
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class SensingService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var database: AppDatabase
    private var isRunning = false

    override fun onCreate() {
        super.onCreate()

        telephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE)
                    as TelephonyManager

        database = AppDatabase.getDatabase(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        startForeground(1, createNotification())
        isRunning = true

        startSensing()

        return START_STICKY
    }

    private fun startSensing() {

        telephonyManager.listen(object : PhoneStateListener() {

            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {

                if (!isRunning) return

                CoroutineScope(Dispatchers.IO).launch @androidx.annotation.RequiresPermission(
                    android.Manifest.permission.READ_PHONE_STATE
                ) {

                    val (rsrp, rsrq) =
                        NetworkUtils.getSignalStrength(signalStrength)

                    val location =
                        LocationUtils.getLocation(this@SensingService)

                    val entity = MeasurementEntity(
                        deviceId = Settings.Secure.getString(
                            contentResolver,
                            Settings.Secure.ANDROID_ID
                        ),
                        timestamp = System.currentTimeMillis(),
                        rsrp = rsrp,
                        rsrq = rsrq,
                        pci = 0.0,
                        cellId = "Unknown",
                        networkTechnology =
                            NetworkUtils.getNetworkType(this@SensingService),
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0
                    )

                    database.measurementDao().insert(entity)
                }
            }

        }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {

        val channelId = "SensingChannel"

        val channel = NotificationChannel(
            channelId,
            "Sensing Service",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("CrowdSenseNet Running")
            .setContentText("Collecting network measurements")
            .setSmallIcon(R.drawable.ic_signal_cellular)
            .build()
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
