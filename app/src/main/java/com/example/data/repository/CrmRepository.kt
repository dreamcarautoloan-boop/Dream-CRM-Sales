package com.example.data.repository

import com.example.data.local.CrmDao
import com.example.data.local.CustomerDao
import com.example.data.model.CallLogEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.DealEntity
import com.example.data.model.MonthlyFinancialEntity
import com.example.data.model.SalesRepTargetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class CrmRepository(
    private val crmDao: CrmDao,
    private val customerDao: CustomerDao? = null
) {
    // --- DEALS ---
    val allDeals: Flow<List<DealEntity>> = crmDao.getAllDeals()
    val allCallLogs: Flow<List<CallLogEntity>> = crmDao.getAllCallLogs()
    val allTargets: Flow<List<SalesRepTargetEntity>> = crmDao.getAllTargets()
    val allFinancials: Flow<List<MonthlyFinancialEntity>> = crmDao.getAllFinancials()

    // --- CUSTOMERS ---
    val allCustomers: Flow<List<CustomerEntity>> = customerDao?.getAllCustomers() ?: emptyFlow()

    fun getCustomerById(id: Long): Flow<CustomerEntity?> =
        customerDao?.getCustomerById(id) ?: emptyFlow()

    suspend fun getCustomerByPhone(phone: String): CustomerEntity? =
        customerDao?.getCustomerByPhone(phone)

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> =
        customerDao?.searchCustomers(query) ?: emptyFlow()

    fun getCustomersByRep(rep: String): Flow<List<CustomerEntity>> =
        customerDao?.getCustomersByRep(rep) ?: emptyFlow()

    fun getCustomersByInterest(status: String): Flow<List<CustomerEntity>> =
        customerDao?.getCustomersByInterest(status) ?: emptyFlow()

    fun getCustomersBySource(source: String): Flow<List<CustomerEntity>> =
        customerDao?.getCustomersBySource(source) ?: emptyFlow()

    suspend fun insertCustomer(customer: CustomerEntity): Long =
        customerDao?.insertCustomer(customer) ?: 0L

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao?.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao?.deleteCustomer(customer)
    }

    suspend fun updateCustomerInterest(id: Long, status: String) {
        customerDao?.updateInterestStatus(id, status)
    }

    suspend fun reassignCustomer(id: Long, newRep: String) {
        customerDao?.reassignCustomer(id, newRep)
    }

    // --- DEALS OPERATIONS ---
    fun getDealById(id: Long): Flow<DealEntity?> = crmDao.getDealById(id)
    suspend fun getDealByPhone(phone: String): DealEntity? = crmDao.getDealByPhone(phone)
    suspend fun searchDealsByPhone(phone: String): List<DealEntity> = crmDao.searchDealsByPhone(phone)

    fun getFinancialsByMonth(month: String): Flow<MonthlyFinancialEntity?> = crmDao.getFinancialsByMonth(month)
    fun getTargetsByMonth(month: String): Flow<List<SalesRepTargetEntity>> = crmDao.getTargetsByMonth(month)

    suspend fun insertDeal(deal: DealEntity): Long = crmDao.insertDeal(deal)
    suspend fun updateDeal(deal: DealEntity) = crmDao.updateDeal(deal)
    suspend fun deleteDeal(deal: DealEntity) = crmDao.deleteDeal(deal)

    suspend fun updateDealStageAndProbability(id: Long, stage: String, probability: Int) =
        crmDao.updateDealStageAndProbability(id, stage, probability)

    suspend fun reassignDeal(id: Long, newRep: String, isRecycled: Boolean = false) =
        crmDao.reassignDeal(id, newRep, isRecycled)

    suspend fun markDealAsLost(id: Long, lostReason: String) =
        crmDao.markDealAsLost(id, lostReason)

    suspend fun updateCommissionStatus(id: Long, isReceived: Boolean, notes: String) =
        crmDao.updateCommissionStatus(id, isReceived, notes)

    // --- ACTIVITY LOGS ---
    suspend fun insertCallLog(log: CallLogEntity): Long = crmDao.insertCallLog(log)
    suspend fun deleteCallLog(log: CallLogEntity) = crmDao.deleteCallLog(log)

    // --- TARGETS ---
    suspend fun insertOrUpdateTarget(target: SalesRepTargetEntity): Long = crmDao.insertOrUpdateTarget(target)
    suspend fun updateTarget(target: SalesRepTargetEntity) = crmDao.updateTarget(target)

    // --- FINANCIALS ---
    suspend fun insertOrUpdateFinancial(financial: MonthlyFinancialEntity): Long =
        crmDao.insertOrUpdateFinancial(financial)
}
