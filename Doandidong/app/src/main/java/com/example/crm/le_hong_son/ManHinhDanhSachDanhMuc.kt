package com.example.crm.le_hong_son

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Category
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

// Lớp Activity quản lý màn hình hiển thị danh sách các danh mục
class ManHinhDanhSachDanhMuc : AppCompatActivity() {

    // Khai báo biến cho RecyclerView để hiển thị danh sách
    private lateinit var recyclerView: RecyclerView
    // Khai báo biến cho nút FloatingActionButton để thêm mới danh mục
    private lateinit var fabThemDanhMuc: FloatingActionButton
    // Khai báo biến cho Adapter của RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    // Khai báo biến cho đối tượng truy cập cơ sở dữ liệu
    private lateinit var db: AppDatabase
    private lateinit var tvTongSoLuongDM: TextView // Khai báo TextView tổng số lượng

    // --- CÁC HÀM CHÍNH CỦA ACTIVITY ---

    // Hàm được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_man_hinh_danh_sach_danh_muc)

        db = AppDatabase.getDatabase(this)

        setControl()          // Ánh xạ các view từ layout
        setupRecyclerView()   // Cài đặt cho RecyclerView
        setEvent()            // Gán các sự kiện cho view
    }

    // Hàm được gọi mỗi khi Activity quay trở lại foreground (ví dụ: từ màn hình khác quay về)
    override fun onResume() {
        super.onResume()
        loadCategories() // Tải lại danh sách danh mục để cập nhật dữ liệu mới nhất
    }

    // --- CÁC HÀM CÀI ĐẶT ---

    // Hàm để ánh xạ (liên kết) các biến trong code với các view trong file layout XML
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "QUẢN LÝ DANH MỤC" // Set text cho tiêu đề
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ RecyclerView và nút FAB từ layout
        recyclerView = findViewById(R.id.rvDanhMuc)
        fabThemDanhMuc = findViewById(R.id.fabThemDanhMuc)
        tvTongSoLuongDM = findViewById(R.id.tvTongSoLuongDM) // Ánh xạ TextView tổng số lượng
    }
    
    // Hàm để gán các sự kiện (ví dụ: click) cho các view
    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Đóng Activity hiện tại khi nhấn nút back
        }
        // --- KẾT THÚC ĐỒNG BỘ ---
        
        // Gán sự kiện click cho nút FAB (nút tròn có dấu cộng)
        fabThemDanhMuc.setOnClickListener {
            val intent = Intent(this, ManHinhThemDanhMuc::class.java)
            startActivity(intent)
        }
    }

    // Hàm cài đặt và cấu hình cho RecyclerView
    private fun setupRecyclerView() {
        // Khởi tạo CategoryAdapter với một danh sách rỗng ban đầu và các hành động (lambda)
        categoryAdapter = CategoryAdapter(
            emptyList(), // Danh sách ban đầu là rỗng
            // Hành động khi nhấn nút "Sửa" trên một item
            onEditClick = { category ->
                val intent = Intent(this, ManHinhSuaDanhMuc::class.java)
                intent.putExtra("CATEGORY_ID", category.id)
                startActivity(intent)
            },
            // Hành động khi nhấn nút "Xóa" trên một item
            onDeleteClick = { category ->
                showDeleteConfirmationDialog(category)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this) // Đặt LayoutManager
        recyclerView.adapter = categoryAdapter // Gán adapter
    }


    // --- CÁC HÀM XỬ LÝ LOGIC ---

    // Hàm tải danh sách danh mục từ cơ sở dữ liệu Room và cập nhật lên RecyclerView
    private fun loadCategories() {
        lifecycleScope.launch {
            val categories = db.categoryDao().getAllCategories() // Lấy tất cả danh mục
            categoryAdapter.updateData(categories) // Cập nhật dữ liệu mới cho adapter
            tvTongSoLuongDM.text = "Tổng số lượng: ${categories.size}" // Cập nhật TextView tổng số lượng
        }
    }

    // Hàm hiển thị hộp thoại xác nhận việc xóa một danh mục
    private fun showDeleteConfirmationDialog(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa danh mục '${category.name}' không?")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    db.categoryDao().deleteCategory(category)
                    loadCategories() // Tải lại danh sách để cập nhật giao diện
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}