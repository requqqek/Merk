package com.example.merk.data.api

import com.example.merk.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Auth/create-student")
    suspend fun createStudent(@Body request: CreateStudentRequest): Response<Any>

    @GET("api/Assignment")
    suspend fun getAssignments(): Response<List<Assignment>>

    @POST("api/Assignment/submit")
    suspend fun submitAnswer(@Body request: SubmissionRequest): Response<SubmissionResponse>

    @GET("api/Teacher/submissions")
    suspend fun getAllSubmissions(): Response<List<SubmissionItem>>
}