package com.example.crowdsensenet.data.remote

sealed class ConnectionResult {
    data class Success(val message: String = "Connection Active") : ConnectionResult()
    data class Failure(val error: String = "Connection Failed") : ConnectionResult()
}
