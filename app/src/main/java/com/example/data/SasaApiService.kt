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

    @POST("api/v1/media/generate")
    suspend fun generateMedia(
        @Body request: MediaGenerationRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<MediaGenerationResponse>

    @POST("api/v1/media/process")
    suspend fun processMedia(
        @Body request: MediaProcessRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<MediaProcessResponse>

    @POST("api/v1/code/fix")
    suspend fun fixCode(
        @Body request: CodeAutoFixRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<CodeAutoFixResponse>

    @POST("api/v1/repo/fix")
    suspend fun scanAndFixRepo(
        @Body request: RepoScanFixRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<RepoScanFixResponse>

    @POST("api/v1/environment/evolve")
    suspend fun evolveEnvironment(
        @Body request: EnvironmentEvolutionRequest,
        @Header("x-api-key") serverApiKey: String? = null
    ): Response<EnvironmentEvolutionResponse>
}

