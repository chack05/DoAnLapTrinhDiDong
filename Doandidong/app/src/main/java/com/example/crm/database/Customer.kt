package com.example.crm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity cho bảng Customer.
 * File này được ghi đè để đảm bảo trình biên dịch nhận đúng cấu trúc mới,
 * giải quyết lỗi cache của hệ thống build.
 */
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val code: String,       // Mã khách hàng, được tự sinh
    val name: String,       // Tên khách hàng, đồng bộ từ User
    val phone: String,      // SĐT, đồng bộ từ User
    val email: String,      // Email, đồng bộ từ User
    val password: String,   // Mật khẩu, đồng bộ từ User
    val imagePath: String? = null, // Đường dẫn ảnh, có thể null
    val userId: Int = 0     // Khóa ngoại (ảo) liên kết tới bảng User
)