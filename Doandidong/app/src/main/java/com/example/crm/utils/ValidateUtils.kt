package com.example.crm.utils

import android.util.Patterns // Import Patterns để sử dụng regex cho email

// Đối tượng tiện ích chứa các hàm kiểm tra tính hợp lệ của dữ liệu đầu vào
object ValidateUtils {

    // Hàm kiểm tra định dạng email
    // Sử dụng Patterns.EMAIL_ADDRESS của Android để kiểm tra email theo định dạng chuẩn
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Hàm kiểm tra định dạng số điện thoại
    // Yêu cầu: 10 chữ số, bắt đầu bằng số 0
    fun isValidPhone(phone: String): Boolean {
        // Regex: ^0[0-9]{9}$
        // ^   : Bắt đầu chuỗi
        // 0   : Ký tự đầu tiên phải là số 0
        // [0-9]{9} : Tiếp theo là 9 chữ số từ 0 đến 9
        // $   : Kết thúc chuỗi
        return phone.matches(Regex("^0[0-9]{9}$"))
    }

    // Hàm kiểm tra định dạng mã (ví dụ: mã khách hàng, mã sản phẩm)
    // Yêu cầu: Chỉ chứa chữ cái (hoa/thường) và số, không có ký tự đặc biệt
    fun isValidCode(code: String): Boolean {
        // Regex: ^[a-zA-Z0-9]+$
        // ^   : Bắt đầu chuỗi
        // [a-zA-Z0-9]+ : Một hoặc nhiều ký tự là chữ cái (hoa/thường) hoặc chữ số
        // $   : Kết thúc chuỗi
        return code.matches(Regex("^[a-zA-Z0-9]+$"))
    }
}
