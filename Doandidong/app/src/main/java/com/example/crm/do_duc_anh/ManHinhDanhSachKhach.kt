package com.example.crm.do_duc_anh

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton // Import ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Customer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Khai báo lớp ManHinhDanhSachKhach
class ManHinhDanhSachKhach : AppCompatActivity() {

    // --- Khai báo các biến cho View và logic ---
    private lateinit var tvTongSoLuong: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTaoKhachHangMoi: FloatingActionButton
    private lateinit var customerAdapter: CustomerAdapter
    private lateinit var db: AppDatabase
    private var allCustomers: List<Customer> = emptyList() // Danh sách tất cả khách hàng (không lọc)
    private lateinit var edtTimKiemKhach: EditText
    private lateinit var btnXuatExcel: ImageButton // Đã đổi thành ImageButton

    // Launcher để yêu cầu quyền ghi bộ nhớ (chỉ cho Android 10 trở xuống)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            xuatFileExcel() // Nếu được cấp quyền thì tiến hành xuất Excel
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để lưu file Excel.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_man_hinh_danh_sach_khach)

        db = AppDatabase.getDatabase(this)
        setControl()
        setEvent()
    }

    override fun onResume() {
        super.onResume()
        loadAllCustomers()
    }

    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "QUẢN LÝ KHÁCH HÀNG"
        // Ánh xạ nút xuất Excel và làm cho nó hiển thị
        btnXuatExcel = header.findViewById(R.id.btnXuatExcel) // Ánh xạ ImageButton từ toolbar
        btnXuatExcel.visibility = View.VISIBLE // Làm cho nút hiển thị
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ các view khác
        tvTongSoLuong = findViewById(R.id.tvTongSoLuong)
        recyclerView = findViewById(R.id.recyclerViewKhachHang)
        fabTaoKhachHangMoi = findViewById(R.id.fabTaoKhachHangMoi)
        edtTimKiemKhach = findViewById(R.id.edtTimKiemKhach)
    }

    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        // Gán sự kiện click cho nút xuất Excel (từ toolbar)
        btnXuatExcel.setOnClickListener {
            checkAndRequestPermissions() // Kiểm tra quyền trước khi xuất Excel
        }
        // --- KẾT THÚC ĐỒNG BỘ ---

        fabTaoKhachHangMoi.setOnClickListener {
            val intent = Intent(this, ManHinhThemKhach::class.java)
            startActivity(intent)
        }

        edtTimKiemKhach.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchCustomers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Hàm tải toàn bộ danh sách khách hàng từ DB
    private fun loadAllCustomers() {
        lifecycleScope.launch {
            allCustomers = db.customerDao().getAllCustomers()
            tvTongSoLuong.text = "Tổng số lượng: ${allCustomers.size}"
            searchCustomers(edtTimKiemKhach.text.toString())
        }
    }

    // Hàm tìm kiếm khách hàng
    private fun searchCustomers(query: String) {
        lifecycleScope.launch {
            val filteredList = if (query.isEmpty()) {
                allCustomers
            } else {
                db.customerDao().searchCustomer(query)
            }
            if (!::customerAdapter.isInitialized) {
                customerAdapter = CustomerAdapter(
                    filteredList,
                    onEditClick = { customer ->
                        val intent = Intent(this@ManHinhDanhSachKhach, ManHinhSuaKhach::class.java)
                        intent.putExtra("CUSTOMER_ID", customer.id)
                        startActivity(intent)
                    },
                    onDeleteClick = { customer ->
                        showDeleteConfirmationDialog(customer)
                    }
                )
                recyclerView.layoutManager = LinearLayoutManager(this@ManHinhDanhSachKhach)
                recyclerView.adapter = customerAdapter
            } else {
                customerAdapter.updateData(filteredList)
            }
            tvTongSoLuong.text = "Tổng số lượng: ${filteredList.size}"
        }
    }

    // Hàm hiển thị hộp thoại xác nhận xóa
    private fun showDeleteConfirmationDialog(customer: Customer) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa?")
            .setMessage("Xóa vĩnh viễn khách hàng [${customer.name}] không thể phục hồi.")
            .setPositiveButton("Đồng ý xóa") { _, _ ->
                lifecycleScope.launch {
                    db.customerDao().deleteCustomer(customer)
                    loadAllCustomers()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // --- BỔ SUNG: CHỨC NĂNG XUẤT FILE EXCEL (HTML to XLS) ---

    // Hàm kiểm tra và xin quyền lưu trữ (nếu cần)
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Đối với Android 11 (API 30) trở lên, không cần xin quyền WRITE_EXTERNAL_STORAGE
            xuatFileExcel()
        } else {
            // Đối với Android 10 trở xuống, cần xin quyền WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                xuatFileExcel()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // Hàm xuất file Excel (HTML to XLS)
    private fun xuatFileExcel() {
        lifecycleScope.launch {
            val customersToExport = db.customerDao().getAllCustomers() // Lấy tất cả khách hàng hiện có

            if (customersToExport.isEmpty()) {
                Toast.makeText(this@ManHinhDanhSachKhach, "Không có dữ liệu khách hàng để xuất Excel.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 1. Tạo tên file: DanhSachKhachHang_ngay_gio.xls
            val fileName = "DanhSachKhachHang_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xls"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)

            try {
                // 2. Tạo nội dung HTML
                val sb = StringBuilder()
                sb.append("<html><head><meta charset=\"UTF-8\"><style>") // Thêm charset UTF-8
                sb.append("table { border-collapse: collapse; width: 100%; }")
                sb.append("th, td { border: 1px solid black; padding: 8px; text-align: left; }")
                sb.append("th { background-color: #4CAF50; color: white; }") // Header Xanh lá
                sb.append("</style></head><body>")

                sb.append("<table>")
                sb.append("<tr><th>Mã KH</th><th>Tên Khách Hàng</th><th>SĐT</th><th>Email</th></tr>") // Tiêu đề

                // 3. Duyệt danh sách đổ vào <tr><td>
                for (kh in customersToExport) {
                    sb.append("<tr>")
                    sb.append("<td>${kh.code}</td>")
                    sb.append("<td>${kh.name}</td>")
                    sb.append("<td>${kh.phone}</td>")
                    sb.append("<td>${kh.email}</td>")
                    sb.append("</tr>")
                }
                sb.append("</table></body></html>")

                // 4. Lưu file đuôi .xls
                FileOutputStream(file).use { fos ->
                    OutputStreamWriter(fos, Charsets.UTF_8).use { writer -> // Sử dụng UTF-8
                        writer.write(sb.toString())
                    }
                }

                // 5. Toast thông báo đường dẫn.
                Toast.makeText(this@ManHinhDanhSachKhach, "Đã lưu Excel tại: ${file.absolutePath}", Toast.LENGTH_LONG).show()

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@ManHinhDanhSachKhach, "Lỗi khi lưu file Excel: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
