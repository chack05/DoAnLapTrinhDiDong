package com.example.crm.do_duc_anh

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
import com.example.crm.database.Customer
import com.example.crm.database.User
import com.example.crm.utils.ValidateUtils
import kotlinx.coroutines.launch

class ManHinhThemKhach : AppCompatActivity() {

    private lateinit var edtTenKhachHang: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtSoDienThoai: EditText
    private lateinit var edtMatKhau: EditText // Thêm EditText cho mật khẩu
    private lateinit var btnLuuKhachHang: Button

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_them_khach)
        db = AppDatabase.getDatabase(this)
        setControl()
        setEvent()
    }

    private fun setControl() {
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "TẠO TÀI KHOẢN KHÁCH HÀNG"

        edtTenKhachHang = findViewById(R.id.edtTenKhachHang)
        edtEmail = findViewById(R.id.edtEmail)
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai)
        edtMatKhau = findViewById(R.id.edtMatKhau) // Ánh xạ EditText mật khẩu
        btnLuuKhachHang = findViewById(R.id.btnLuuKhachHang)
    }

    private fun setEvent() {
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        btnLuuKhachHang.setOnClickListener {
            saveCustomerAndUser()
        }
    }

    private fun saveCustomerAndUser() {
        val name = edtTenKhachHang.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val phone = edtSoDienThoai.text.toString().trim()
        val password = edtMatKhau.text.toString().trim()

        // Xóa lỗi cũ
        edtTenKhachHang.error = null
        edtEmail.error = null
        edtSoDienThoai.error = null
        edtMatKhau.error = null

        // --- VALIDATION ---
        if (name.isEmpty()) {
            edtTenKhachHang.error = "Tên không được để trống."
            return
        }
        if (email.isEmpty()) {
            edtEmail.error = "Email không được để trống."
            return
        }
        if (!ValidateUtils.isValidEmail(email)) {
            edtEmail.error = "Email sai định dạng."
            return
        }
        if (password.isEmpty()) {
            edtMatKhau.error = "Mật khẩu không được để trống."
            return
        }
        if (phone.isEmpty()) {
            edtSoDienThoai.error = "Số điện thoại không được để trống."
            return
        }
        if (!ValidateUtils.isValidPhone(phone)) {
            edtSoDienThoai.error = "SĐT phải có 10 chữ số và bắt đầu bằng 0."
            return
        }

        lifecycleScope.launch {
            // 1. Kiểm tra email đã được dùng để đăng ký tài khoản chưa
            if (db.userDao().checkEmailExist(email) > 0) {
                edtEmail.error = "Email này đã được dùng để tạo tài khoản."
                return@launch
            }

            // 2. Tạo và chèn User mới
            val newUser = User(name = name, email = email, password = password, phone = phone)
            db.userDao().insert(newUser)

            // 3. Lấy lại User vừa chèn để có ID
            val insertedUser = db.userDao().getUserByEmail(email)
            if (insertedUser == null) {
                Toast.makeText(this@ManHinhThemKhach, "Lỗi khi tạo tài khoản người dùng.", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val newUserId = insertedUser.id

            // 4. Tạo mã khách hàng tự động và chèn Customer
            val autoCode = "KH${System.currentTimeMillis() % 100000}"
            val newCustomer = Customer(
                code = autoCode,
                name = name,
                email = email,
                phone = phone,
                password = password,
                userId = newUserId
            )
            db.customerDao().insert(newCustomer)

            Toast.makeText(this@ManHinhThemKhach, "Tạo tài khoản khách hàng thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}