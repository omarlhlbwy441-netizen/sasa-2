package com.example.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SasaApiService {

    @POST("api/v1/gemini/chat")
    suspend fun chatWithBackend(
        @Body request: SasaBackendChatRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<SasaBackendChatResponse>

    @POST("api/v1/files/generate")
    suspend fun generateFile(
        @Body request: FileGenerationRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<FileGenerationResponse>

    @POST("api/v1/github/push")
    suspend fun pushToCloudRepo(
        @Body request: CloudPushRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<CloudPushResponse>

    @POST("api/v1/github/scan")
    suspend fun scanRepo(
        @Body request: CloudPushRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<GitHubRepoScanResult>
}
