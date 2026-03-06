package com.example.crowdsensenet.data.remote

import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadMeasurement(measurement: MeasurementEntity) {

        val data = hashMapOf(
            "deviceId" to measurement.deviceId,
            "timestamp" to measurement.timestamp,
            "rsrp" to measurement.rsrp,
            "rsrq" to measurement.rsrq,
            "pci" to measurement.pci,
            "cellId" to measurement.cellId,
            "networkTechnology" to measurement.networkTechnology,
            "latitude" to measurement.latitude,
            "longitude" to measurement.longitude,
            "uploadStatus" to "uploaded",
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("measurements")
            .add(data)
            .await()
    }
}