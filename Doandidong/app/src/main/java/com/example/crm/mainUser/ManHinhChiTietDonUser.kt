package com.example.crm.mainUser

import android.content.Context
import android.content.Intent // Import Intent
import android.os.Bundle
import android.util.Log // Import Log
import android.view.View
import android.widget.Button // Import Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Order
import com.example.crm.phan_thanh_tai.ManHinhSuaDon // Tạm thời dùng ManHinhSuaDon của admin, sẽ tạo ManHinhSuaDonUser sau
import com.example.crm.phan_thanh_tai.OrderDetailSummaryAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Lớp Activity quản lý màn hình hiển thị chi tiết một đơn hàng cho người dùng
class ManHinhChiTietDonUser : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var tvChiTietOrderId: TextView
    private lateinit var tvChiTietCustomerName: TextView
    private lateinit var tvChiTietOrderDate: TextView
    private lateinit var tvChiTietTotalAmount: TextView
    private lateinit var tvChiTietOrderStatus: TextView // TextView hiển thị trạng thái đơn hàng
    private lateinit var rvChiTietSanPhamTrongDon: RecyclerView
    private lateinit var btnSuaDonUser: Button // Nút sửa đơn hàng của user
    private lateinit var btnXoaDonUser: Button // Nút xóa đơn hàng của user

    // --- Khai báo biến logic ---
    private lateinit var db: AppDatabase
    private lateinit var orderDetailSummaryAdapter: OrderDetailSummaryAdapter
    private var orderId: Int = -1 // ID của đơn hàng cần hiển thị chi tiết
    private var currentOrder: Order? = null // Lưu đối tượng Order hiện tại

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_chi_tiet_don_user) // Sử dụng layout riêng cho user

        db = AppDatabase.getDatabase(this)
        orderId = intent.getIntExtra("ORDER_ID", -1) // Lấy orderId từ Intent
        Log.d("ManHinhChiTietDonUser", "onCreate: orderId nhận được = $orderId")

        setControl()
        setEvent()
        setupRecyclerView()

        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng.", Toast.LENGTH_SHORT).show()
            Log.e("ManHinhChiTietDonUser", "onCreate: orderId không hợp lệ, đóng Activity.")
            finish()
        } else {
            loadOrderDetails() // Tải dữ liệu chi tiết đơn hàng
        }
    }

    // --- Các hàm cài đặt ---

    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "CHI TIẾT ĐƠN HÀNG CỦA BẠN" // Đặt tiêu đề cho màn hình user
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ các TextView hiển thị thông tin chung
        tvChiTietOrderId = findViewById(R.id.tvChiTietOrderId)
        tvChiTietCustomerName = findViewById(R.id.tvChiTietCustomerName)
        tvChiTietOrderDate = findViewById(R.id.tvChiTietOrderDate)
        tvChiTietTotalAmount = findViewById(R.id.tvChiTietTotalAmount)
        tvChiTietOrderStatus = findViewById(R.id.tvChiTietOrderStatus) // Ánh xạ TextView trạng thái

        // Ánh xạ RecyclerView và các nút user
        rvChiTietSanPhamTrongDon = findViewById(R.id.rvChiTietSanPhamTrongDon)
        btnSuaDonUser = findViewById(R.id.btnSuaDonUser)
        btnXoaDonUser = findViewById(R.id.btnXoaDonUser)
    }

    private fun setupRecyclerView() {
        orderDetailSummaryAdapter = OrderDetailSummaryAdapter(emptyList()) // Khởi tạo adapter với danh sách rỗng
        rvChiTietSanPhamTrongDon.layoutManager = LinearLayoutManager(this)
        rvChiTietSanPhamTrongDon.adapter = orderDetailSummaryAdapter
    }

    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Nhấn nút back thì đóng màn hình
        }
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Gán sự kiện click cho nút sửa đơn hàng của user
        btnSuaDonUser.setOnClickListener {
            // Chỉ cho phép sửa khi trạng thái là "Chờ xác nhận"
            if (currentOrder?.status == 0) {
                val intent = Intent(this, ManHinhSuaDonUser::class.java) // Sử dụng ManHinhSuaDonUser của user
                intent.putExtra("ORDER_ID", orderId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Chỉ có thể sửa đơn hàng khi đang 'Chờ xác nhận'.", Toast.LENGTH_SHORT).show()
            }
        }

        // Gán sự kiện click cho nút xóa đơn hàng của user
        btnXoaDonUser.setOnClickListener {
            currentOrder?.let { showDeleteConfirmationDialogUser(it) }
        }
    }

    // --- Các hàm xử lý Logic nghiệp vụ ---

    // Hàm tải thông tin chi tiết của đơn hàng
    private fun loadOrderDetails() {
        lifecycleScope.launch {
            Log.d("ManHinhChiTietDonUser", "loadOrderDetails: Bắt đầu tải chi tiết đơn hàng cho orderId = $orderId")
            // Lấy Order và OrderDetails từ database
            val order = db.orderDao().getOrderById(orderId)
            val orderDetails = db.orderDao().getOrderDetails(orderId)

            if (order == null) {
                Toast.makeText(this@ManHinhChiTietDonUser, "Không tìm thấy đơn hàng này.", Toast.LENGTH_SHORT).show()
                Log.e("ManHinhChiTietDonUser", "loadOrderDetails: Không tìm thấy đơn hàng với orderId = $orderId")
                finish()
                return@launch
            }

            currentOrder = order
            Log.d("ManHinhChiTietDonUser", "loadOrderDetails: Đã tải đơn hàng: ID=${order.id}, Status=${order.status}")
            Log.d("ManHinhChiTietDonUser", "loadOrderDetails: Số lượng chi tiết đơn hàng: ${orderDetails.size}")

            // Cập nhật thông tin chung của đơn hàng
            tvChiTietOrderId.text = "Mã đơn hàng: #${order.id}"
            tvChiTietCustomerName.text = "Khách hàng: ${order.customerName}"
            tvChiTietOrderDate.text = "Ngày đặt: ${order.date}"
            tvChiTietTotalAmount.text = formatCurrency(order.totalAmount)

            // Hiển thị trạng thái đơn hàng
            displayOrderStatus(order.status, tvChiTietOrderStatus)

            // Cập nhật dữ liệu cho RecyclerView chi tiết sản phẩm
            orderDetailSummaryAdapter.updateData(orderDetails)

            // Điều chỉnh khả năng tương tác của các nút user dựa trên trạng thái đơn hàng
            when (order.status) {
                0 -> { // Chờ xác nhận: có thể sửa/hủy
                    btnSuaDonUser.isEnabled = true
                    btnXoaDonUser.isEnabled = true
                }
                else -> { // Các trạng thái khác: không thể sửa/hủy
                    btnSuaDonUser.isEnabled = false
                    btnXoaDonUser.isEnabled = false
                }
            }
        }
    }

    // Hàm hiển thị hộp thoại xác nhận xóa đơn hàng (của user)
    private fun showDeleteConfirmationDialogUser(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận hủy đơn hàng")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #${order.id} không? Thao tác này có thể không hoàn tác.")
            .setPositiveButton("Hủy đơn") { _, _ ->
                lifecycleScope.launch {
                    // Cập nhật trạng thái thành "Đã hủy" (status = 3)
                    db.orderDao().updateOrderStatus(order.id, 3)
                    Toast.makeText(this@ManHinhChiTietDonUser, "Đơn hàng #${order.id} đã được hủy.", Toast.LENGTH_SHORT).show()
                    loadOrderDetails() // Tải lại chi tiết để cập nhật trạng thái trên giao diện
                }
            }
            .setNegativeButton("Không", null)
            .show()
    }

    // Hàm định dạng tiền tệ
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }

    // Hàm tiện ích để hiển thị trạng thái và màu sắc
    private fun displayOrderStatus(status: Int, textView: TextView) {
        when (status) {
            0 -> { // Chờ xác nhận
                textView.text = "Trạng thái: 🔴 Chờ xác nhận"
                textView.setTextColor(ContextCompat.getColor(this, R.color.orange_status))
            }
            1 -> { // Đang giao
                textView.text = "Trạng thái: 🔵 Đang giao"
                textView.setTextColor(ContextCompat.getColor(this, R.color.blue_status))
            }
            2 -> { // Thành công
                textView.text = "Trạng thái: 🟢 Thành công"
                textView.setTextColor(ContextCompat.getColor(this, R.color.green_status))
            }
            3 -> { // Đã hủy
                textView.text = "Trạng thái: ⚫ Đã hủy"
                textView.setTextColor(ContextCompat.getColor(this, R.color.red_status))
            }
            else -> { // Trạng thái không xác định
                textView.text = "Trạng thái: Không xác định"
                textView.setTextColor(ContextCompat.getColor(this, R.color.gray_status))
            }
        }
    }
}