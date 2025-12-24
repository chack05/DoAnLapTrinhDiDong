package com.example.crm.database // Khai báo package chứa file này

import androidx.room.Dao // Import annotation @Dao để đánh dấu đây là một Data Access Object
import androidx.room.Delete // Import annotation @Delete để định nghĩa hàm xóa
import androidx.room.Insert // Import annotation @Insert để định nghĩa hàm thêm dữ liệu
import androidx.room.Query // Import annotation @Query để định nghĩa các câu truy vấn tùy chỉnh
import androidx.room.Update // Import annotation @Update để định nghĩa hàm cập nhật dữ liệu
import androidx.lifecycle.LiveData // Import lớp LiveData để sử dụng cho kiểu trả về tự động cập nhật

// Annotation @Dao để Room hiểu rằng interface này chứa các phương thức truy cập database
@Dao
interface ProductDao {

    @Insert
    suspend fun insertProduct(product: Product) // Chèn một sản phẩm

    @Update
    suspend fun updateProduct(product: Product) // Cập nhật một sản phẩm

    @Delete
    suspend fun deleteProduct(product: Product) // Xóa một sản phẩm

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<Product> // Lấy tất cả sản phẩm

    // FIX 100%: Đảm bảo phương thức này trả về Product? (có thể null)
    // Đây là điều kiện tiên quyết để code ở màn hình chi tiết có thể kiểm tra và không bị crash
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product? // Lấy sản phẩm theo ID, có thể trả về null

    @Query("SELECT COUNT(*) FROM products WHERE code = :code")
    suspend fun checkCodeExist(code: String): Int

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    suspend fun searchProduct(query: String): List<Product> // Tìm kiếm sản phẩm theo tên hoặc mã

    // Hàm truy vấn sản phẩm kết hợp tìm kiếm theo từ khóa và lọc theo danh mục
    @Query("""
        SELECT * FROM products
        WHERE (name LIKE '%' || :keyword || '%' OR code LIKE '%' || :keyword || '%')
        AND (:category = 'Tất cả' OR category = :category)
    """)
    suspend fun getProductsCombined(keyword: String, category: String): List<Product>

    // Bổ sung hàm xóa tất cả sản phẩm
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    // Lấy tổng số lượng sản phẩm, trả về LiveData để tự động cập nhật
    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductCount(): LiveData<Int>
}