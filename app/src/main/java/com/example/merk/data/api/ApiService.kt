package com.example.merk.data.api

import com.example.merk.data.models.*
import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

interface ApiService {

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Auth/create-student")
    suspend fun createStudent(@Body request: CreateStudentRequest): Response<Any>

    @GET("api/Assignment")
    suspend fun getAssignments(): Response<List<Assignment>>

    @POST("api/Assignment")
    suspend fun createAssignment(@Body request: CreateAssignmentRequest): Response<Assignment>

    @GET("api/Assignment/student/{userId}")
    suspend fun getAssignmentsForStudent(@Path("userId") userId: Int): Response<List<AssignmentStatus>>

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
    suspend fun getProfile(@Query("userId") userId: Int): Response<UserProfile>

    @PUT("api/User/profile")
    suspend fun updateProfile(@Query("userId") userId: Int, @Body request: UpdateProfileRequest): Response<UserProfile>

}

data class StudentItem(
    @SerializedName("userId") val userId: Int,
    @SerializedName("login") val login: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("groupId") val groupId: Int?,
    @SerializedName("groupName") val groupName: String?
)

// Группа
data class GroupItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

// Профиль пользователя
data class UserProfile(
    @SerializedName("userId") val userId: Int,
    @SerializedName("login") val login: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("groupId") val groupId: Int?,
    @SerializedName("groupName") val groupName: String?
)

// Запрос на обновление профиля
data class UpdateProfileRequest(
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("password") val password: String?
)