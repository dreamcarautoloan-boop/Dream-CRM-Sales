package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DealStage(val displayName: String, val arabicName: String, val defaultProbability: Int) {
    PROSPECTING("Prospecting", "ليد جديد", 10),
    QUALIFICATION("Qualification", "تأهيل العميل", 30),
    PROPOSAL("Installment Papers", "أوراق التقسيط", 50),
    VIEWING("Viewing / Offer", "معاينة / عرض سعر", 70),
    NEGOTIATION("Negotiation / Signing", "مفاوضات وتوقيع", 90),
    DONE("Closed Won", "إتمام البيع والترخيص", 100),
    LOST("Closed Lost", "فرصة ضائعة", 0)
}

enum class LeadSource(val displayName: String, val arabicName: String) {
    META_ADS("Facebook / Meta Ads", "إعلانات فيسبوك"),
    COLD_CALL("Outbound Call", "مكالمة خارجية"),
    REFERRAL("Referral", "ترشيح من عميل"),
    RETURNING("Returning Client", "عميل قديم"),
    WALK_IN("Showroom Walk-in", "زيارة المعرض")
}

enum class InstallmentPartner(val displayName: String) {
    DRIVE("درايف - Drive"),
    CONTACT("كونتاكت - Contact"),
    AMAN("أمان - Aman"),
    ONE_FINANCE("وان فاينانس - One Finance"),
    BEDAYA("بداية - Bedaya"),
    BANK("بنك - Bank"),
    CASH("كاش - Cash")
}

enum class InstallmentStatus(val displayName: String, val arabicName: String) {
    PENDING_PAPERS("Pending Papers", "انتظار الأوراق"),
    SUBMITTED("Submitted to Partner", "تم تقديم الطلب"),
    APPROVED("Approved", "موافقة معتمدة"),
    PENDING_CONDITIONS("Pending Conditions", "معلق (أوراق ناقصة)"),
    REJECTED("Rejected", "مرفوض"),
    CLIENT_CANCELLED("Client Cancelled", "رفض العميل")
}

enum class QualificationStatus(val displayName: String, val arabicName: String) {
    QUALIFIED("Qualified", "مؤهل"),
    UNQUALIFIED("Unqualified", "غير مؤهل"),
    PENDING("Pending Review", "قيد المراجعة")
}

enum class InterestLevel(val displayName: String, val arabicName: String) {
    HOT("Hot / Interested", "مهتم جداً"),
    WARM("Warm / Considering", "بيفكر"),
    COLD("Cold / Not Interested", "غير مهتم")
}

enum class UserRole(val displayName: String, val arabicName: String) {
    SALES_MANAGER("Sales Manager", "مدير المبيعات"),
    TEAM_LEADER("Team Leader", "تيم ليدر"),
    SALES_REP("Sales Representative", "السيلز"),
    MODERATOR("Moderator (Meta Intake)", "الموديريتور")
}

@Entity(tableName = "deals")
data class DealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientName: String,
    val phone: String,
    val salesRep: String,
    val stage: String = DealStage.PROSPECTING.name,
    val amount: Double = 0.0, // Deal Value / Car Price
    val probability: Int = 10, // Probability % (0-100)
    val expectedCloseDate: String = "", // Expected Close Date (e.g. 2026-09-15)
    val carModel: String = "", // Car Model (e.g. Hyundai Elantra 2024)
    val carType: String = "NEW", // "NEW" or "USED"
    val downPayment: Double = 0.0, // Down payment
    val loanAmount: Double = 0.0, // Financing / Loan amount
    val installmentPartner: String = InstallmentPartner.DRIVE.displayName,
    val installmentStatus: String = InstallmentStatus.PENDING_PAPERS.name,
    val leadSource: String = LeadSource.META_ADS.name,
    val qualificationStatus: String = QualificationStatus.QUALIFIED.name,
    val interestLevel: String = InterestLevel.HOT.name,
    val followUpDate: String = "", // Next follow up datetime
    val lostReason: String = "", // Reason if lost
    val isLostRecycled: Boolean = false, // Was recycled to another rep
    val commissionRate: Double = 0.025, // default 2.5%
    val commissionAmount: Double = 0.0,
    val isCommissionReceived: Boolean = false,
    val receivedNotes: String = "",
    val date: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
