package com.example.crowdsensenet.data.model

data class Measurement(
    val deviceId: String,
    val timestamp: Long,
    val rsrp: Double,
    val rsrq: Double,
    val pci: Double,
    val cellId: String,
    val networkTechnology: String,
    val latitude: Double,
    val longitude: Double,
    val isUploaded: Boolean = false
)