package com.example.crm.mainUser // Đổi package thành mainUser

import android.content.Intent
import android.os.Bundle
import android.util.Log // Import Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.phan_thanh_tai.ManHinhChiTietDon
import com.example.crm.phan_thanh_tai.OrderAdapter
import kotlinx.coroutines.launch

/**
 * Lớp này quản lý màn hình "Lịch sử mua hàng" của người dùng.
 * Nó hiển thị danh sách các đơn hàng mà người dùng đã tạo.
 * Đã được tái cấu trúc và bình luận chi tiết.
 */
class ManHinhLichSuMuaHang : AppCompatActivity() {

    // --- Khai báo biến cho View ---
    private lateinit var toolbar: Toolbar         // Thanh công cụ ở trên cùng
    private lateinit var recyclerView: RecyclerView // View để hiển thị danh sách đơn hàng

    // --- Khai báo biến cho logic ---
    private lateinit var orderAdapter: OrderAdapter // Adapter để kết nối dữ liệu với RecyclerView
    private lateinit var db: AppDatabase          // Đối tượng truy cập database
    private var customerId: Int = -1              // ID của khách hàng đang đăng nhập

    // --- Vòng đời của Activity ---

    // Hàm được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Gọi hàm của lớp cha
        // Gắn layout XML vào Activity
        setContentView(R.layout.activity_man_hinh_lich_su_mua_hang)

        // Khởi tạo database
        db = AppDatabase.getDatabase(this)
        // Lấy customerId được truyền từ màn hình trước
        customerId = intent.getIntExtra("CUSTOMER_ID", -1)
        Log.d("ManHinhLichSuMuaHang", "onCreate: customerId nhận được = $customerId")


        // Gọi các hàm để cài đặt
        setControl()
        setEvent()
        setupRecyclerView()

        // Nếu có customerId hợp lệ, tải danh sách đơn hàng
        if (customerId != -1) {
            loadOrders()
        } else {
            // Nếu không có ID, thông báo lỗi và đóng màn hình
            Toast.makeText(this, "Lỗi: Không thể tải lịch sử mua hàng.", Toast.LENGTH_SHORT).show()
            Log.e("ManHinhLichSuMuaHang", "onCreate: customerId không hợp lệ, đóng Activity.")
            finish()
        }
    }

    // --- Các hàm cài đặt ---

    /**
     * Hàm ánh xạ các view từ file layout XML.
     */
    private fun setControl() {
        // Ánh xạ Toolbar và RecyclerView
        toolbar = findViewById(R.id.toolbar_lich_su_mua_hang)
        recyclerView = findViewById(R.id.recyclerViewLichSuMuaHang)
    }

    /**
     * Hàm gán sự kiện cho các view.
     */
    private fun setEvent() {
        // Cài đặt Toolbar làm Action Bar
        setSupportActionBar(toolbar)
        // Hiển thị nút back trên Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Gán sự kiện cho nút back để đóng màn hình
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    // --- Các hàm xử lý logic ---

    /**
     * Hàm cài đặt RecyclerView và Adapter.
     */
    private fun setupRecyclerView() {
        // Sửa lỗi: Khởi tạo OrderAdapter với danh sách rỗng và xử lý sự kiện click
        // Khi người dùng click vào một đơn hàng, sẽ mở màn hình chi tiết đơn hàng đó (dành cho User)
        orderAdapter = OrderAdapter(emptyList()) { order ->
            Log.d("ManHinhLichSuMuaHang", "Clicked on order: ID=${order.id}")
            val intent = Intent(this, ManHinhChiTietDonUser::class.java) // Mở ManHinhChiTietDonUser
            intent.putExtra("ORDER_ID", order.id) // Truyền ID của đơn hàng được chọn
            startActivity(intent)
        }
        // Đặt LayoutManager cho RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Gán adapter cho RecyclerView
        recyclerView.adapter = orderAdapter
        Log.d("ManHinhLichSuMuaHang", "setupRecyclerView: OrderAdapter đã được cài đặt.")
    }

    /**
     * Hàm tải danh sách đơn hàng từ database và cập nhật lên RecyclerView.
     */
    private fun loadOrders() {
        // Chạy coroutine để truy vấn database trên luồng nền
        lifecycleScope.launch {
            Log.d("ManHinhLichSuMuaHang", "loadOrders: Bắt đầu tải đơn hàng cho customerId = $customerId")
            // Lấy danh sách đơn hàng của khách hàng theo customerId
            val orders = db.orderDao().getOrdersByCustomerId(customerId)
            Log.d("ManHinhLichSuMuaHang", "loadOrders: Đã tải ${orders.size} đơn hàng.")
            // Sửa lỗi: Gọi đúng hàm updateData của adapter
            orderAdapter.updateData(orders)
            if (orders.isEmpty()) {
                Toast.makeText(this@ManHinhLichSuMuaHang, "Bạn chưa có đơn hàng nào.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}