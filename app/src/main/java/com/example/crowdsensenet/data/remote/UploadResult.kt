package com.example.crowdsensenet.data.remote

sealed class UploadResult {
    data class Success(val count: Int) : UploadResult()
    data class Failure(val error: String = "Upload failed") : UploadResult()
}
