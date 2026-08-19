package com.example.data.service

import android.util.Log
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.InstallmentPartner
import com.example.data.model.InterestLevel
import com.example.data.model.LeadSource
import com.example.data.model.QualificationStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * FirestoreLeadService
 *
 * Manages incoming leads from Cloud Firestore ("leads" collection).
 * Supports real-time listening, filtering, status updates, and seeding initial Meta/Ad leads.
 */
class FirestoreLeadService {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not available: ${e.message}")
            null
        }

    companion object {
        private const val TAG = "FirestoreLeadService"
        const val LEADS_COLLECTION = "leads"

        val INITIAL_INCOMING_LEADS: List<DealEntity> = listOf(
            DealEntity(
                id = 101,
                clientName = "أحمد محمد محمود (Ahmed Mahmoud)",
                phone = "01012345678",
                salesRep = "Mahmoud",
                stage = DealStage.PROSPECTING.name,
                amount = 950000.0,
                probability = 15,
                expectedCloseDate = "2026-08-25",
                carModel = "Hyundai Elantra CN7 2024",
                carType = "NEW",
                downPayment = 285000.0,
                loanAmount = 665000.0,
                installmentPartner = InstallmentPartner.DRIVE.displayName,
                leadSource = LeadSource.META_ADS.name,
                qualificationStatus = QualificationStatus.PENDING.name,
                interestLevel = InterestLevel.HOT.name,
                followUpDate = "Today 2:00 PM (متابعة أولية)",
                notes = "سجل عبر إعلان فيسبوك، مهتم بمقدم 30% وتقسيط 5 سنوات مع شركة درايف",
                date = "2026-08-18"
            ),
            DealEntity(
                id = 102,
                clientName = "سارة عبد الرحمن (Sara Abdelrahman)",
                phone = "01123456789",
                salesRep = "Nahla",
                stage = DealStage.QUALIFICATION.name,
                amount = 1350000.0,
                probability = 35,
                expectedCloseDate = "2026-08-28",
                carModel = "Kia Sportage 2024",
                carType = "NEW",
                downPayment = 400000.0,
                loanAmount = 950000.0,
                installmentPartner = InstallmentPartner.CONTACT.displayName,
                leadSource = LeadSource.META_ADS.name,
                qualificationStatus = QualificationStatus.QUALIFIED.name,
                interestLevel = InterestLevel.HOT.name,
                followUpDate = "Tomorrow 11:00 AM (إرسال بيان المرتب)",
                notes = "عميل فيسبوك، تم تأكيد العمل وجاري تجهيز كشف الحساب البنكي لشركة كونتاكت",
                date = "2026-08-17"
            ),
            DealEntity(
                id = 103,
                clientName = "كريم سامي إبراهيم (Karim Sami)",
                phone = "01234567890",
                salesRep = "Esraa",
                stage = DealStage.PROPOSAL.name,
                amount = 820000.0,
                probability = 55,
                expectedCloseDate = "2026-08-30",
                carModel = "Nissan Sentra 2024",
                carType = "NEW",
                downPayment = 200000.0,
                loanAmount = 620000.0,
                installmentPartner = InstallmentPartner.AMAN.displayName,
                leadSource = LeadSource.REFERRAL.name,
                qualificationStatus = QualificationStatus.QUALIFIED.name,
                interestLevel = InterestLevel.WARM.name,
                followUpDate = "Today 5:00 PM (مراجعة الأوراق)",
                notes = "ترشيح من عميل سابق، تم استلام صورة البطاقة وإرسالها للاستعلام في أمان",
                date = "2026-08-16"
            ),
            DealEntity(
                id = 104,
                clientName = "مصطفى حسن كمال (Mostafa Hassan)",
                phone = "01098765432",
                salesRep = "Nada",
                stage = DealStage.VIEWING.name,
                amount = 1750000.0,
                probability = 75,
                expectedCloseDate = "2026-09-02",
                carModel = "Tiggo 8 Pro 2024",
                carType = "NEW",
                downPayment = 500000.0,
                loanAmount = 1250000.0,
                installmentPartner = InstallmentPartner.ONE_FINANCE.displayName,
                leadSource = LeadSource.WALK_IN.name,
                qualificationStatus = QualificationStatus.QUALIFIED.name,
                interestLevel = InterestLevel.HOT.name,
                followUpDate = "2026-08-22 (معاينة في المعرض)",
                notes = "حضر للمعرض، أعجب بالسيارة ومستعد لحجز المعاينة بعد موافقة وان فاينانس",
                date = "2026-08-15"
            ),
            DealEntity(
                id = 105,
                clientName = "خالد عمر فاروق (Khaled Omar)",
                phone = "01511223344",
                salesRep = "Alaa",
                stage = DealStage.NEGOTIATION.name,
                amount = 1100000.0,
                probability = 90,
                expectedCloseDate = "2026-08-24",
                carModel = "MG 6 2024",
                carType = "NEW",
                downPayment = 330000.0,
                loanAmount = 770000.0,
                installmentPartner = InstallmentPartner.BEDAYA.displayName,
                leadSource = LeadSource.META_ADS.name,
                qualificationStatus = QualificationStatus.QUALIFIED.name,
                interestLevel = InterestLevel.HOT.name,
                followUpDate = "Today (توقيع الشيكات والعقد)",
                notes = "تم صدور الموافقة الائتمانية من شركة بداية، متبقي دفع المقدم وتوقيع العقود",
                date = "2026-08-14"
            ),
            DealEntity(
                id = 106,
                clientName = "طارق يوسف السعيد (Tarek Youssef)",
                phone = "01033445566",
                salesRep = "Marwan",
                stage = DealStage.PROSPECTING.name,
                amount = 680000.0,
                probability = 20,
                expectedCloseDate = "2026-09-05",
                carModel = "Chery Arrizo 5 2024",
                carType = "NEW",
                downPayment = 150000.0,
                loanAmount = 530000.0,
                installmentPartner = InstallmentPartner.DRIVE.displayName,
                leadSource = LeadSource.META_ADS.name,
                qualificationStatus = QualificationStatus.PENDING.name,
                interestLevel = InterestLevel.WARM.name,
                followUpDate = "Tomorrow (اتصال أول)",
                notes = "ليد جديد من حملة دريم كار فيسبوك، مطلوب التواصل لشرح أنظمة التقسيط",
                date = "2026-08-18"
            ),
            DealEntity(
                id = 107,
                clientName = "ياسمين عادل إبراهيم (Yasmine Adel)",
                phone = "01188990011",
                salesRep = "Mahmoud",
                stage = DealStage.LOST.name,
                amount = 1200000.0,
                probability = 0,
                expectedCloseDate = "2026-08-10",
                carModel = "Toyota Corolla 2024",
                carType = "NEW",
                downPayment = 300000.0,
                loanAmount = 900000.0,
                installmentPartner = InstallmentPartner.DRIVE.displayName,
                leadSource = LeadSource.COLD_CALL.name,
                qualificationStatus = QualificationStatus.UNQUALIFIED.name,
                interestLevel = InterestLevel.COLD.name,
                lostReason = "i-Score Score Issue (مشكلة في الآي سكور)",
                notes = "تم الرفض بسبب التقرير الائتماني، تم حفظ البيانات لإعادة التدوير لاحقاً",
                date = "2026-08-10"
            )
        )
    }

    /**
     * Observes real-time leads from Cloud Firestore.
     */
    fun observeFirestoreLeads(): Flow<List<DealEntity>> {
        val fs = firestore ?: return flowOf(INITIAL_INCOMING_LEADS)

        return callbackFlow {
            val listener = fs.collection(LEADS_COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null || snapshot.isEmpty) {
                        trySend(INITIAL_INCOMING_LEADS)
                        return@addSnapshotListener
                    }

                    val leads = snapshot.documents.mapNotNull { doc ->
                        try {
                            DealEntity(
                                id = doc.getLong("id") ?: (doc.id.hashCode().toLong().let { if (it < 0) -it else it }),
                                clientName = doc.getString("clientName") ?: doc.getString("name") ?: "عميل جديد",
                                phone = doc.getString("phone") ?: "",
                                salesRep = doc.getString("salesRep") ?: "Nada",
                                stage = doc.getString("stage") ?: doc.getString("status") ?: DealStage.PROSPECTING.name,
                                amount = doc.getDouble("amount") ?: 0.0,
                                probability = doc.getLong("probability")?.toInt() ?: 10,
                                expectedCloseDate = doc.getString("expectedCloseDate") ?: "",
                                carModel = doc.getString("carModel") ?: "",
                                carType = doc.getString("carType") ?: "NEW",
                                downPayment = doc.getDouble("downPayment") ?: 0.0,
                                loanAmount = doc.getDouble("loanAmount") ?: 0.0,
                                installmentPartner = doc.getString("installmentPartner") ?: InstallmentPartner.DRIVE.displayName,
                                installmentStatus = doc.getString("installmentStatus") ?: "",
                                leadSource = doc.getString("leadSource") ?: LeadSource.META_ADS.name,
                                qualificationStatus = doc.getString("qualificationStatus") ?: QualificationStatus.PENDING.name,
                                interestLevel = doc.getString("interestLevel") ?: InterestLevel.HOT.name,
                                followUpDate = doc.getString("followUpDate") ?: "",
                                lostReason = doc.getString("lostReason") ?: "",
                                commissionRate = doc.getDouble("commissionRate") ?: 0.025,
                                commissionAmount = doc.getDouble("commissionAmount") ?: 0.0,
                                isCommissionReceived = doc.getBoolean("isCommissionReceived") ?: false,
                                receivedNotes = doc.getString("receivedNotes") ?: "",
                                date = doc.getString("date") ?: "",
                                notes = doc.getString("notes") ?: "",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing lead doc ${doc.id}: ${e.message}")
                            null
                        }
                    }
                    trySend(if (leads.isNotEmpty()) leads else INITIAL_INCOMING_LEADS)
                }

            awaitClose { listener.remove() }
        }
    }

    /**
     * Seeds initial incoming leads to Firestore if collection is empty.
     */
    suspend fun seedInitialLeadsIfEmpty(): Result<Int> {
        return try {
            val fs = firestore ?: return Result.success(INITIAL_INCOMING_LEADS.size)
            val existing = fs.collection(LEADS_COLLECTION).limit(1).get().await()

            if (existing.isEmpty) {
                val batch = fs.batch()
                for (lead in INITIAL_INCOMING_LEADS) {
                    val docRef = fs.collection(LEADS_COLLECTION).document("lead_${lead.id}")
                    val data = hashMapOf(
                        "id" to lead.id,
                        "clientName" to lead.clientName,
                        "phone" to lead.phone,
                        "salesRep" to lead.salesRep,
                        "stage" to lead.stage,
                        "amount" to lead.amount,
                        "probability" to lead.probability,
                        "expectedCloseDate" to lead.expectedCloseDate,
                        "carModel" to lead.carModel,
                        "carType" to lead.carType,
                        "downPayment" to lead.downPayment,
                        "loanAmount" to lead.loanAmount,
                        "installmentPartner" to lead.installmentPartner,
                        "leadSource" to lead.leadSource,
                        "qualificationStatus" to lead.qualificationStatus,
                        "interestLevel" to lead.interestLevel,
                        "followUpDate" to lead.followUpDate,
                        "lostReason" to lead.lostReason,
                        "notes" to lead.notes,
                        "date" to lead.date,
                        "createdAt" to lead.createdAt,
                        "updatedAt" to lead.updatedAt
                    )
                    batch.set(docRef, data, SetOptions.merge())
                }
                batch.commit().await()
                Log.i(TAG, "Seeded ${INITIAL_INCOMING_LEADS.size} leads into Firestore.")
                Result.success(INITIAL_INCOMING_LEADS.size)
            } else {
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed seeding leads to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes or updates a lead document in Firestore.
     */
    suspend fun saveLeadToFirestore(lead: DealEntity): Result<Unit> {
        return try {
            val fs = firestore ?: return Result.success(Unit)
            val docId = if (lead.id > 0) "lead_${lead.id}" else "lead_${System.currentTimeMillis()}"
            val data = hashMapOf(
                "id" to lead.id,
                "clientName" to lead.clientName,
                "phone" to lead.phone,
                "salesRep" to lead.salesRep,
                "stage" to lead.stage,
                "amount" to lead.amount,
                "probability" to lead.probability,
                "expectedCloseDate" to lead.expectedCloseDate,
                "carModel" to lead.carModel,
                "carType" to lead.carType,
                "downPayment" to lead.downPayment,
                "loanAmount" to lead.loanAmount,
                "installmentPartner" to lead.installmentPartner,
                "leadSource" to lead.leadSource,
                "qualificationStatus" to lead.qualificationStatus,
                "interestLevel" to lead.interestLevel,
                "followUpDate" to lead.followUpDate,
                "lostReason" to lead.lostReason,
                "notes" to lead.notes,
                "date" to lead.date,
                "createdAt" to lead.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection(LEADS_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving lead to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates the status or installmentStatus of a lead in Firestore.
     */
    suspend fun updateLeadStatus(leadId: String, status: String): Result<Unit> {
        return try {
            val fs = firestore ?: return Result.success(Unit)
            val docId = if (leadId.startsWith("lead_")) leadId else "lead_$leadId"
            val updates = hashMapOf<String, Any>(
                "installmentStatus" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection(LEADS_COLLECTION).document(docId).set(updates, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating lead status in Firestore: ${e.message}")
            Result.failure(e)
        }
    }
}
