package com.example.crowdsensenet.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine

object LocationUtils {

    suspend fun getLocation(context: Context): Location? {

        val fused =
            LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) { cont ->
            fused.lastLocation
                .addOnSuccessListener { cont.resume(it, null) }
                .addOnFailureListener { cont.resume(null, null) }
        }
    }
}
