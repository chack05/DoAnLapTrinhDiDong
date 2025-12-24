package com.example.crm.database // Khai báo package chứa file này

import androidx.room.Entity // Import annotation @Entity để đánh dấu đây là một bảng trong database
import androidx.room.PrimaryKey // Import annotation @PrimaryKey để đánh dấu đây là khóa chính

// Annotation @Entity để khai báo đây là một bảng dữ liệu trong Room DB, với tên bảng là 'users'
@Entity(tableName = "users")
data class User(
    // Annotation @PrimaryKey để định nghĩa cột 'id' là khóa chính.
    // autoGenerate = true nghĩa là giá trị của id sẽ được tự động tăng mỗi khi có một User mới được thêm vào.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Khai báo trường id, là một số nguyên và có giá trị mặc định là 0

    val email: String, // Khai báo trường email, kiểu chuỗi, để lưu email đăng nhập
    val password: String, // Khai báo trường password, kiểu chuỗi, để lưu mật khẩu
    val name: String, // Khai báo trường name, kiểu chuỗi, để lưu tên người dùng
    val phone: String // THÊM MỚI
)
