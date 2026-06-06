package com.example.merk.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("login") val login: String,
    @SerializedName("role") val role: String
)

data class CreateStudentRequest(
    @SerializedName("login") val login: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null
)

data class Assignment(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String = "",
    @SerializedName("correctAnswer") val correctAnswer: String = ""
)

data class SubmissionRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("assignmentId") val assignmentId: Int,
    @SerializedName("studentAnswer") val studentAnswer: String
)

data class SubmissionResponse(
    @SerializedName("grade") val grade: Int,
    @SerializedName("comment") val comment: String
)

data class SubmissionItem(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("assignmentId") val assignmentId: Int,
    @SerializedName("studentAnswer") val studentAnswer: String,
    @SerializedName("grade") val grade: Int?,
    @SerializedName("comment") val comment: String?,
    @SerializedName("studentLogin") val studentLogin: String? = null,
    @SerializedName("assignmentTitle") val assignmentTitle: String? = null,
    @SerializedName("submittedAt") val submittedAt: String? = null
)