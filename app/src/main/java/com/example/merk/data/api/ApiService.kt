package com.example.merk.data.api

import com.example.merk.data.models.*
import com.example.merk.data.models.UpdateProfileRequest
import com.google.gson.annotations.SerializedName
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

    @GET("api/Assignment/student/{userId}")
    suspend fun getAssignmentsForStudent(@Path("userId") userId: Int): Response<List<AssignmentStatus>>

    @POST("api/Assignment/submit")
    suspend fun submitAnswer(@Body request: SubmissionRequest): Response<SubmissionResponse>

    @GET("api/Assignment/teacher/{teacherId}")
    suspend fun getTeacherAssignments(@Path("teacherId") teacherId: Int): Response<List<Assignment>>

    @PUT("api/Assignment/{id}")
    suspend fun updateAssignment(
        @Path("id") id: Int,
        @Body request: CreateAssignmentRequest
    ): Response<Assignment>

    @DELETE("api/Assignment/{id}")
    suspend fun deleteAssignment(@Path("id") id: Int): Response<Any>

    @GET("api/Assignment/stats/{userId}")
    suspend fun getStudentStats(@Path("userId") userId: Int): Response<StudentStats>

    @GET("api/Teacher/submissions")
    suspend fun getAllSubmissions(
        @Query("studentId") studentId: Int? = null,
        @Query("groupId") groupId: Int? = null,
        @Query("teacherId") teacherId: Int? = null
    ): Response<List<SubmissionItem>>

    @GET("api/Teacher/students")
    suspend fun getTeacherStudents(@Query("teacherId") teacherId: Int? = null): Response<List<StudentItem>>

    @GET("api/Groups")
    suspend fun getGroups(): Response<List<GroupItem>>

    @DELETE("api/Teacher/students/{studentId}")
    suspend fun deleteStudent(@Path("studentId") studentId: Int): Response<Any>

    // НОВОЕ: создание группы
    @POST("api/Groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<GroupItem>

    @GET("api/User/profile")
    suspend fun getProfile(@Query("userId") userId: Int): Response<UserProfile>

    @PUT("api/User/profile")
    suspend fun updateProfile(
        @Query("userId") userId: Int,
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>

    @PUT("api/User/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Any>

    @GET("api/Invitation/teacher/{teacherId}")
    suspend fun getInviteCodes(@Path("teacherId") teacherId: Int): Response<List<InviteCodeItem>>

    @POST("api/Invitation")
    suspend fun createInviteCode(@Body request: CreateInviteCodeRequest): Response<InviteCodeItem>
}

data class StudentItem(
    @SerializedName("userId") val userId: Int,
    @SerializedName("login") val login: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("groupId") val groupId: Int?,
    @SerializedName("groupName") val groupName: String?
)

data class GroupItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class CreateGroupRequest(
    @SerializedName("name") val name: String
)

data class UserProfile(
    @SerializedName("userId") val userId: Int,
    @SerializedName("login") val login: String,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String,
    @SerializedName("groupId") val groupId: Int?,
    @SerializedName("groupName") val groupName: String?
)

data class ChangePasswordRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("oldPassword") val oldPassword: String,
    @SerializedName("newPassword") val newPassword: String
)

data class InviteCodeItem(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: String,
    @SerializedName("groupId") val groupId: Int?,
    @SerializedName("groupName") val groupName: String?,
    @SerializedName("isActive") val isActive: Boolean
)

data class CreateInviteCodeRequest(
    @SerializedName("teacherId") val teacherId: Int,
    @SerializedName("groupId") val groupId: Int? = null
)