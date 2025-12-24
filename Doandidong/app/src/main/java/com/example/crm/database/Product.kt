package com.example.crm.database // Khai báo file này thuộc package database

import androidx.room.ColumnInfo // Import annotation @ColumnInfo để tùy chỉnh tên cột
import androidx.room.Entity // Import annotation @Entity để đánh dấu đây là một bảng
import androidx.room.PrimaryKey // Import annotation @PrimaryKey để đánh dấu khóa chính

// Annotation @Entity để khai báo đây là một bảng dữ liệu trong Room DB, với tên bảng là 'products'
@Entity(tableName = "products")
data class Product(
    // Annotation @PrimaryKey để định nghĩa cột 'id' là khóa chính.
    // autoGenerate = true nghĩa là giá trị của id sẽ được tự động tăng.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Khai báo trường id, là một số nguyên, giá trị mặc định là 0

    // Khai báo trường 'code' để lưu mã sản phẩm duy nhất (VD: SP001)
    val code: String,

    // Khai báo trường 'name' để lưu tên sản phẩm
    val name: String,

    // Khai báo trường 'unit' để lưu đơn vị tính của sản phẩm (VD: Cái, Hộp, Ly)
    val unit: String,

    // Khai báo trường 'price' để lưu giá bán của sản phẩm
    val price: Double,

    // Khai báo trường 'category' để lưu danh mục sản phẩm (VD: Đồ ăn, Công nghệ)
    val category: String,

    // Khai báo trường 'description' để lưu mô tả chi tiết cho sản phẩm
    val description: String,

    // Khai báo trường 'imageUri' để lưu đường dẫn (dưới dạng String) của ảnh sản phẩm trong bộ nhớ điện thoại
    @ColumnInfo(name = "image_uri")
    val imageUri: String? // Cho phép giá trị null nếu sản phẩm không có ảnh
)