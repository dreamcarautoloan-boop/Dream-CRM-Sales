package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_rep_targets")
data class SalesRepTargetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val month: String = "May",
    val worstCaseTarget: Double = 3_000_000.0,
    val baseTarget: Double = 5_000_000.0,
    val bestCaseTarget: Double = 7_000_000.0,
    val worstCaseRate: Double = 0.002, // 0.2%
    val baseRate: Double = 0.005,      // 0.5%
    val bestCaseRate: Double = 0.007,  // 0.7%
    val salary: Double = 5_000.0,
    val mktgAllocation: Double = 7_421.0,
    val rentAllocation: Double = 0.0,
    val callsCount: Int = 0
)
