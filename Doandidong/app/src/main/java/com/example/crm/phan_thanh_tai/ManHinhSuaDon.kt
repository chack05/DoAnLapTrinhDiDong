package com.example.crm.phan_thanh_tai

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Customer
import com.example.crm.database.Order
import com.example.crm.database.OrderDetail
import com.example.crm.database.Product
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Lớp Activity quản lý màn hình chỉnh sửa đơn hàng
class ManHinhSuaDon : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var spinnerKhachHang: Spinner
    private lateinit var spinnerSanPham: Spinner
    private lateinit var edtSoLuong: EditText
    private lateinit var btnThemMon: Button
    private lateinit var rvGioHang: RecyclerView
    private lateinit var tvTongTien: TextView
    private lateinit var btnLuuDon: Button

    // --- Khai báo biến logic và dữ liệu ---
    private lateinit var db: AppDatabase
    private lateinit var gioHangAdapter: ChiTietDonHangAdapter // Giỏ hàng tạm thời để chỉnh sửa
    private val gioHangItems = mutableListOf<CartItem>()
    private var customerList: List<Customer> = listOf()
    private var productList: List<Product> = listOf()
    private var orderId: Int = -1 // ID của đơn hàng cần chỉnh sửa
    private var currentOrder: Order? = null // Đối tượng Order hiện tại

    // --- Hàm chính của Activity ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_sua_don)

        db = AppDatabase.getDatabase(this)
        orderId = intent.getIntExtra("ORDER_ID", -1) // Lấy ID đơn hàng từ Intent

        setControl()
        setupRecyclerView()
        loadSpinnersData() // Tải dữ liệu cho spinners trước
        setEvent()

        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng để chỉnh sửa.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // --- Các hàm cài đặt ---

    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "CHỈNH SỬA ĐƠN HÀNG" // Đặt tiêu đề cho màn hình
        // --- KẾT THÚC ĐỒNG BỘ ---

        spinnerKhachHang = findViewById(R.id.spinnerKhachHang)
        spinnerSanPham = findViewById(R.id.spinnerSanPham)
        edtSoLuong = findViewById(R.id.edtSoLuong)
        btnThemMon = findViewById(R.id.btnThemSanPhamVaoDon) // Nút "Cập nhật" món
        rvGioHang = findViewById(R.id.rvGioHang)
        tvTongTien = findViewById(R.id.tvTongTien)
        btnLuuDon = findViewById(R.id.btnLuuDon) // Nút "CẬP NHẬT ĐƠN HÀNG"
    }

    private fun setupRecyclerView() {
        gioHangAdapter = ChiTietDonHangAdapter(gioHangItems) { itemToDelete ->
            gioHangItems.remove(itemToDelete)
            gioHangAdapter.notifyDataSetChanged()
            updateTongTien()
        }
        rvGioHang.layoutManager = LinearLayoutManager(this)
        rvGioHang.adapter = gioHangAdapter
    }

    private fun loadSpinnersData() {
        lifecycleScope.launch {
            customerList = db.customerDao().getAllCustomers()
            productList = db.productDao().getAllProducts()

            val customerNames = customerList.map { it.name }
            val customerAdapter = ArrayAdapter(this@ManHinhSuaDon, android.R.layout.simple_spinner_item, customerNames)
            customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKhachHang.adapter = customerAdapter

            val productNames = productList.map { it.name }
            val productAdapter = ArrayAdapter(this@ManHinhSuaDon, android.R.layout.simple_spinner_item, productNames)
            productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSanPham.adapter = productAdapter

            // Sau khi spinners đã có dữ liệu, mới tải chi tiết đơn hàng để set selection
            if (orderId != -1) {
                loadOrderDetails()
            }
        }
    }

    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }
        // --- KẾT THÚC ĐỒNG BỘ ---

        btnThemMon.setOnClickListener { themMonVaoGioHang() }
        btnLuuDon.setOnClickListener { capNhatDonHang() }
    }

    // --- Các hàm xử lý Logic nghiệp vụ ---

    // Hàm tải thông tin đơn hàng và chi tiết sản phẩm để hiển thị lên form chỉnh sửa
    private suspend fun loadOrderDetails() {
        val order = db.orderDao().getOrderById(orderId)
        val orderDetails = db.orderDao().getOrderDetails(orderId)

        if (order == null) {
            Toast.makeText(this@ManHinhSuaDon, "Không tìm thấy đơn hàng này.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentOrder = order

        // Chọn khách hàng trên spinner
        val customerPosition = customerList.indexOfFirst { it.id == order.customerId }
        if (customerPosition != -1) {
            spinnerKhachHang.setSelection(customerPosition)
        }
        // Tạm thời khóa spinner khách hàng để tránh sửa đổi ID khách hàng của đơn hàng đã tồn tại
        spinnerKhachHang.isEnabled = false

        // Đổ dữ liệu chi tiết đơn hàng vào giỏ hàng tạm
        gioHangItems.clear()
        orderDetails.forEach { detail ->
            val product = productList.find { it.id == detail.productId }
            product?.let {
                gioHangItems.add(CartItem(it, detail.quantity))
            }
        }
        gioHangAdapter.notifyDataSetChanged()
        updateTongTien()
    }

    private fun themMonVaoGioHang() {
        if (productList.isEmpty()) {
            Toast.makeText(this, "Chưa có sản phẩm nào để thêm.", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedProduct = productList[spinnerSanPham.selectedItemPosition]
        val quantity = edtSoLuong.text.toString().toIntOrNull() ?: 1

        if (quantity <= 0) {
            Toast.makeText(this, "Số lượng phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
            return
        }

        gioHangAdapter.addItem(selectedProduct, quantity)
        updateTongTien()
    }

    private fun updateTongTien() {
        val total = gioHangAdapter.getTotalAmount()
        val formattedTotal = formatCurrency(total)
        tvTongTien.text = "Tổng tiền: $formattedTotal"
    }

    // Hàm xử lý cập nhật đơn hàng vào Database
    private fun capNhatDonHang() {
        val itemsInCart = gioHangAdapter.getItems()
        if (itemsInCart.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm sản phẩm vào đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Không cho phép thay đổi khách hàng đã đặt đơn hàng (vì spinnerKhachHang.isEnabled = false)
        val selectedCustomer = currentOrder?.let { customerList.find { cust -> cust.id == it.customerId } }
        if (selectedCustomer == null) {
            Toast.makeText(this, "Lỗi: Thông tin khách hàng không hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo đối tượng Order đã chỉnh sửa
        val updatedOrder = Order(
            id = orderId, // Giữ nguyên ID đơn hàng
            customerId = selectedCustomer.id,
            customerName = selectedCustomer.name,
            date = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date()), // Cập nhật ngày sửa thành ngày hiện tại
            totalAmount = gioHangAdapter.getTotalAmount()
        )

        lifecycleScope.launch {
            // 1. Cập nhật thông tin Order chính
            db.orderDao().updateOrder(updatedOrder)

            // 2. Xóa tất cả các chi tiết đơn hàng cũ
            db.orderDao().deleteOrderDetails(orderId)

            // 3. Chèn các chi tiết đơn hàng mới từ giỏ hàng tạm
            for (cartItem in itemsInCart) {
                val orderDetail = OrderDetail(
                    orderId = orderId,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    price = cartItem.product.price ?: 0.0,
                    total = cartItem.quantity * (cartItem.product.price ?: 0.0)
                )
                db.orderDao().insertOrderDetail(orderDetail)
            }

            Toast.makeText(this@ManHinhSuaDon, "Đơn hàng đã được cập nhật thành công!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }
}
