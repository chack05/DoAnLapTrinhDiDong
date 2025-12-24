package com.example.crm.mai_duc_phi_son

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Order
import kotlinx.coroutines.launch

// Lớp Activity quản lý màn hình Lịch sử đơn hàng
class ManHinhLichSu : AppCompatActivity() {

    // --- Khai báo View và Adapter ---
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var tvTongSoLichSu: TextView // Khai báo TextView tổng số lịch sử

    // Khai báo biến Database
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_man_hinh_lich_su)

        db = AppDatabase.getDatabase(this)

        setControl()
        setEvent()
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadHistoryData() // Tải dữ liệu lịch sử mỗi khi Activity quay lại foreground
    }

    // Hàm ánh xạ View
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "LỊCH SỬ GIAO DỊCH" // Đặt tiêu đề
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ RecyclerView và TextView tổng số lịch sử
        recyclerView = findViewById(R.id.recyclerViewLichSu)
        tvTongSoLichSu = findViewById(R.id.tvTongSoLichSu) // Ánh xạ TextView tổng số lịch sử
    }

    // Hàm gán sự kiện
    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Đóng màn hình
        }
        // --- KẾT THÚC ĐỒNG BỘ ---
    }
    
    // Hàm cài đặt RecyclerView
    private fun setupRecyclerView() {
        // Khởi tạo HistoryAdapter với danh sách rỗng và callback cho nút xóa
        historyAdapter = HistoryAdapter(emptyList()) { order ->
            // Xử lý sự kiện khi nút xóa trên item được click (chức năng xóa mềm)
            showSoftDeleteConfirmationDialog(order)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter
    }

    // Hàm tải dữ liệu lịch sử từ DB (chỉ các đơn hàng có isVisibleInHistory = true)
    private fun loadHistoryData() {
        lifecycleScope.launch {
            val orderList = db.orderDao().getVisibleOrdersInHistory() // Lấy chỉ các đơn hàng hiển thị
            historyAdapter.updateData(orderList)
            tvTongSoLichSu.text = "Tổng số lượng: ${orderList.size}" // Cập nhật TextView tổng số lịch sử
        }
    }

    // Hàm hiển thị hộp thoại xác nhận xóa mềm
    private fun showSoftDeleteConfirmationDialog(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Ẩn đơn hàng khỏi lịch sử")
            .setMessage("Bạn có chắc chắn muốn ẩn đơn hàng #${order.id} khỏi lịch sử không? Đơn hàng vẫn sẽ tồn tại trong hệ thống quản lý.")
            .setPositiveButton("Ẩn") { _, _ ->
                lifecycleScope.launch {
                    db.orderDao().hideOrderFromHistory(order.id) // Gọi DAO để ẩn đơn hàng
                    loadHistoryData() // Tải lại dữ liệu sau khi ẩn
                    Toast.makeText(this@ManHinhLichSu, "Đơn hàng #${order.id} đã được ẩn khỏi lịch sử.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}