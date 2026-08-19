package com.example.data.model

data class CrmUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.SALES_MANAGER,
    val assignedRepName: String = "Nada",
    val photoUrl: String = "",
    val phone: String = "",
    val lastLoginAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
