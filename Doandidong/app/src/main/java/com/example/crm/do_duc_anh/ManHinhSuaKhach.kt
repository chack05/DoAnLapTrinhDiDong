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

class ManHinhSuaKhach : AppCompatActivity() {

    private lateinit var edtTenKhachHang: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtSoDienThoai: EditText
    private lateinit var edtMatKhau: EditText // Thêm trường mật khẩu
    private lateinit var btnCapNhatKhachHang: Button

    private lateinit var db: AppDatabase
    private var customerId: Int = -1
    private var currentCustomer: Customer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_sua_khach)

        db = AppDatabase.getDatabase(this)
        customerId = intent.getIntExtra("CUSTOMER_ID", -1)

        setControl()
        setEvent()

        if (customerId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy khách hàng.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadCustomerData()
        }
    }

    private fun setControl() {
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "CẬP NHẬT KHÁCH HÀNG"

        edtTenKhachHang = findViewById(R.id.edtTenKhachHang)
        edtEmail = findViewById(R.id.edtEmail)
        edtMatKhau = findViewById(R.id.edtMatKhau) // Ánh xạ trường mật khẩu
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai)
        btnCapNhatKhachHang = findViewById(R.id.btnCapNhatKhachHang)
    }

    private fun setEvent() {
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        btnCapNhatKhachHang.setOnClickListener {
            updateCustomerAndUser()
        }
    }

    private fun loadCustomerData() {
        lifecycleScope.launch {
            currentCustomer = db.customerDao().getCustomerById(customerId)
            currentCustomer?.let { customer ->
                findViewById<EditText>(R.id.edtMaKhachHang).setText(customer.code) // Giữ lại để hiển thị mã
                edtTenKhachHang.setText(customer.name)
                edtEmail.setText(customer.email)
                edtSoDienThoai.setText(customer.phone)
                edtMatKhau.setText(customer.password) // Hiển thị mật khẩu
            }
        }
    }

    private fun updateCustomerAndUser() {
        val name = edtTenKhachHang.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val phone = edtSoDienThoai.text.toString().trim()
        val password = edtMatKhau.text.toString().trim()

        edtTenKhachHang.error = null
        edtEmail.error = null
        edtSoDienThoai.error = null
        edtMatKhau.error = null

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng không để trống thông tin.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!ValidateUtils.isValidEmail(email)) {
            edtEmail.error = "Email sai định dạng."
            return
        }
        if (!ValidateUtils.isValidPhone(phone)) {
            edtSoDienThoai.error = "SĐT phải có 10 chữ số và bắt đầu bằng 0."
            return
        }

        lifecycleScope.launch {
            val originalCustomer = currentCustomer
            if (originalCustomer == null) {
                Toast.makeText(this@ManHinhSuaKhach, "Lỗi: Không tìm thấy dữ liệu gốc.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 1. Cập nhật bảng Customer
            val updatedCustomer = originalCustomer.copy(
                name = name,
                email = email,
                phone = phone,
                password = password
            )
            db.customerDao().updateCustomer(updatedCustomer)

            // 2. Cập nhật bảng User (nếu có userId hợp lệ)
            if (originalCustomer.userId > 0) {
                val userToUpdate = db.userDao().getUserByEmail(originalCustomer.email)
                if (userToUpdate != null) {
                    val updatedUser = userToUpdate.copy(
                        name = name,
                        email = email,
                        phone = phone,
                        password = password
                    )
                    db.userDao().update(updatedUser)
                }
            }

            Toast.makeText(this@ManHinhSuaKhach, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}