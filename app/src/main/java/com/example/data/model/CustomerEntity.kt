package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * CustomerEntity
 *
 * Represents a customer lead or buyer in the Dream Auto Loan CRM system,
 * containing comprehensive contact information, lead source, interest status,
 * vehicle preferences, and financing criteria as specified in the PRD.
 */
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["assignedSalesRep"]),
        Index(value = ["interestStatus"]),
        Index(value = ["leadSource"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val secondaryPhone: String = "",
    val email: String = "",
    val city: String = "القاهرة",
    val address: String = "",
    val leadSource: String = LeadSource.META_ADS.name,
    val interestStatus: String = InterestLevel.HOT.name, // HOT, WARM, COLD
    val qualificationStatus: String = QualificationStatus.QUALIFIED.name, // PENDING, QUALIFIED, UNQUALIFIED
    val interestedCarModel: String = "",
    val carCondition: String = "NEW", // NEW, USED
    val budget: Double = 0.0,
    val downPaymentAvailable: Double = 0.0,
    val preferredInstallmentPartner: String = InstallmentPartner.DRIVE.displayName,
    val assignedSalesRep: String = "Nada",
    val jobTitle: String = "",
    val employer: String = "",
    val monthlyIncome: Double = 0.0,
    val notes: String = "",
    val lastContactDate: String = "",
    val nextFollowUpDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
