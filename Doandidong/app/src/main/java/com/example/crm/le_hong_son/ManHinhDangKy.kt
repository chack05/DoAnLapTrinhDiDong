package com.example.crm.le_hong_son

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Customer
import com.example.crm.database.User
import kotlinx.coroutines.launch

/**
 * Màn hình đăng ký.
 * File này được ghi đè để đảm bảo logic đồng bộ User -> Customer
 * được thực thi với phiên bản code mới nhất, tránh lỗi cache.
 */
class ManHinhDangKy : AppCompatActivity() {

    private lateinit var edtHoTen: EditText
    private lateinit var edtSDT: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtMatKhau: EditText
    private lateinit var btnDangKy: Button
    private lateinit var tvQuayLaiDangNhap: TextView
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_dang_ky)
        appDatabase = AppDatabase.getDatabase(this)
        setControl()
        setEvent()
    }

    private fun setControl() {
        edtHoTen = findViewById(R.id.edtHoTen)
        edtSDT = findViewById(R.id.edtSDT)
        edtEmail = findViewById(R.id.edtEmail)
        edtMatKhau = findViewById(R.id.edtMatKhau)
        btnDangKy = findViewById(R.id.btnDangKy)
        tvQuayLaiDangNhap = findViewById(R.id.tvQuayLaiDangNhap)
    }

    // Ghi đè lại toàn bộ hàm setEvent để đảm bảo không có lỗi tiềm ẩn
    private fun setEvent() {
        tvQuayLaiDangNhap.setOnClickListener {
            finish()
        }

        btnDangKy.setOnClickListener {
            val name = edtHoTen.text.toString().trim()
            val phone = edtSDT.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val pass = edtMatKhau.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    Log.d("REG_FLOW", "Bắt đầu tiến trình đăng ký cho email: $email")

                    // Bước 1: Kiểm tra email tồn tại
                    if (appDatabase.userDao().checkEmailExist(email) > 0) {
                        Toast.makeText(this@ManHinhDangKy, "Email này đã được đăng ký.", Toast.LENGTH_SHORT).show()
                        Log.w("REG_FLOW", "Email đã tồn tại. Dừng tiến trình.")
                        return@launch
                    }

                    // Bước 2: Thêm User
                    Log.d("REG_FLOW", "Đang chèn User...")
                    val newUser = User(name = name, email = email, password = pass, phone = phone)
                    appDatabase.userDao().insert(newUser)
                    Log.d("REG_FLOW", "Đã chèn User thành công.")

                    // Bước 3: Lấy lại User để có ID
                    Log.d("REG_FLOW", "Đang lấy lại User theo email...")
                    val insertedUser = appDatabase.userDao().getUserByEmail(email)
                    if (insertedUser == null) {
                        Toast.makeText(this@ManHinhDangKy, "Lỗi nghiêm trọng: Không thể lấy lại user sau khi chèn.", Toast.LENGTH_SHORT).show()
                        Log.e("REG_FLOW", "getUserByEmail trả về null.")
                        return@launch
                    }
                    val newUserId = insertedUser.id
                    Log.d("REG_FLOW", "Lấy được userId: $newUserId")

                    // Bước 4: Tạo Customer
                    val autoCode = "KH${System.currentTimeMillis() % 100000}"
                    val newCustomer = Customer(
                        code = autoCode,
                        name = name,
                        phone = phone,
                        email = email,
                        password = pass,
                        imagePath = null,
                        userId = newUserId
                    )
                    Log.d("REG_FLOW", "Đã tạo đối tượng Customer: $newCustomer")

                    // Bước 5: Thêm Customer
                    Log.d("REG_FLOW", "Đang chèn Customer...")
                    appDatabase.customerDao().insert(newCustomer)
                    Log.d("REG_FLOW", "Đã chèn Customer thành công.")

                    // Bước 6: Hoàn tất
                    Toast.makeText(this@ManHinhDangKy, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    Log.i("REG_FLOW", "Hoàn tất tiến trình đăng ký.")
                    finish()

                } catch (e: Exception) {
                    Log.e("REG_FLOW", "Lỗi nghiêm trọng xảy ra trong quá trình đăng ký!", e)
                    Toast.makeText(this@ManHinhDangKy, "Lỗi đăng ký: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
