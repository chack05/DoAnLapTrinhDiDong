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

// Lớp Activity quản lý màn hình tạo đơn hàng mới
class ManHinhTaoDonHang : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var spinnerKhachHang: Spinner
    private lateinit var spinnerSanPham: Spinner
    private lateinit var edtSoLuong: EditText
    private lateinit var btnThemMon: Button // Đã đổi tên từ btnThemSanPhamVaoDon
    private lateinit var rvGioHang: RecyclerView // Đã đổi tên từ recyclerViewChiTietDonHang
    private lateinit var tvTongTien: TextView
    private lateinit var btnLuuDon: Button // Đã đổi tên từ btnLuuDonHang

    // --- Khai báo biến logic và dữ liệu ---
    private lateinit var db: AppDatabase
    private lateinit var gioHangAdapter: ChiTietDonHangAdapter // Adapter cho danh sách sản phẩm trong giỏ hàng
    private val gioHangItems = mutableListOf<CartItem>() // Danh sách các sản phẩm trong giỏ hàng tạm thời
    private var customerList: List<Customer> = listOf() // Danh sách khách hàng tải từ DB
    private var productList: List<Product> = listOf() // Danh sách sản phẩm tải từ DB

    // --- Hàm chính của Activity ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_man_hinh_tao_don_hang)

        db = AppDatabase.getDatabase(this)

        setControl()          // Ánh xạ các View
        setupRecyclerView()   // Cài đặt RecyclerView (giỏ hàng)
        loadSpinnersData()    // Tải dữ liệu cho các Spinner (khách hàng, sản phẩm)
        setEvent()            // Gán các sự kiện
    }

    // --- Các hàm cài đặt ---

    // Hàm ánh xạ các View từ layout XML vào biến trong code
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "TẠO ĐƠN HÀNG" // Đặt tiêu đề cho màn hình
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ các Spinner
        spinnerKhachHang = findViewById(R.id.spinnerKhachHang)
        spinnerSanPham = findViewById(R.id.spinnerSanPham)
        // Ánh xạ EditText số lượng và nút thêm vào đơn
        edtSoLuong = findViewById(R.id.edtSoLuong)
        btnThemMon = findViewById(R.id.btnThemSanPhamVaoDon) // Sử dụng ID cũ, nhưng biến đã đổi tên

        // Ánh xạ RecyclerView (giỏ hàng) và TextView tổng tiền
        rvGioHang = findViewById(R.id.rvGioHang) // Sử dụng ID mới
        tvTongTien = findViewById(R.id.tvTongTien)
        // Ánh xạ nút Lưu đơn hàng
        btnLuuDon = findViewById(R.id.btnLuuDon) // Sử dụng ID mới
    }

    // Hàm cài đặt RecyclerView cho danh sách chi tiết đơn hàng (giỏ hàng)
    private fun setupRecyclerView() {
        // Khởi tạo adapter, truyền vào danh sách giỏ hàng và một lambda để xử lý xóa item
        gioHangAdapter = ChiTietDonHangAdapter(gioHangItems) { itemToDelete ->
            gioHangItems.remove(itemToDelete) // Xóa item khỏi danh sách tạm thời
            gioHangAdapter.notifyDataSetChanged() // Cập nhật RecyclerView
            updateTongTien() // Cập nhật lại tổng tiền
        }
        rvGioHang.layoutManager = LinearLayoutManager(this) // Đặt LayoutManager
        rvGioHang.adapter = gioHangAdapter // Gán Adapter
    }

    // Hàm tải dữ liệu khách hàng và sản phẩm vào các Spinner
    private fun loadSpinnersData() {
        lifecycleScope.launch {
            // Tải danh sách khách hàng từ Database
            customerList = db.customerDao().getAllCustomers()
            // Tải danh sách sản phẩm từ Database
            productList = db.productDao().getAllProducts()

            // Cài đặt Adapter cho Spinner Khách hàng
            val customerNames = customerList.map { it.name }
            val customerAdapter = ArrayAdapter(this@ManHinhTaoDonHang, android.R.layout.simple_spinner_item, customerNames)
            customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerKhachHang.adapter = customerAdapter

            // Cài đặt Adapter cho Spinner Sản phẩm
            val productNames = productList.map { it.name }
            val productAdapter = ArrayAdapter(this@ManHinhTaoDonHang, android.R.layout.simple_spinner_item, productNames)
            productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSanPham.adapter = productAdapter
        }
    }

    // Hàm gán các sự kiện cho các View
    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Nhấn nút back thì đóng màn hình
        }
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Sự kiện click cho nút "Thêm món"
        btnThemMon.setOnClickListener {
            themMonVaoGioHang() // Gọi hàm xử lý thêm sản phẩm vào giỏ
        }

        // Sự kiện click cho nút "LƯU ĐƠN"
        btnLuuDon.setOnClickListener {
            luuDonHang() // Gọi hàm xử lý lưu đơn hàng vào DB
        }
    }

    // --- Các hàm xử lý Logic nghiệp vụ ---

    // Hàm xử lý khi nhấn nút "Thêm món"
    private fun themMonVaoGioHang() {
        // Kiểm tra xem có sản phẩm nào trong danh sách không
        if (productList.isEmpty()) {
            Toast.makeText(this, "Chưa có sản phẩm nào để thêm.", Toast.LENGTH_SHORT).show()
            return
        }
        // Lấy sản phẩm đang được chọn trên Spinner
        val selectedProduct = productList[spinnerSanPham.selectedItemPosition]
        // Lấy số lượng từ EditText, mặc định là 1 nếu không nhập hoặc nhập sai
        val quantity = edtSoLuong.text.toString().toIntOrNull() ?: 1

        if (quantity <= 0) {
            Toast.makeText(this, "Số lượng phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
            return
        }

        // Thêm sản phẩm vào giỏ hàng thông qua Adapter
        gioHangAdapter.addItem(selectedProduct, quantity)
        updateTongTien() // Cập nhật tổng tiền hiển thị
    }

    // Hàm cập nhật tổng tiền hiển thị trên TextView
    private fun updateTongTien() {
        val total = gioHangAdapter.getTotalAmount() // Lấy tổng tiền từ adapter
        val formattedTotal = formatCurrency(total) // Định dạng tiền tệ
        tvTongTien.text = "Tổng tiền: $formattedTotal" // Cập nhật TextView
    }

    // Hàm xử lý lưu toàn bộ đơn hàng vào Database
    private fun luuDonHang() {
        // Lấy danh sách các sản phẩm trong giỏ hàng
        val itemsInCart = gioHangAdapter.getItems()

        // Kiểm tra giỏ hàng có rỗng không
        if (itemsInCart.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm sản phẩm vào đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }
        // Kiểm tra có khách hàng nào được chọn không
        if (customerList.isEmpty()) {
            Toast.makeText(this, "Không có khách hàng nào để tạo đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy thông tin khách hàng được chọn
        val selectedCustomer = customerList[spinnerKhachHang.selectedItemPosition]

        // Tạo đối tượng Order
        val newOrder = Order(
            customerId = selectedCustomer.id,
            customerName = selectedCustomer.name, // Lưu tên khách hàng để tiện hiển thị
            date = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date()), // Định dạng ngày (đã đổi từ orderDate sang date)
            totalAmount = gioHangAdapter.getTotalAmount() // Tổng tiền từ giỏ hàng
        )

        lifecycleScope.launch {
            // Chèn Order vào database và lấy về ID của đơn hàng vừa tạo
            val newOrderId = db.orderDao().insertOrder(newOrder).toInt()

            // Duyệt qua từng sản phẩm trong giỏ hàng để chèn vào OrderDetail
            for (cartItem in itemsInCart) {
                val orderDetail = OrderDetail(
                    orderId = newOrderId,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    price = cartItem.product.price ?: 0.0, // Sử dụng 'price' thay vì 'unitPrice'
                    total = cartItem.quantity * (cartItem.product.price ?: 0.0) // Sử dụng 'total' thay vì 'totalPrice'
                )
                db.orderDao().insertOrderDetail(orderDetail) // Chèn chi tiết đơn hàng
            }

            // Thông báo thành công và đóng màn hình
            Toast.makeText(this@ManHinhTaoDonHang, "Đơn hàng đã được tạo thành công!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Hàm định dạng số Double thành chuỗi tiền tệ (ví dụ: 100.000 VNĐ)
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }
}
