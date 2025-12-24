package com.example.crm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lớp Entity đại diện cho bảng 'orders' trong cơ sở dữ liệu.
 * Mỗi một đối tượng Order sẽ tương ứng với một hàng trong bảng.
 */
@Entity(tableName = "orders") // Đánh dấu lớp này là một Entity với tên bảng là "orders"
data class Order(
    @PrimaryKey(autoGenerate = true) // Đánh dấu 'id' là khóa chính và tự động tăng
    val id: Int = 0, // Mã đơn hàng duy nhất

    val customerId: Int, // ID của khách hàng đã đặt đơn hàng này

    val customerName: String, // Tên của khách hàng tại thời điểm đặt (lưu lại để hiển thị nhanh)

    val totalAmount: Double, // Tổng giá trị của đơn hàng

    val date: String, // Ngày đặt hàng, lưu dưới dạng String theo định dạng đã chọn (ví dụ: dd/MM/yyyy)
    val isVisibleInHistory: Boolean = true, // Trường mới cho chức năng xóa mềm
    val status: Int = 0 // 0: Chờ xác nhận, 1: Đang giao, 2: Thành công, 3: Đã hủy
)