package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * CustomerDao
 *
 * Data Access Object for managing Customer records in the Room database,
 * offering reactive queries by lead source, interest level, sales representative,
 * and text search across names and contact details.
 */
@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): CustomerEntity?

    @Query("""
        SELECT * FROM customers 
        WHERE name LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%' 
           OR interestedCarModel LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE assignedSalesRep = :repName ORDER BY updatedAt DESC")
    fun getCustomersByRep(repName: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE interestStatus = :interestStatus ORDER BY updatedAt DESC")
    fun getCustomersByInterest(interestStatus: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE leadSource = :leadSource ORDER BY updatedAt DESC")
    fun getCustomersBySource(leadSource: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE qualificationStatus = :qualificationStatus ORDER BY updatedAt DESC")
    fun getCustomersByQualification(qualificationStatus: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("UPDATE customers SET interestStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateInterestStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET qualificationStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQualificationStatus(id: Long, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET assignedSalesRep = :newRep, updatedAt = :updatedAt WHERE id = :id")
    suspend fun reassignCustomer(id: Long, newRep: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE customers SET nextFollowUpDate = :followUpDate, lastContactDate = :contactDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFollowUp(id: Long, followUpDate: String, contactDate: String, updatedAt: Long = System.currentTimeMillis())
}
