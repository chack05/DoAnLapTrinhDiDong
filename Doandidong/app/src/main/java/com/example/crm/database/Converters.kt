package com.example.crm.database // Khai báo package chứa file này

import androidx.room.TypeConverter // Import annotation @TypeConverter
import java.util.Date // Import lớp Date

// Lớp này chứa các phương thức chuyển đổi kiểu dữ liệu mà Room không hỗ trợ mặc định
class Converters {
    // Annotation @TypeConverter để báo cho Room biết đây là một hàm chuyển đổi từ kiểu Timestamp (Long) sang Date.
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? { // Hàm nhận vào một giá trị Long? (có thể null)
        // Nếu value là null, trả về null. Nếu không, tạo một đối tượng Date từ value.
        return value?.let { Date(it) }
    }

    // Annotation @TypeConverter để báo cho Room biết đây là một hàm chuyển đổi từ kiểu Date sang Timestamp (Long).
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? { // Hàm nhận vào một đối tượng Date? (có thể null)
        // Nếu date là null, trả về null. Nếu không, trả về thời gian (dưới dạng Long) của đối tượng Date.
        return date?.time
    }
}
