package com.example.application

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TaskApiService {

    @GET("api/tasks")
    suspend fun getRemoteTasks(): Response<List<TaskEntity>>

    @POST("api/tasks")
    suspend fun syncTask(@Body task: TaskEntity): Response<TaskEntity>

    companion object {
        // Replace with your Node.js backend URL or local IP (10.0.2.2 for emulator)
        private const val BASE_URL = "https://your-api-endpoint.com/"

        fun create(): TaskApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TaskApiService::class.java)
        }
    }
}