package com.example.crm.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.lifecycle.LiveData

/**
 * DAO cho Customer, được viết lại hoàn toàn để sửa lỗi insert thầm lặng.
 * Chỉ bao gồm các hàm cần thiết để đảm bảo sự ổn định.
 */
@Dao
interface CustomerDao {

    @Insert
    suspend fun insert(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY id DESC")
    suspend fun getAllCustomers(): List<Customer>

    @Query("SELECT * FROM customers WHERE id = :customerId LIMIT 1")
    suspend fun getCustomerById(customerId: Int): Customer?

    @Query("SELECT COUNT(*) FROM customers WHERE code = :code")
    suspend fun checkCodeExist(code: String): Int

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    suspend fun searchCustomer(query: String): List<Customer>

    @Query("SELECT * FROM customers WHERE userId = :userId LIMIT 1")
    suspend fun getCustomerByUserId(userId: Int): Customer?
    
    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()

    // Lấy tổng số lượng khách hàng, trả về LiveData để tự động cập nhật
    @Query("SELECT COUNT(*) FROM customers")
    fun getTotalCustomerCount(): LiveData<Int>
}
