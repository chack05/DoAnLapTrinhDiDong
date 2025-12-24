package com.example.crm.le_hong_son

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Category
import kotlinx.coroutines.launch

// Lớp Activity quản lý màn hình chỉnh sửa thông tin một danh mục
class ManHinhSuaDanhMuc : AppCompatActivity() {

    // Khai báo biến cho ô nhập liệu tên danh mục
    private lateinit var edtCategoryName: EditText
    // Khai báo biến cho nút bấm để cập nhật danh mục
    private lateinit var btnLuuDanhMuc: Button
    // Khai báo biến cho đối tượng truy cập cơ sở dữ liệu
    private lateinit var db: AppDatabase
    // Biến để lưu ID của danh mục đang được sửa, -1 nghĩa là chưa có ID
    private var categoryId: Int = -1
    private var originalCategory: Category? = null // Biến lưu trữ danh mục gốc khi tải dữ liệu

    // --- CÁC HÀM CHÍNH CỦA ACTIVITY ---

    // Hàm được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        // Gọi hàm onCreate của lớp cha
        super.onCreate(savedInstanceState)
        // Tái sử dụng layout của màn hình Thêm Danh Mục
        setContentView(R.layout.layout_man_hinh_them_danh_muc)

        // Khởi tạo đối tượng database
        db = AppDatabase.getDatabase(this)

        // Lấy ID của danh mục được truyền qua từ màn hình danh sách
        categoryId = intent.getIntExtra("CATEGORY_ID", -1)

        // Gọi các hàm để cài đặt
        setControl() // Ánh xạ các view từ layout
        setEvent()   // Gán các sự kiện cho view

        // Kiểm tra xem ID có hợp lệ không
        if (categoryId == -1) {
            // Nếu ID không hợp lệ, hiển thị lỗi và đóng màn hình
            Toast.makeText(this, "Lỗi: Không tìm thấy danh mục để sửa", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Nếu ID hợp lệ, tải dữ liệu của danh mục đó lên
            loadCategoryData()
        }
    }

    // --- CÁC HÀM CÀI ĐẶT ---

    // Hàm để ánh xạ (liên kết) các biến trong code với các view trong file layout XML
    private fun setControl() {
        // --- SỬA LỖI CRASH: ÁNH XẠ TOOLBAR ĐÚNG CÁCH ---
        // Ghi chú: Đảm bảo trong file layout XML, thẻ <include> của bạn có android:id="@+id/layoutHeader"
        // 1. Ánh xạ view cha (container) của toolbar được include vào
        val header = findViewById<View>(R.id.layoutHeader)
        // 2. Từ view cha, tìm các view con bên trong nó (nút back, tiêu đề)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)

        // 3. Gán nội dung cho tiêu đề toolbar
        tvTitle.text = "CẬP NHẬT DANH MỤC"

        // Ánh xạ các view khác
        edtCategoryName = findViewById(R.id.edtCategoryName)
        btnLuuDanhMuc = findViewById(R.id.btnLuuDanhMuc)

        // Thay đổi text của nút từ "Lưu" thành "Cập nhật"
        btnLuuDanhMuc.text = "Cập nhật"
    }

    // Hàm để gán các sự kiện (ví dụ: click) cho các view
    private fun setEvent() {
        // --- SỬA LỖI CRASH: GÁN SỰ KIỆN CHO TOOLBAR ĐÚNG CÁCH ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        // Gán sự kiện click cho nút back
        btnBack.setOnClickListener {
            finish() // Đóng Activity hiện tại
        }
        // --- KẾT THÚC SỬA LỖI ---

        // Gán sự kiện click cho nút "Cập nhật"
        btnLuuDanhMuc.setOnClickListener {
            // Gọi hàm xử lý logic cập nhật
            updateCategory()
        }
    }

    // --- HÀM XỬ LÝ LOGIC ---

    // Hàm tải dữ liệu của danh mục (dựa vào categoryId) và hiển thị lên ô EditText
    private fun loadCategoryData() {
        // Sử dụng coroutine để truy vấn database trên luồng nền
        lifecycleScope.launch {
            // Tìm danh mục trong database có id trùng với categoryId
            // Lưu ý: Hiệu quả hơn nếu có hàm getCategoryById(id) trong DAO
            val category = db.categoryDao().getAllCategories().find { it.id == categoryId }

            // Kiểm tra xem có tìm thấy danh mục không
            if (category != null) {
                // Lưu danh mục gốc
                originalCategory = category
                // Nếu có, hiển thị tên của nó lên ô EditText
                edtCategoryName.setText(category.name)
            } else {
                // Nếu không tìm thấy, báo lỗi và đóng màn hình
                Toast.makeText(this@ManHinhSuaDanhMuc, "Lỗi: Dữ liệu danh mục không tồn tại.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Hàm kiểm tra và cập nhật thông tin danh mục vào database
    private fun updateCategory() {
        // Lấy tên mới từ ô EditText
        val name = edtCategoryName.text.toString().trim()

        // Xóa lỗi cũ (nếu có)
        edtCategoryName.error = null

        // 1. Kiểm tra xem tên có bị để trống không
        if (name.isEmpty()) {
            edtCategoryName.error = "Tên danh mục không được để trống."
            return
        }

        // Chạy coroutine để cập nhật trên luồng nền
        lifecycleScope.launch {
            // 2. Kiểm tra tên danh mục trùng lặp (chỉ khi tên thay đổi)
            if (name != originalCategory?.name) { // So sánh với tên gốc
                if (db.categoryDao().checkNameExist(name) > 0) {
                    edtCategoryName.error = "Tên danh mục này đã tồn tại."
                    return@launch
                }
            }

            // Tạo một đối tượng Category mới với ID cũ và tên mới
            val updatedCategory = Category(id = categoryId, name = name)

            // Gọi hàm update trong DAO
            db.categoryDao().updateCategory(updatedCategory)
            // Thông báo thành công
            Toast.makeText(this@ManHinhSuaDanhMuc, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            // Đóng màn hình sửa
            finish()
        }
    }
}