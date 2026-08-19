package com.example.data.model

data class UserPermissions(
    val canViewAllDeals: Boolean = true,
    val canReassignDeals: Boolean = true,
    val canViewFinancials: Boolean = true,
    val canEditTargetsAndSalaries: Boolean = true,
    val canRecycleLostDeals: Boolean = true,
    val canAccessMetaIntake: Boolean = true,
    val canToggleCommissions: Boolean = true,
    val canManageTeam: Boolean = true
) {
    companion object {
        fun fromRole(role: UserRole): UserPermissions {
            return when (role) {
                UserRole.SALES_MANAGER -> UserPermissions(
                    canViewAllDeals = true,
                    canReassignDeals = true,
                    canViewFinancials = true,
                    canEditTargetsAndSalaries = true,
                    canRecycleLostDeals = true,
                    canAccessMetaIntake = true,
                    canToggleCommissions = true,
                    canManageTeam = true
                )

                UserRole.TEAM_LEADER -> UserPermissions(
                    canViewAllDeals = true,
                    canReassignDeals = true,
                    canViewFinancials = false, // Manager confidential P&L
                    canEditTargetsAndSalaries = false,
                    canRecycleLostDeals = true,
                    canAccessMetaIntake = true,
                    canToggleCommissions = true,
                    canManageTeam = true
                )

                UserRole.SALES_REP -> UserPermissions(
                    canViewAllDeals = false, // Restricted to own assigned clients
                    canReassignDeals = false,
                    canViewFinancials = false,
                    canEditTargetsAndSalaries = false,
                    canRecycleLostDeals = false,
                    canAccessMetaIntake = true,
                    canToggleCommissions = false,
                    canManageTeam = false
                )

                UserRole.MODERATOR -> UserPermissions(
                    canViewAllDeals = true,
                    canReassignDeals = false,
                    canViewFinancials = false,
                    canEditTargetsAndSalaries = false,
                    canRecycleLostDeals = false,
                    canAccessMetaIntake = true, // Primary focus: Meta Ads lead entry
                    canToggleCommissions = false,
                    canManageTeam = false
                )
            }
        }
    }
}
