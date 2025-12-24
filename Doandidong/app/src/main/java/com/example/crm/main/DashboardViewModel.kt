package com.example.crm.main

import android.app.Application // Lớp Application cơ bản của Android.
import androidx.lifecycle.AndroidViewModel // Lớp ViewModel có tham chiếu đến Application context.
import androidx.lifecycle.LiveData // Lớp LiveData để giữ dữ liệu có thể quan sát được.
import com.example.crm.database.AppDatabase // Lớp database chính của ứng dụng.
import com.example.crm.database.CategoryDao // DAO cho bảng Category.
import com.example.crm.database.CustomerDao // DAO cho bảng Customer.
import com.example.crm.database.OrderDao // DAO cho bảng Order.
import com.example.crm.database.ProductDao // DAO cho bảng Product.

// ViewModel cho màn hình Dashboard, kế thừa từ AndroidViewModel để có thể truy cập Application context.
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    // Khai báo biến cho các DAO để truy cập vào database.
    private val customerDao: CustomerDao // Biến giữ tham chiếu đến CustomerDao.
    private val productDao: ProductDao // Biến giữ tham chiếu đến ProductDao.
    private val categoryDao: CategoryDao // Biến giữ tham chiếu đến CategoryDao.
    private val orderDao: OrderDao // Biến giữ tham chiếu đến OrderDao.

    // Khai báo các LiveData để chứa dữ liệu thống kê.
    val totalCustomers: LiveData<Int> // LiveData cho tổng số khách hàng.
    val totalProducts: LiveData<Int> // LiveData cho tổng số sản phẩm.
    val totalCategories: LiveData<Int> // LiveData cho tổng số danh mục.
    val totalOrders: LiveData<Int> // LiveData cho tổng số hóa đơn.
    val approvedOrders: LiveData<Int> // LiveData cho số hóa đơn đã được duyệt.
    val completedOrders: LiveData<Int> // LiveData cho số hóa đơn đã hoàn tất.
    val cancelledOrders: LiveData<Int> // LiveData cho số hóa đơn đã bị hủy.

    // Khối init được chạy khi một instance của DashboardViewModel được tạo.
    init {
        // Lấy instance của database.
        val database = AppDatabase.getDatabase(application)
        // Khởi tạo các DAO từ instance của database.
        customerDao = database.customerDao()
        productDao = database.productDao()
        categoryDao = database.categoryDao()
        orderDao = database.orderDao()

        // Gọi các hàm từ DAO để lấy dữ liệu LiveData.
        totalCustomers = customerDao.getTotalCustomerCount() // Lấy tổng số khách hàng.
        totalProducts = productDao.getTotalProductCount() // Lấy tổng số sản phẩm.
        totalCategories = categoryDao.getTotalCategoryCount() // Lấy tổng số danh mục.
        totalOrders = orderDao.getTotalOrderCount() // Lấy tổng số hóa đơn.
        // Lấy số lượng hóa đơn theo từng trạng thái bằng mã số nguyên (Int).
        // 1 = Đang giao (tương ứng với "Duyệt" trong yêu cầu)
        // 2 = Thành công (tương ứng với "Hoàn tất")
        // 3 = Đã hủy (tương ứng với "Hủy")
        approvedOrders = orderDao.getOrderStatusCount(1)
        completedOrders = orderDao.getOrderStatusCount(2)
        cancelledOrders = orderDao.getOrderStatusCount(3)
    }
}
