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

    @POST("api/Assignment")
    suspend fun createAssignment(@Body request: CreateAssignmentRequest): Response<Assignment>

    @POST("api/Assignment/submit")
    suspend fun submitAnswer(@Body request: SubmissionRequest): Response<SubmissionResponse>

    @GET("api/Teacher/submissions")
    suspend fun getAllSubmissions(
        @Query("studentId") studentId: Int? = null,
        @Query("groupId") groupId: Int? = null
    ): Response<List<SubmissionItem>>

    @GET("api/Teacher/students")
    suspend fun getTeacherStudents(): Response<List<StudentItem>>

    @GET("api/Groups")
    suspend fun getGroups(): Response<List<GroupItem>>

    @GET("api/User/profile")
    suspend fun getProfile(): Response<UserProfile>

    @PUT("api/User/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfile>
}