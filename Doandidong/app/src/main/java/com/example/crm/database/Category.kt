package com.example.crm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

// Định nghĩa bảng 'categories' trong database
@Entity(tableName = "categories")
data class Category(
    // Khai báo khóa chính (primary key) với thuộc tính tự động tăng (auto-increment)
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Khai báo cột 'name' để lưu tên danh mục, kiểu String
    val name: String
)
