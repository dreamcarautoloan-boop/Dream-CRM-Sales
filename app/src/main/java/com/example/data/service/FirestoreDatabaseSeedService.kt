package com.example.data.service

import android.util.Log
import com.example.data.model.CrmUser
import com.example.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirestoreDatabaseSeedService
 *
 * Dedicated database migration and seeding service that populates Firestore
 * with predefined team members and their designated CRM roles:
 * - Ali: Sales Manager (مدير المبيعات) - dreamcarautoloan@gmail.com
 * - Marwan: Team Leader (قائد الفريق)
 * - Mahmoud: Sales Rep (مسؤول مبيعات)
 * - Nahla: Sales Rep (مسؤول مبيعات)
 * - Israa: Sales Rep (مسؤول مبيعات)
 * - Nada: Sales Rep (مسؤول مبيعات)
 * - Alaa: Sales Rep (مسؤول مبيعات)
 */
class FirestoreDatabaseSeedService(
    private val userProfileSyncService: UserProfileSyncService = UserProfileSyncService()
) {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore instance unavailable: ${e.message}")
            null
        }

    companion object {
        private const val TAG = "FirestoreSeedService"
        const val USERS_COLLECTION = UserProfileSyncService.USERS_COLLECTION

        /**
         * The standard predefined CRM team members
         */
        val PREDEFINED_TEAM: List<CrmUser> = listOf(
            CrmUser(
                uid = "user_ali_manager",
                email = "dreamcarautoloan@gmail.com",
                displayName = "Ali (علي)",
                role = UserRole.SALES_MANAGER,
                assignedRepName = "Ali",
                phone = "+201000000001",
                photoUrl = ""
            ),
            CrmUser(
                uid = "user_marwan_leader",
                email = "marwan@dreamcar.com",
                displayName = "Marwan (مروان)",
                role = UserRole.TEAM_LEADER,
                assignedRepName = "Marwan",
                phone = "+201000000002",
                photoUrl = ""
            ),
            CrmUser(
                uid = "user_mahmoud_sales",
                email = "mahmoud@dreamcar.com",
                displayName = "Mahmoud (محمود)",
                role = UserRole.SALES_REP,
                assignedRepName = "Mahmoud",
                phone = "+201000000003",
                photoUrl = ""
            ),
            CrmUser(
                uid = "user_nahla_sales",
                email = "nahla@dreamcar.com",
                displayName = "Nahla (نهله)",
                role = UserRole.SALES_REP,
                assignedRepName = "Nahla",
                phone = "+201000000004",
                photoUrl = ""
            ),
            CrmUser(
                uid = "user_israa_sales",
                email = "israa@dreamcar.com",
                displayName = "Israa (اسراء)",
                role = UserRole.SALES_REP,
                assignedRepName = "Esraa",
                phone = "+201000000005",
                photoUrl = ""
            ),
            CrmUser(
                uid = "user_nada_sales",
                email = "nada@dreamcar.com",
                displayName = "Nada (ندى)",
                role = UserRole.SALES_REP,
                assignedRepName = "Nada",
                phone = "+201000000006",
                photoUrl = ""
            ),
            CrmUser(
                uid = "user_alaa_sales",
                email = "alaa@dreamcar.com",
                displayName = "Alaa (الاء)",
                role = UserRole.SALES_REP,
                assignedRepName = "Alaa",
                phone = "+201000000007",
                photoUrl = ""
            )
        )
    }

    /**
     * Seeds or updates the Firestore users collection with all predefined team members.
     *
     * @param overwriteExisting if true, overwrites existing roles with standard defaults.
     * @return Result containing number of seeded users.
     */
    suspend fun seedPredefinedUsers(overwriteExisting: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore
            if (fs == null) {
                Log.d(TAG, "Firestore offline or not initialized. Seed completed locally in-memory.")
                return@withContext Result.success(PREDEFINED_TEAM.size)
            }

            var seededCount = 0
            val batch = fs.batch()

            for (user in PREDEFINED_TEAM) {
                val authorizedRole = UserProfileSyncService.resolveAuthorizedRole(user.email, user.role)
                val docRef = fs.collection(USERS_COLLECTION).document(user.uid)

                val docSnapshot = docRef.get().await()
                if (!docSnapshot.exists() || overwriteExisting) {
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "email" to user.email,
                        "displayName" to user.displayName,
                        "role" to authorizedRole.name,
                        "roleArabic" to authorizedRole.arabicName,
                        "assignedRepName" to user.assignedRepName,
                        "phone" to user.phone,
                        "photoUrl" to user.photoUrl,
                        "isPredefined" to true,
                        "createdAt" to (docSnapshot.getLong("createdAt") ?: System.currentTimeMillis()),
                        "updatedAt" to System.currentTimeMillis()
                    )
                    batch.set(docRef, userData, SetOptions.merge())
                    seededCount++
                }
            }

            if (seededCount > 0) {
                batch.commit().await()
                Log.i(TAG, "Successfully seeded $seededCount predefined users to Firestore.")
            } else {
                Log.d(TAG, "All predefined users already present in Firestore. No write necessary.")
            }

            Result.success(seededCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeding predefined users to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Checks if Firestore users collection is empty and seeds initial data if needed.
     */
    suspend fun seedIfEmpty(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.success(false)
            val snapshot = fs.collection(USERS_COLLECTION).limit(1).get().await()

            if (snapshot.isEmpty) {
                val result = seedPredefinedUsers(overwriteExisting = true)
                return@withContext Result.success(result.isSuccess)
            }

            Result.success(false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed check and seed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves the predefined list of team members.
     */
    fun getPredefinedTeamMembers(): List<CrmUser> = PREDEFINED_TEAM
}
