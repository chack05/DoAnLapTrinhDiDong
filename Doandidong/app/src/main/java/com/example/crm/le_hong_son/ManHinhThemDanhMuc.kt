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

// Lớp Activity quản lý màn hình thêm mới một danh mục
class ManHinhThemDanhMuc : AppCompatActivity() {

    // Khai báo biến cho ô nhập liệu tên danh mục
    private lateinit var edtCategoryName: EditText
    // Khai báo biến cho nút bấm để lưu danh mục
    private lateinit var btnLuuDanhMuc: Button
    // Khai báo biến cho đối tượng truy cập cơ sở dữ liệu
    private lateinit var db: AppDatabase

    // --- CÁC HÀM CHÍNH CỦA ACTIVITY ---

    // Hàm được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        // Gọi hàm onCreate của lớp cha
        super.onCreate(savedInstanceState)
        // Set layout cho Activity này từ file XML `layout_man_hinh_them_danh_muc`
        setContentView(R.layout.layout_man_hinh_them_danh_muc)

        // Khởi tạo đối tượng database
        db = AppDatabase.getDatabase(this)

        // Gọi các hàm để cài đặt
        setControl() // Ánh xạ các view từ layout
        setEvent()   // Gán các sự kiện cho view
    }

    // --- CÁC HÀM CÀI ĐẶT ---

    // Hàm để ánh xạ (liên kết) các biến trong code với các view trong file layout XML
    private fun setControl() {
        // --- SỬA LỖI CRASH: ÁNH XẠ TOOLBAR ĐÚNG CÁCH ---
        // Ghi chú: Đảm bảo trong file layout XML, thẻ <include> của bạn có android:id="@+id/layoutHeader"
        // 1. Ánh xạ view cha (container) của toolbar được include vào
        val header = findViewById<View>(R.id.layoutHeader)
        // 2. Từ view cha, tìm các view con bên trong nó (nút back, tiêu đề)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)

        // 3. Gán nội dung cho tiêu đề toolbar
        tvTitle.text = "THÊM DANH MỤC"

        // Ánh xạ các view khác trong layout
        edtCategoryName = findViewById(R.id.edtCategoryName)
        btnLuuDanhMuc = findViewById(R.id.btnLuuDanhMuc)
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

        // Gán sự kiện click cho nút "Lưu"
        btnLuuDanhMuc.setOnClickListener {
            // Gọi hàm xử lý logic lưu danh mục
            saveCategory()
        }
    }

    // --- HÀM XỬ LÝ LOGIC ---

    // Hàm kiểm tra dữ liệu và lưu danh mục mới vào cơ sở dữ liệu
    private fun saveCategory() {
        // Lấy tên danh mục từ ô EditText và loại bỏ khoảng trắng thừa ở hai đầu
        val name = edtCategoryName.text.toString().trim()

        // Xóa lỗi cũ (nếu có)
        edtCategoryName.error = null

        // 1. Kiểm tra xem người dùng đã nhập tên danh mục hay chưa
        if (name.isEmpty()) {
            edtCategoryName.error = "Tên danh mục không được để trống."
            return
        }

        // Sử dụng coroutine để thực hiện thao tác chèn (insert) vào database trên luồng nền
        lifecycleScope.launch {
            // 2. Kiểm tra tên danh mục đã tồn tại hay chưa
            if (db.categoryDao().checkNameExist(name) > 0) {
                edtCategoryName.error = "Tên danh mục này đã tồn tại."
                return@launch
            }

            // Tạo một đối tượng Category mới với tên đã nhập
            val newCategory = Category(name = name)

            // Gọi hàm trong DAO để chèn đối tượng mới vào bảng
            db.categoryDao().insertCategory(newCategory)
            
            // Sau khi chèn thành công, hiển thị thông báo cho người dùng
            // Toast được chạy trên Main thread
            Toast.makeText(this@ManHinhThemDanhMuc, "Thêm danh mục thành công", Toast.LENGTH_SHORT).show()
            
            // Đóng màn hình thêm mới và quay trở lại màn hình danh sách
            finish()
        }
    }
}