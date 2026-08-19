package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_financials")
data class MonthlyFinancialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val month: String = "May",
    val commissionRevenueRate: Double = 0.025, // 2.5% avg deal fee
    val totalSalaries: Double = 20_000.0,
    val totalMarketing: Double = 30_000.0,
    val totalRent: Double = 0.0,
    val otherExpenses: Double = 0.0
)
