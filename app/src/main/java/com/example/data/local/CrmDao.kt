package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallLogEntity
import com.example.data.model.DealEntity
import com.example.data.model.MonthlyFinancialEntity
import com.example.data.model.SalesRepTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrmDao {
    // --- DEALS ---
    @Query("SELECT * FROM deals ORDER BY createdAt DESC")
    fun getAllDeals(): Flow<List<DealEntity>>

    @Query("SELECT * FROM deals WHERE id = :id")
    fun getDealById(id: Long): Flow<DealEntity?>

    @Query("SELECT * FROM deals WHERE phone = :phone LIMIT 1")
    suspend fun getDealByPhone(phone: String): DealEntity?

    @Query("SELECT * FROM deals WHERE phone LIKE '%' || :phone || '%' LIMIT 5")
    suspend fun searchDealsByPhone(phone: String): List<DealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: DealEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDeals(deals: List<DealEntity>)

    @Update
    suspend fun updateDeal(deal: DealEntity)

    @Delete
    suspend fun deleteDeal(deal: DealEntity)

    @Query("UPDATE deals SET stage = :stage, probability = :probability, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateDealStageAndProbability(id: Long, stage: String, probability: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE deals SET salesRep = :newRep, isLostRecycled = :isRecycled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun reassignDeal(id: Long, newRep: String, isRecycled: Boolean = false, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE deals SET stage = 'LOST', lostReason = :lostReason, probability = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDealAsLost(id: Long, lostReason: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE deals SET isCommissionReceived = :isReceived, receivedNotes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCommissionStatus(id: Long, isReceived: Boolean, notes: String, updatedAt: Long = System.currentTimeMillis())

    // --- CALL / ACTIVITY LOGS ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getCallLogsForClient(clientId: Long): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(log: CallLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCallLogs(logs: List<CallLogEntity>)

    @Delete
    suspend fun deleteCallLog(log: CallLogEntity)

    // --- TARGETS ---
    @Query("SELECT * FROM sales_rep_targets ORDER BY id ASC")
    fun getAllTargets(): Flow<List<SalesRepTargetEntity>>

    @Query("SELECT * FROM sales_rep_targets WHERE month = :month")
    fun getTargetsByMonth(month: String): Flow<List<SalesRepTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTarget(target: SalesRepTargetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTargets(targets: List<SalesRepTargetEntity>)

    @Update
    suspend fun updateTarget(target: SalesRepTargetEntity)

    // --- FINANCIALS ---
    @Query("SELECT * FROM monthly_financials WHERE month = :month LIMIT 1")
    fun getFinancialsByMonth(month: String): Flow<MonthlyFinancialEntity?>

    @Query("SELECT * FROM monthly_financials")
    fun getAllFinancials(): Flow<List<MonthlyFinancialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFinancial(financial: MonthlyFinancialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFinancials(financials: List<MonthlyFinancialEntity>)
}
