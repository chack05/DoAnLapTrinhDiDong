package com.example.crm.le_hong_son // Khai báo package chứa file

import android.content.Context // Import Context để truy cập SharedPreferences
import android.content.Intent // Import lớp Intent để thực hiện chuyển đổi giữa các Activity
import android.os.Bundle // Import lớp Bundle để xử lý dữ liệu được truyền giữa các Activity
import android.widget.Button // Import lớp Button để sử dụng widget Button
import android.widget.EditText // Import lớp EditText để sử dụng widget EditText
import android.widget.TextView // Import lớp TextView
import android.widget.Toast // Import lớp Toast để hiển thị thông báo nhanh
import androidx.appcompat.app.AppCompatActivity // Import lớp AppCompatActivity làm lớp cơ sở
import androidx.lifecycle.lifecycleScope // Import lifecycleScope để chạy coroutine an toàn
import com.example.crm.database.AppDatabase // Import lớp AppDatabase để truy cập vào cơ sở dữ liệu
import com.example.crm.main.ManHinhChinh // Import màn hình chính
import com.example.crm.R // Import R class để truy cập tài nguyên
import com.example.crm.mainUser.ManHinhTrangChuUser // Cập nhật import cho ManHinhTrangChuUser
import kotlinx.coroutines.launch // Import hàm launch để bắt đầu một coroutine mới

// Lớp ManHinhDangNhap kế thừa từ AppCompatActivity để quản lý màn hình đăng nhập
class ManHinhDangNhap : AppCompatActivity() {

    // Khai báo biến cho các view, sử dụng `lateinit` vì chúng sẽ được khởi tạo trong `onCreate`
    private lateinit var edtEmail: EditText
    private lateinit var edtMatKhau: EditText
    private lateinit var btnDangNhap: Button
    private lateinit var tvChuyenDangKy: TextView
    private lateinit var appDatabase: AppDatabase

    // Hàm `onCreate` được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Thiết lập layout cho Activity từ file XML đã được thiết kế lại
        setContentView(R.layout.layout_dang_nhap)

        // Khởi tạo database
        appDatabase = AppDatabase.getDatabase(applicationContext)

        // Gọi các hàm để khởi tạo view và thiết lập sự kiện
        setControl()
        setEvent()
    }

    // Hàm `setControl` để ánh xạ các biến với các view trong layout
    private fun setControl() {
        edtEmail = findViewById(R.id.edtEmail) // Ánh xạ ô nhập email
        edtMatKhau = findViewById(R.id.edtMatKhau) // Ánh xạ ô nhập mật khẩu
        btnDangNhap = findViewById(R.id.btnDangNhap) // Ánh xạ nút đăng nhập
        tvChuyenDangKy = findViewById(R.id.tvChuyenDangKy) // Ánh xạ text view chuyển sang đăng ký
    }

    // Hàm `setEvent` để xử lý các tương tác của người dùng
    private fun setEvent() {
        // Thiết lập sự kiện click cho nút Đăng nhập
        btnDangNhap.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val pass = edtMatKhau.text.toString().trim()

            // 1. ADMIN CỨNG
            if (email == "admin@gmail.com" && pass == "Admin123@") {
                startActivity(Intent(this, ManHinhChinh::class.java))
                finish()
                return@setOnClickListener
            }

            // 2. USER THƯỜNG
            lifecycleScope.launch {
                val user = appDatabase.userDao().checkLogin(email, pass)
                if (user != null) {
                    // Lưu session
                    val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
                    prefs.edit().putString("EMAIL", email).apply()

                    startActivity(Intent(this@ManHinhDangNhap, ManHinhTrangChuUser::class.java))
                    finish()
                } else {
                    Toast.makeText(this@ManHinhDangNhap, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Thiết lập sự kiện click cho text view để chuyển sang màn hình Đăng ký
        tvChuyenDangKy.setOnClickListener {
            // Tạo một Intent để mở ManHinhDangKy
            val intent = Intent(this, ManHinhDangKy::class.java)
            startActivity(intent) // Bắt đầu Activity đăng ký
        }
    }
}
