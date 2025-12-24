package com.example.crm.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update // Thêm Update để có thể cập nhật trạng thái đơn hàng nếu cần
import androidx.lifecycle.LiveData // Thêm LiveData để hỗ trợ LiveData<Int> cho các hàm thống kê

// Annotation @Dao để Room hiểu rằng interface này chứa các phương thức truy cập database
@Dao
interface OrderDao {

    // --- Chức năng liên quan đến Order ---

    // Chèn một đơn hàng mới vào database. Trả về Long là id của dòng mới được chèn vào.
    @Insert
    suspend fun insertOrder(order: Order): Long

    // Cập nhật thông tin một đơn hàng đã có (nếu cần thay đổi thông tin chung của đơn hàng).
    @Update
    suspend fun updateOrder(order: Order) // Thêm hàm update Order

    // Xóa một đơn hàng khỏi database.
    @Delete
    suspend fun deleteOrder(order: Order)

    // Lấy tất cả đơn hàng, sắp xếp mới nhất lên trước theo ID.
    @Query("SELECT * FROM orders ORDER BY id DESC")
    suspend fun getAllOrders(): List<Order>

    // Lấy một đơn hàng cụ thể dựa trên ID.
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Int): Order? // Thêm hàm lấy Order theo ID

    // --- Chức năng liên quan đến Xóa mềm (Soft Delete) ---

    // Lấy tất cả đơn hàng có cờ isVisibleInHistory = true, sắp xếp mới nhất lên trước theo ID.
    @Query("SELECT * FROM orders WHERE isVisibleInHistory = 1 ORDER BY id DESC")
    suspend fun getVisibleOrdersInHistory(): List<Order>

    // Cập nhật trường isVisibleInHistory của một đơn hàng thành false (ẩn khỏi lịch sử).
    @Query("UPDATE orders SET isVisibleInHistory = 0 WHERE id = :orderId")
    suspend fun hideOrderFromHistory(orderId: Int)

    // --- Chức năng liên quan đến OrderDetail ---

    // Chèn một chi tiết đơn hàng mới.
    @Insert
    suspend fun insertOrderDetail(detail: OrderDetail) // Đổi tên tham số thành 'detail'

    // Xóa tất cả chi tiết đơn hàng dựa trên orderId.
    @Query("DELETE FROM order_details WHERE orderId = :orderId")
    suspend fun deleteOrderDetails(orderId: Int)

    // Lấy tất cả chi tiết của một đơn hàng cụ thể dựa trên orderId.
    @Query("SELECT * FROM order_details WHERE orderId = :orderId")
    suspend fun getOrderDetails(orderId: Int): List<OrderDetail> // Đổi tên hàm thành getOrderDetails

    // Bổ sung hàm tìm kiếm đơn hàng theo tên khách hàng hoặc ID đơn hàng
    @Query("SELECT * FROM orders WHERE customerName LIKE '%' || :query || '%' OR id LIKE '%' || :query || '%'")
    suspend fun searchOrder(query: String): List<Order>

    // Bổ sung hàm xóa tất cả đơn hàng
    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()

    // Bổ sung hàm xóa tất cả chi tiết đơn hàng
    @Query("DELETE FROM order_details")
    suspend fun deleteAllOrderDetails()

    // Bổ sung hàm tìm kiếm và lọc đơn hàng theo từ khóa và ID sản phẩm
    @Query("""
        SELECT o.* FROM orders o
        WHERE o.isVisibleInHistory = 1
        AND (o.customerName LIKE '%' || :keyword || '%' OR CAST(o.id AS TEXT) LIKE '%' || :keyword || '%')
        AND (NOT (:productId > 0) OR o.id IN (SELECT od.orderId FROM order_details od WHERE od.productId = :productId))
        ORDER BY o.id DESC
    """)
    suspend fun getOrdersCombined(keyword: String, productId: Int): List<Order>

    // Bổ sung hàm đếm số lượng đơn hàng chứa một sản phẩm cụ thể
    @Query("SELECT COUNT(DISTINCT orderId) FROM order_details WHERE productId = :productId")
    suspend fun countOrdersByProductId(productId: Int): Int

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY id DESC")
    suspend fun getOrdersByCustomerId(customerId: Int): List<Order>

    // Bổ sung hàm cập nhật trạng thái đơn hàng
    @Query("UPDATE orders SET status = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, newStatus: Int)
    
    // --- Thống kê cho Dashboard ---

    // Lấy tổng số lượng hóa đơn, trả về LiveData để tự động cập nhật
    @Query("SELECT COUNT(*) FROM orders")
    fun getTotalOrderCount(): LiveData<Int>

    // Lấy số lượng hóa đơn theo trạng thái, trả về LiveData
    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    fun getOrderStatusCount(status: Int): LiveData<Int>
}