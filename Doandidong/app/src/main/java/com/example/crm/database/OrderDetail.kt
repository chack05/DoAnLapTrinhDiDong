package com.example.crm.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Lớp Entity đại diện cho bảng 'order_details' trong cơ sở dữ liệu.
 * Bảng này chứa chi tiết của từng sản phẩm trong một đơn hàng.
 */
@Entity(
    tableName = "order_details",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE // Nếu một Order bị xóa, các OrderDetail liên quan cũng sẽ bị xóa
        )
    ]
)
data class OrderDetail(
    @PrimaryKey(autoGenerate = true) // Đánh dấu 'id' là khóa chính và tự động tăng
    val id: Int = 0, // ID của dòng chi tiết đơn hàng

    val orderId: Int, // Khóa ngoại, trỏ về ID của bảng 'orders'

    val productId: Int, // ID của sản phẩm được mua

    val productName: String, // Tên sản phẩm tại thời điểm mua

    val quantity: Int, // Số lượng sản phẩm được mua

    val price: Double, // Giá bán của một sản phẩm tại thời điểm mua

    val total: Double // Thành tiền cho dòng này (quantity * price)
)