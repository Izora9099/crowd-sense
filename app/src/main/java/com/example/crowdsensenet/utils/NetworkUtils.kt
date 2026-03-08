package com.example.crowdsensenet.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.telephony.CellIdentity
import android.telephony.CellInfo
import android.telephony.CellSignalStrengthLte
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine

object NetworkUtils {

    data class NetworkRating(val text: String, val color: Int)

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getNetworkType(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return when (tm.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
            TelephonyManager.NETWORK_TYPE_CDMA -> "2G"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "3G"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "3G"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "3G"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "3G"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "3G"
            TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "WiFi"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"
            else -> "Unknown"
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getSignalStrength(signalStrength: SignalStrength): Pair<Double, Double> {
        val lte = signalStrength.cellSignalStrengths
            .filterIsInstance<CellSignalStrengthLte>()
            .firstOrNull()

        val rsrp = lte?.rsrp?.toDouble() ?: 0.0
        val rsrq = lte?.rsrq?.toDouble() ?: 0.0

        return Pair(rsrp, rsrq)
    }

    fun getNetworkRating(rsrp: Double): NetworkRating {
        return when {
            rsrp > -90 -> NetworkRating("Excellent", Color.GREEN)
            rsrp in -110.0..-90.0 -> NetworkRating("Fair", Color.parseColor("#FFA500"))
            rsrp < -110 -> NetworkRating("Poor", Color.RED)
            else -> NetworkRating("Unknown", Color.GRAY)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    fun getCellInfo(context: Context): Pair<String, Double> {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        return try {
            val cellLocation = tm.cellLocation
            val cellId = cellLocation?.toString() ?: "Unknown"
            val pci = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val cellInfo = tm.allCellInfo.firstOrNull()
                    when (cellInfo?.cellIdentity) {
                        is android.telephony.CellIdentityLte -> (cellInfo.cellIdentity as android.telephony.CellIdentityLte).pci?.toDouble() ?: 0.0
                        is android.telephony.CellIdentityNr -> (cellInfo.cellIdentity as android.telephony.CellIdentityNr).pci?.toDouble() ?: 0.0
                        else -> 0.0
                    }
                } else {
                    0.0
                }
            } catch (e: Exception) {
                0.0
            }
            Pair(cellId, pci)
        } catch (e: Exception) {
            Pair("Unknown", 0.0)
        }
    }

    @SuppressLint("MissingPermission")
    fun getSignalStrengthLegacy(context: Context): Pair<Double, Double> {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        return try {
            // For older Android versions, use signal strength as approximation
            val signalStrength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                tm.signalStrength
            } else {
                // For pre-P Android versions, use a different approach
                try {
                    val listener = object : PhoneStateListener() {
                        override fun onSignalStrengthsChanged(signalStrength: android.telephony.SignalStrength?) {
                            super.onSignalStrengthsChanged(signalStrength)
                        }
                    }
                    tm.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTH)
                    tm.listen(listener, PhoneStateListener.LISTEN_NONE)
                    null // Will be handled below
                } catch (e: Exception) {
                    null
                }
            }
            
            val rsrp = signalStrength?.level?.let { level ->
                // Convert signal level (0-4) to approximate RSRP
                when (level) {
                    4 -> -70.0
                    3 -> -85.0
                    2 -> -95.0
                    1 -> -105.0
                    0 -> -115.0
                    else -> -120.0
                }
            } ?: -120.0
            
            val rsrq = when {
                rsrp > -90 -> -5.0
                rsrp in -110.0..-90.0 -> -10.0
                else -> -15.0
            }
            
            Pair(rsrp, rsrq)
        } catch (e: Exception) {
            Pair(-120.0, -20.0)
        }
    }
}