package com.example.crm.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.crm.databinding.LayoutManHinhDashboardBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Activity cho màn hình Dashboard, kế thừa từ AppCompatActivity.
class ManHinhDashboard : AppCompatActivity() {

    // Khai báo biến binding để truy cập các view trong layout một cách an toàn.
    private lateinit var binding: LayoutManHinhDashboardBinding
    // Khởi tạo DashboardViewModel bằng cách sử dụng 'by viewModels()', một cách ngắn gọn và được khuyến nghị.
    private val viewModel: DashboardViewModel by viewModels()

    // Launcher để yêu cầu quyền ghi bộ nhớ (chỉ cho Android 10 trở xuống)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            xuatThongKeRaExcel() // Nếu được cấp quyền thì tiến hành xuất Excel
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để lưu file Excel.", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm được gọi khi Activity được tạo.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Gọi hàm onCreate của lớp cha.
        // Inflate layout và gán cho biến binding.
        binding = LayoutManHinhDashboardBinding.inflate(layoutInflater)
        // Thiết lập view gốc của activity là root của binding.
        setContentView(binding.root)

        // Gọi các hàm để thiết lập control và event.
        setControl() // Hàm để khởi tạo và ánh xạ các view.
        setEvent() // Hàm để lắng nghe các sự kiện và cập nhật UI.
    }

    // Hàm để khởi tạo và cấu hình các view.
    private fun setControl() {
        // Thiết lập tiêu đề cho toolbar.
        binding.toolbar.tvTieuDe.text = "Trang chủ Admin"
    }

    // Hàm để thiết lập các observer cho LiveData và xử lý sự kiện.
    private fun setEvent() {
        // Lắng nghe sự kiện click nút back trên toolbar để quay lại màn hình trước.
        binding.toolbar.btnBack.setOnClickListener {
            onBackPressed() // Gọi hàm quay lại mặc định.
        }

        // Sự kiện click cho nút xuất Excel
        binding.btnExportExcel.setOnClickListener {
            checkAndRequestPermissions()
        }

        // Quan sát LiveData 'totalCustomers' từ ViewModel.
        viewModel.totalCustomers.observe(this, Observer { count ->
            binding.tvTotalCustomers.text = count.toString()
        })

        // Quan sát LiveData 'totalProducts'.
        viewModel.totalProducts.observe(this, Observer { count ->
            binding.tvTotalProducts.text = count.toString()
        })

        // Quan sát LiveData 'totalCategories'.
        viewModel.totalCategories.observe(this, Observer { count ->
            binding.tvTotalCategories.text = count.toString()
        })

        // Quan sát LiveData 'totalOrders'.
        viewModel.totalOrders.observe(this, Observer { count ->
            binding.tvTotalOrders.text = count.toString()
        })

        // Quan sát LiveData 'approvedOrders'.
        viewModel.approvedOrders.observe(this, Observer { count ->
            binding.tvApprovedOrders.text = count.toString()
        })

        // Quan sát LiveData 'completedOrders'.
        viewModel.completedOrders.observe(this, Observer { count ->
            binding.tvCompletedOrders.text = count.toString()
        })

        // Quan sát LiveData 'cancelledOrders'.
        viewModel.cancelledOrders.observe(this, Observer { count ->
            binding.tvCancelledOrders.text = count.toString()
        })
    }

    // --- CHỨC NĂNG XUẤT FILE EXCEL ---

    // Hàm kiểm tra và xin quyền lưu trữ (nếu cần)
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Đối với Android 11 (API 30) trở lên, không cần xin quyền WRITE_EXTERNAL_STORAGE
            xuatThongKeRaExcel()
        } else {
            // Đối với Android 10 trở xuống, cần xin quyền WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                xuatThongKeRaExcel()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // Hàm xuất file Excel (HTML to XLS)
    private fun xuatThongKeRaExcel() {
        // 1. Tạo tên file: ThongKe_ngay_gio.xls
        val fileName = "ThongKe_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xls"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            // 2. Tạo nội dung HTML từ dữ liệu thống kê
            val sb = StringBuilder()
            sb.append("<html><head><meta charset=\"UTF-8\"><style>")
            sb.append("table { border-collapse: collapse; width: 50%; }")
            sb.append("th, td { border: 1px solid black; padding: 8px; text-align: left; }")
            sb.append("th { background-color: #007BFF; color: white; }") // Header Xanh dương
            sb.append("</style></head><body>")
            sb.append("<h2>Báo cáo Thống kê Nhanh</h2>")
            sb.append("<table>")
            sb.append("<tr><th>Mục Thống Kê</th><th>Số Lượng</th></tr>") // Tiêu đề bảng

            // 3. Lấy dữ liệu từ ViewModel và đổ vào bảng
            // Sử dụng .value để lấy giá trị hiện tại của LiveData, dùng '?: 0' để tránh lỗi null
            sb.append("<tr><td>Tổng số khách hàng</td><td>${viewModel.totalCustomers.value ?: 0}</td></tr>")
            sb.append("<tr><td>Tổng số sản phẩm</td><td>${viewModel.totalProducts.value ?: 0}</td></tr>")
            sb.append("<tr><td>Tổng số danh mục</td><td>${viewModel.totalCategories.value ?: 0}</td></tr>")
            sb.append("<tr><td>Tổng số hóa đơn</td><td>${viewModel.totalOrders.value ?: 0}</td></tr>")
            sb.append("<tr><td>Hóa đơn đã duyệt (đang giao)</td><td>${viewModel.approvedOrders.value ?: 0}</td></tr>")
            sb.append("<tr><td>Hóa đơn hoàn tất</td><td>${viewModel.completedOrders.value ?: 0}</td></tr>")
            sb.append("<tr><td>Hóa đơn bị hủy</td><td>${viewModel.cancelledOrders.value ?: 0}</td></tr>")

            sb.append("</table></body></html>")

            // 4. Lưu file đuôi .xls
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    writer.write(sb.toString())
                }
            }

            // 5. Toast thông báo đường dẫn.
            Toast.makeText(this, "Đã lưu Excel tại: ${file.absolutePath}", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi lưu file Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
