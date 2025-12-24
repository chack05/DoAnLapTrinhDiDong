package com.example.crm.phan_thanh_tai

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout // Import LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // Import ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Order
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.util.Locale

// Lớp Activity quản lý màn hình hiển thị chi tiết một đơn hàng
class ManHinhChiTietDon : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var tvChiTietOrderId: TextView
    private lateinit var tvChiTietCustomerName: TextView
    private lateinit var tvChiTietOrderDate: TextView
    private lateinit var tvChiTietTotalAmount: TextView
    private lateinit var tvChiTietOrderStatus: TextView // TextView hiển thị trạng thái đơn hàng
    private lateinit var rvChiTietSanPhamTrongDon: RecyclerView
    private lateinit var btnXoaDon: Button
    private lateinit var btnSuaDon: Button
    private lateinit var btnInHoaDon: Button // Khai báo nút in hóa đơn

    private lateinit var layoutAdminActions: LinearLayout // Layout chứa các nút duyệt/hoàn tất/hủy
    private lateinit var btnDuyetDon: Button
    private lateinit var btnHoanTatDon: Button
    private lateinit var btnHuyDon: Button


    // --- Khai báo biến logic ---
    private lateinit var db: AppDatabase
    private lateinit var orderDetailSummaryAdapter: OrderDetailSummaryAdapter
    private var orderId: Int = -1 // ID của đơn hàng cần hiển thị chi tiết
    private var currentOrder: Order? = null // Lưu đối tượng Order hiện tại

    // Launcher để yêu cầu quyền ghi bộ nhớ (chỉ cho Android 10 trở xuống)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            xuatHoaDonPDF() // Nếu được cấp quyền thì tiến hành xuất PDF
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để lưu file PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_chi_tiet_don)

        db = AppDatabase.getDatabase(this)
        orderId = intent.getIntExtra("ORDER_ID", -1) // Lấy orderId từ Intent

        setControl()
        setEvent()
        setupRecyclerView()

        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadOrderDetails() // Tải dữ liệu chi tiết đơn hàng
        }
    }

    // --- Các hàm cài đặt ---

    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "CHI TIẾT ĐƠN HÀNG" // Đặt tiêu đề cho màn hình
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ các TextView hiển thị thông tin chung
        tvChiTietOrderId = findViewById(R.id.tvChiTietOrderId)
        tvChiTietCustomerName = findViewById(R.id.tvChiTietCustomerName)
        tvChiTietOrderDate = findViewById(R.id.tvChiTietOrderDate)
        tvChiTietTotalAmount = findViewById(R.id.tvChiTietTotalAmount)
        tvChiTietOrderStatus = findViewById(R.id.tvChiTietOrderStatus) // Ánh xạ TextView trạng thái

        // Ánh xạ RecyclerView và nút xóa, sửa, in PDF
        rvChiTietSanPhamTrongDon = findViewById(R.id.rvChiTietSanPhamTrongDon)
        btnXoaDon = findViewById(R.id.btnXoaDon)
        btnSuaDon = findViewById(R.id.btnSuaDon)
        btnInHoaDon = findViewById(R.id.btnInHoaDon) // Ánh xạ nút in hóa đơn

        // Ánh xạ các View cho chức năng admin
        layoutAdminActions = findViewById(R.id.layoutAdminActions)
        btnDuyetDon = findViewById(R.id.btnDuyetDon)
        btnHoanTatDon = findViewById(R.id.btnHoanTatDon)
        btnHuyDon = findViewById(R.id.btnHuyDon)

        // Vì màn hình này là dành riêng cho Admin, tất cả các nút quản lý sẽ luôn hiển thị.
        btnInHoaDon.visibility = View.VISIBLE
        btnXoaDon.visibility = View.VISIBLE
        btnSuaDon.visibility = View.VISIBLE
        layoutAdminActions.visibility = View.VISIBLE // Các nút trạng thái cũng luôn hiển thị cho admin
    }
    private fun setupRecyclerView() {
        orderDetailSummaryAdapter = OrderDetailSummaryAdapter(emptyList()) // Khởi tạo adapter với danh sách rỗng
        rvChiTietSanPhamTrongDon.layoutManager = LinearLayoutManager(this)
        rvChiTietSanPhamTrongDon.adapter = orderDetailSummaryAdapter
    }

    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Nhấn nút back thì đóng màn hình
        }
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Gán sự kiện click cho nút xóa đơn hàng
        btnXoaDon.setOnClickListener {
            currentOrder?.let { showDeleteConfirmationDialog(it) } // Chỉ xóa khi có Order hiện tại
        }

        // Gán sự kiện click cho nút sửa đơn hàng
        btnSuaDon.setOnClickListener {
            val intent = Intent(this, ManHinhSuaDon::class.java) // Chuyển sang màn hình sửa
            intent.putExtra("ORDER_ID", orderId) // Truyền ID đơn hàng
            startActivity(intent)
        }

        // Gán sự kiện click cho nút in hóa đơn (chỉ admin mới thấy)
        btnInHoaDon.setOnClickListener {
            checkAndRequestPermissions() // Kiểm tra quyền trước khi xuất PDF
        }

        // Gán sự kiện cho các nút cập nhật trạng thái (chỉ admin mới thấy)
        btnDuyetDon.setOnClickListener {
            updateOrderStatus(currentOrder?.id ?: -1, 1) // 1: Đang giao
        }
        btnHoanTatDon.setOnClickListener {
            updateOrderStatus(currentOrder?.id ?: -1, 2) // 2: Thành công
        }
        btnHuyDon.setOnClickListener {
            updateOrderStatus(currentOrder?.id ?: -1, 3) // 3: Đã hủy
        }
    }

    // --- Các hàm xử lý Logic nghiệp vụ ---

    // Hàm kiểm tra xem người dùng hiện tại có phải là Admin không
    private fun isAdmin(): Boolean {
        val sharedPreferences = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("EMAIL", null)
        // Giả định email admin là "admin@gmail.com". Có thể mở rộng bằng cách lưu role trong SharedPreferences.
        return userEmail == "admin@gmail.com"
    }

    // Hàm tải thông tin chi tiết của đơn hàng
    private fun loadOrderDetails() {
        lifecycleScope.launch {
            // Lấy Order và OrderDetails từ database
            val order = db.orderDao().getOrderById(orderId)
            val orderDetails = db.orderDao().getOrderDetails(orderId)

            if (order == null) {
                Toast.makeText(this@ManHinhChiTietDon, "Không tìm thấy đơn hàng này.", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            currentOrder = order

            // Cập nhật thông tin chung của đơn hàng
            tvChiTietOrderId.text = "Mã đơn hàng: #${order.id}"
            tvChiTietCustomerName.text = "Khách hàng: ${order.customerName}"
            tvChiTietOrderDate.text = "Ngày đặt: ${order.date}"
            tvChiTietTotalAmount.text = formatCurrency(order.totalAmount)

            // Hiển thị trạng thái đơn hàng
            displayOrderStatus(order.status, tvChiTietOrderStatus)

            // Cập nhật dữ liệu cho RecyclerView chi tiết sản phẩm
            orderDetailSummaryAdapter.updateData(orderDetails)

            // Điều chỉnh khả năng tương tác của các nút cập nhật trạng thái
            if (isAdmin()) {
                when (order.status) {
                    0 -> { // Chờ xác nhận
                        btnDuyetDon.isEnabled = true
                        btnHoanTatDon.isEnabled = false
                        btnHuyDon.isEnabled = true
                    }
                    1 -> { // Đang giao
                        btnDuyetDon.isEnabled = false
                        btnHoanTatDon.isEnabled = true
                        btnHuyDon.isEnabled = true
                    }
                    2 -> { // Thành công
                        btnDuyetDon.isEnabled = false
                        btnHoanTatDon.isEnabled = false
                        btnHuyDon.isEnabled = false // Không hủy được đơn đã thành công
                    }
                    3 -> { // Đã hủy
                        btnDuyetDon.isEnabled = false
                        btnHoanTatDon.isEnabled = false
                        btnHuyDon.isEnabled = false
                    }
                }
            }
        }
    }

    // Hàm cập nhật trạng thái đơn hàng
    private fun updateOrderStatus(orderId: Int, newStatus: Int) {
        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không thể cập nhật trạng thái đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            db.orderDao().updateOrderStatus(orderId, newStatus)
            Toast.makeText(this@ManHinhChiTietDon, "Đã cập nhật trạng thái đơn hàng!", Toast.LENGTH_SHORT).show()
            loadOrderDetails() // Tải lại chi tiết để cập nhật giao diện
        }
    }


    // Hàm hiển thị hộp thoại xác nhận xóa đơn hàng
    private fun showDeleteConfirmationDialog(order: Order) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa đơn hàng")
            .setMessage("Bạn có chắc chắn muốn xóa đơn hàng #${order.id} không? Thao tác này không thể hoàn tác.")
            .setPositiveButton("Xóa") { _, _ ->
                lifecycleScope.launch {
                    db.orderDao().deleteOrderDetails(order.id)
                    db.orderDao().deleteOrder(order)
                    Toast.makeText(this@ManHinhChiTietDon, "Đơn hàng #${order.id} đã được xóa.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Hàm định dạng tiền tệ
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }

    // Hàm tiện ích để hiển thị trạng thái và màu sắc
    private fun displayOrderStatus(status: Int, textView: TextView) {
        when (status) {
            0 -> { // Chờ xác nhận
                textView.text = "Trạng thái: 🔴 Chờ xác nhận"
                textView.setTextColor(ContextCompat.getColor(this, R.color.orange_status))
            }
            1 -> { // Đang giao
                textView.text = "Trạng thái: 🔵 Đang giao"
                textView.setTextColor(ContextCompat.getColor(this, R.color.blue_status))
            }
            2 -> { // Thành công
                textView.text = "Trạng thái: 🟢 Thành công"
                textView.setTextColor(ContextCompat.getColor(this, R.color.green_status))
            }
            3 -> { // Đã hủy
                textView.text = "Trạng thái: ⚫ Đã hủy"
                textView.setTextColor(ContextCompat.getColor(this, R.color.red_status))
            }
            else -> { // Trạng thái không xác định
                textView.text = "Trạng thái: Không xác định"
                textView.setTextColor(ContextCompat.getColor(this, R.color.gray_status))
            }
        }
    }


    // --- BỔ SUNG: CHỨC NĂNG XUẤT HÓA ĐƠN PDF ---

    // Hàm kiểm tra và xin quyền lưu trữ (nếu cần)
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Đối với Android 11 (API 30) trở lên, không cần xin quyền WRITE_EXTERNAL_STORAGE
            // Có thể ghi trực tiếp vào thư mục Downloads
            xuatHoaDonPDF()
        } else {
            // Đối với Android 10 trở xuống, cần xin quyền WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                xuatHoaDonPDF()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // Hàm xuất hóa đơn PDF
    private fun xuatHoaDonPDF() {
        // Kiểm tra xem đã có dữ liệu đơn hàng chưa
        val order = currentOrder
        val orderDetails = orderDetailSummaryAdapter.getCurrentList()
        if (order == null || orderDetails.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu đơn hàng để tạo hóa đơn.", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument() // Tạo một đối tượng PdfDocument mới
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Khổ giấy A4 (595x842 pts), trang 1
        val page = pdfDocument.startPage(pageInfo) // Bắt đầu trang mới
        val canvas = page.canvas // Lấy Canvas của trang để vẽ
        val paint = Paint() // Tạo đối tượng Paint để định dạng text và hình vẽ

        var y = 50f // Tọa độ Y bắt đầu vẽ
        val xMargin = 40f // Lề trái/phải

        // 1. HEADER: HÓA ĐƠN BÁN LẺ
        paint.textSize = 24f
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER // Canh giữa text
        canvas.drawText("HÓA ĐƠN BÁN LẺ", canvas.width / 2f, y, paint)
        y += 40f
        paint.textAlign = Paint.Align.LEFT // Trả lại canh trái

        // 2. THÔNG TIN CHUNG ĐƠN HÀNG
        paint.textSize = 14f
        canvas.drawText("Mã đơn: #${order.id}", xMargin, y, paint)
        y += 20f
        canvas.drawText("Khách hàng: ${order.customerName}", xMargin, y, paint)
        y += 20f
        canvas.drawText("Ngày mua: ${order.date}", xMargin, y, paint)
        y += 30f

        // 3. HEADER BẢNG SẢN PHẨM
        paint.textSize = 14f
        paint.color = Color.DKGRAY
        canvas.drawText("Sản phẩm", xMargin, y, paint)
        canvas.drawText("SL", xMargin + 250f, y, paint) // Vị trí cho Số lượng
        canvas.drawText("Đơn giá", xMargin + 320f, y, paint) // Vị trí cho Đơn giá
        paint.textAlign = Paint.Align.RIGHT // Canh phải cho Thành tiền
        canvas.drawText("Thành tiền", canvas.width - xMargin, y, paint)
        paint.textAlign = Paint.Align.LEFT // Trả lại canh trái
        y += 20f

        // Vẽ đường kẻ ngang dưới header bảng
        canvas.drawLine(xMargin, y, canvas.width - xMargin, y, paint)
        y += 10f

        // 4. DANH SÁCH SẢN PHẨM TRONG ĐƠN
        paint.textSize = 12f
        paint.color = Color.BLACK
        orderDetails.forEach { detail ->
            canvas.drawText(detail.productName, xMargin, y, paint)
            canvas.drawText("${detail.quantity}", xMargin + 250f, y, paint)
            canvas.drawText(formatCurrency(detail.price), xMargin + 320f, y, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formatCurrency(detail.total), canvas.width - xMargin, y, paint)
            paint.textAlign = Paint.Align.LEFT
            y += 20f
        }
        y += 10f

        // 5. FOOTER: TỔNG TIỀN
        // Vẽ đường kẻ ngang trên tổng tiền
        paint.color = Color.DKGRAY
        canvas.drawLine(xMargin, y, canvas.width - xMargin, y, paint)
        y += 20f

        paint.textSize = 18f
        paint.isFakeBoldText = true // In đậm
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TỔNG TIỀN: ${formatCurrency(order.totalAmount)}", canvas.width - xMargin, y, paint)
        y += 40f
        
        // Lời cảm ơn
        paint.textSize = 12f
        paint.isFakeBoldText = false // Bỏ in đậm
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Cảm ơn quý khách đã mua hàng!", canvas.width / 2f, y, paint)

        // Kết thúc trang và tài liệu PDF
        pdfDocument.finishPage(page)

        // LƯU FILE PDF
        val fileName = "HoaDon_DH${order.id}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "Đã lưu hóa đơn tại: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Lỗi khi lưu hóa đơn: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close() // Luôn đóng tài liệu PDF
        }
    }
}
