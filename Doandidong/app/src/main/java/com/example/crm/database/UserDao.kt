package com.example.crm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    // --- ĐÂY LÀ CHỖ SỬA LỖI ---
    // Thêm đoạn "AND password = :pass" để sử dụng tham số pass
    @Query("SELECT * FROM users WHERE email = :email AND password = :pass LIMIT 1")
    suspend fun checkLogin(email: String, pass: String): User?

    // Các hàm khác giữ nguyên
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun checkEmailExist(email: String): Int

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}