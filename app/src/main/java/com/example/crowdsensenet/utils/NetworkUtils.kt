package com.example.crowdsensenet.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.telephony.CellSignalStrengthLte
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine

object NetworkUtils {

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getNetworkType(context: Context): String {

        val tm = context.getSystemService(Context.TELEPHONY_SERVICE)
                as TelephonyManager

        return when (tm.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
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
}