package com.example.application

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class UserAuthDto(
    val username: String,
    val passwordHash: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)

interface TaskApiService {

    @POST("api/auth/register")
    suspend fun registerUser(@Body user: UserAuthDto): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun loginUser(@Body user: UserAuthDto): Response<AuthResponse>

    @POST("api/tasks/sync")
    suspend fun syncTask(@Body task: TaskEntity): Response<Unit>

    companion object {
        // TODO: Replace with your actual deployed Render/Railway backend URL
        private const val BASE_URL = "https://your-backend-service.onrender.com/"

        fun create(): TaskApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TaskApiService::class.java)
        }
    }
}