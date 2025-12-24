package com.example.crm.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.lifecycle.LiveData

// Annotation @Dao để Room hiểu rằng interface này chứa các phương thức truy cập database cho Category
@Dao
interface CategoryDao {

    // Chèn một danh mục mới vào database. Nếu đã tồn tại, không làm gì cả.
    @Insert
    suspend fun insertCategory(category: Category)

    // Cập nhật thông tin một danh mục đã có.
    @Update
    suspend fun updateCategory(category: Category)

    // Xóa một danh mục khỏi database.
    @Delete
    suspend fun deleteCategory(category: Category)

    // Truy vấn để lấy tất cả các danh mục, sắp xếp theo tên A-Z.
    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<Category>

    // Truy vấn để chỉ lấy danh sách tên của tất cả các danh mục.
    @Query("SELECT name FROM categories ORDER BY name ASC")
    suspend fun getCategoryNames(): List<String>

    // Bổ sung hàm xóa tất cả danh mục
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    // Kiểm tra xem tên danh mục đã tồn tại chưa
    @Query("SELECT COUNT(*) FROM categories WHERE name = :name")
    suspend fun checkNameExist(name: String): Int

    // Lấy tổng số lượng danh mục, trả về LiveData để tự động cập nhật
    @Query("SELECT COUNT(*) FROM categories")
    fun getTotalCategoryCount(): LiveData<Int>
}
