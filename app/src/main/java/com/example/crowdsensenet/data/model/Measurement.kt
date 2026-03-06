package com.example.crowdsensenet.data.model

data class Measurement(
    val rsrp: Double,
    val rsrq: Double,
    val pci: Double,
    val cellId: String,
    val networkTechnology: String,
    val latitude: Double,
    val longitude: Double
)