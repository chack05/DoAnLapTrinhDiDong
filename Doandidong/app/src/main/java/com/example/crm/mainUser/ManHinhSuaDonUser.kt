package com.example.crm.mainUser

import android.os.Bundle
import android.view.View
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
import com.example.crm.database.Order
import com.example.crm.database.OrderDetail
import com.example.crm.database.Product
import com.example.crm.phan_thanh_tai.ChiTietDonHangAdapter
import com.example.crm.phan_thanh_tai.CartItem
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Lớp Activity quản lý màn hình chỉnh sửa đơn hàng dành riêng cho người dùng
class ManHinhSuaDonUser : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var tvCustomerNameUserEdit: TextView // TextView hiển thị tên khách hàng
    private lateinit var spinnerSanPhamUserEdit: Spinner    // Spinner chọn sản phẩm
    private lateinit var edtSoLuongUserEdit: EditText      // EditText nhập số lượng
    private lateinit var btnThemSanPhamVaoDonUserEdit: Button // Nút thêm/cập nhật sản phẩm vào giỏ
    private lateinit var rvGioHangUserEdit: RecyclerView   // RecyclerView hiển thị sản phẩm trong giỏ
    private lateinit var tvTongTienUserEdit: TextView      // TextView hiển thị tổng tiền
    private lateinit var btnLuuDonUserEdit: Button         // Nút lưu/cập nhật đơn hàng

    // --- Khai báo biến logic và dữ liệu ---
    private lateinit var db: AppDatabase
    private lateinit var gioHangAdapter: ChiTietDonHangAdapter // Adapter cho RecyclerView giỏ hàng
    private val gioHangItems = mutableListOf<CartItem>() // Danh sách các sản phẩm trong giỏ hàng tạm thời
    private var productList: List<Product> = listOf() // Danh sách tất cả sản phẩm
    private var orderId: Int = -1 // ID của đơn hàng cần chỉnh sửa
    private var currentOrder: Order? = null // Đối tượng Order hiện tại đang chỉnh sửa

    // --- Hàm chính của Activity ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_sua_don_user) // Sử dụng layout riêng cho user

        db = AppDatabase.getDatabase(this)
        orderId = intent.getIntExtra("ORDER_ID", -1) // Lấy ID đơn hàng từ Intent

        setControl()          // Ánh xạ các View
        setupRecyclerView()   // Cài đặt RecyclerView
        loadProductSpinnerData() // Tải dữ liệu cho spinner sản phẩm
        setEvent()            // Gán các sự kiện

        // Nếu không tìm thấy ID đơn hàng, thông báo lỗi và đóng màn hình
        if (orderId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy đơn hàng để chỉnh sửa.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // --- Các hàm cài đặt ---

    /**
     * Hàm ánh xạ các View từ file layout XML vào biến trong code.
     */
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "CHỈNH SỬA ĐƠN HÀNG CỦA BẠN" // Đặt tiêu đề cho màn hình user
        // Nút Back trên toolbar
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ các View cụ thể cho chỉnh sửa đơn hàng user
        tvCustomerNameUserEdit = findViewById(R.id.tvCustomerNameUserEdit)
        spinnerSanPhamUserEdit = findViewById(R.id.spinnerSanPhamUserEdit)
        edtSoLuongUserEdit = findViewById(R.id.edtSoLuongUserEdit)
        btnThemSanPhamVaoDonUserEdit = findViewById(R.id.btnThemSanPhamVaoDonUserEdit)
        rvGioHangUserEdit = findViewById(R.id.rvGioHangUserEdit)
        tvTongTienUserEdit = findViewById(R.id.tvTongTienUserEdit)
        btnLuuDonUserEdit = findViewById(R.id.btnLuuDonUserEdit)
    }

    /**
     * Hàm cài đặt RecyclerView và Adapter cho danh sách sản phẩm trong giỏ hàng.
     */
    private fun setupRecyclerView() {
        // Khởi tạo adapter với danh sách sản phẩm trong giỏ hàng tạm thời
        gioHangAdapter = ChiTietDonHangAdapter(gioHangItems) { itemToDelete ->
            gioHangItems.remove(itemToDelete) // Xóa item khỏi danh sách
            gioHangAdapter.notifyDataSetChanged() // Thông báo cho adapter cập nhật UI
            updateTongTien() // Cập nhật lại tổng tiền
        }
        rvGioHangUserEdit.layoutManager = LinearLayoutManager(this) // Đặt LayoutManager
        rvGioHangUserEdit.adapter = gioHangAdapter // Gán Adapter
    }

    /**
     * Hàm tải dữ liệu sản phẩm cho Spinner chọn sản phẩm.
     * Sau khi tải xong, sẽ tải chi tiết đơn hàng để điền vào form.
     */
    private fun loadProductSpinnerData() {
        lifecycleScope.launch {
            productList = db.productDao().getAllProducts() // Lấy tất cả sản phẩm từ DB

            // Tạo ArrayAdapter cho spinner sản phẩm
            val productNames = productList.map { it.name }
            val productAdapter = ArrayAdapter(this@ManHinhSuaDonUser, android.R.layout.simple_spinner_item, productNames)
            productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSanPhamUserEdit.adapter = productAdapter

            // Sau khi spinner đã có dữ liệu, tải chi tiết đơn hàng
            if (orderId != -1) {
                loadOrderDetails()
            }
        }
    }

    /**
     * Hàm gán các sự kiện cho các View.
     */
    private fun setEvent() {
        // Sự kiện click cho nút "Cập nhật sản phẩm"
        btnThemSanPhamVaoDonUserEdit.setOnClickListener { themMonVaoGioHang() }
        // Sự kiện click cho nút "Lưu đơn hàng"
        btnLuuDonUserEdit.setOnClickListener { capNhatDonHang() }
    }

    // --- Các hàm xử lý Logic nghiệp vụ ---

    /**
     * Hàm tải thông tin chi tiết của đơn hàng cần chỉnh sửa từ database
     * và hiển thị lên form.
     */
    private suspend fun loadOrderDetails() {
        val order = db.orderDao().getOrderById(orderId)
        val orderDetails = db.orderDao().getOrderDetails(orderId)

        // Kiểm tra nếu không tìm thấy đơn hàng
        if (order == null) {
            Toast.makeText(this@ManHinhSuaDonUser, "Không tìm thấy đơn hàng này.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentOrder = order // Gán đơn hàng hiện tại

        // Hiển thị tên khách hàng (không cho user sửa đổi)
        tvCustomerNameUserEdit.text = "Khách hàng: ${order.customerName}"

        // Đổ dữ liệu chi tiết đơn hàng vào giỏ hàng tạm để user chỉnh sửa
        gioHangItems.clear() // Xóa các mục cũ
        orderDetails.forEach { detail ->
            val product = productList.find { it.id == detail.productId } // Tìm sản phẩm tương ứng
            product?.let {
                // Thêm sản phẩm vào giỏ hàng tạm
                gioHangItems.add(CartItem(it, detail.quantity))
            }
        }
        gioHangAdapter.notifyDataSetChanged() // Cập nhật RecyclerView
        updateTongTien() // Cập nhật tổng tiền
    }

    /**
     * Hàm thêm hoặc cập nhật sản phẩm vào giỏ hàng tạm thời.
     */
    private fun themMonVaoGioHang() {
        // Kiểm tra danh sách sản phẩm rỗng
        if (productList.isEmpty()) {
            Toast.makeText(this, "Chưa có sản phẩm nào để thêm.", Toast.LENGTH_SHORT).show()
            return
        }
        // Lấy sản phẩm được chọn từ spinner
        val selectedProduct = productList[spinnerSanPhamUserEdit.selectedItemPosition]
        // Lấy số lượng từ EditText
        val quantity = edtSoLuongUserEdit.text.toString().toIntOrNull() ?: 1

        // Kiểm tra số lượng hợp lệ
        if (quantity <= 0) {
            Toast.makeText(this, "Số lượng phải lớn hơn 0.", Toast.LENGTH_SHORT).show()
            return
        }

        gioHangAdapter.addItem(selectedProduct, quantity) // Thêm/cập nhật item trong adapter
        updateTongTien() // Cập nhật tổng tiền
    }

    /**
     * Hàm cập nhật tổng tiền và hiển thị lên TextView.
     */
    private fun updateTongTien() {
        val total = gioHangAdapter.getTotalAmount() // Lấy tổng tiền từ adapter
        val formattedTotal = formatCurrency(total) // Định dạng thành tiền tệ
        tvTongTienUserEdit.text = "Tổng tiền: $formattedTotal" // Hiển thị tổng tiền
    }

    /**
     * Hàm xử lý cập nhật đơn hàng vào Database.
     */
    private fun capNhatDonHang() {
        val itemsInCart = gioHangAdapter.getItems() // Lấy danh sách sản phẩm trong giỏ hàng
        // Kiểm tra giỏ hàng trống
        if (itemsInCart.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm sản phẩm vào đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Kiểm tra đơn hàng hiện tại có tồn tại không
        val orderToUpdate = currentOrder
        if (orderToUpdate == null) {
            Toast.makeText(this, "Lỗi: Đơn hàng không hợp lệ để cập nhật.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // User chỉ có thể sửa đơn hàng khi trạng thái là "Chờ xác nhận"
        if (orderToUpdate.status != 0) {
            Toast.makeText(this, "Chỉ có thể sửa đơn hàng khi đang 'Chờ xác nhận'.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tạo đối tượng Order đã chỉnh sửa
        val updatedOrder = Order(
            id = orderId, // Giữ nguyên ID đơn hàng
            customerId = orderToUpdate.customerId, // Giữ nguyên ID khách hàng
            customerName = orderToUpdate.customerName, // Giữ nguyên tên khách hàng
            date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(Date()), // Cập nhật ngày sửa thành ngày hiện tại
            totalAmount = gioHangAdapter.getTotalAmount(), // Cập nhật tổng tiền
            isVisibleInHistory = orderToUpdate.isVisibleInHistory, // Giữ nguyên cờ hiển thị
            status = 0 // Trạng thái sau khi sửa luôn là "Chờ xác nhận"
        )

        lifecycleScope.launch {
            try {
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

                Toast.makeText(this@ManHinhSuaDonUser, "Đơn hàng đã được cập nhật thành công!", Toast.LENGTH_LONG).show()
                finish() // Đóng màn hình sau khi cập nhật thành công
            } catch (e: Exception) {
                Toast.makeText(this@ManHinhSuaDonUser, "Lỗi khi cập nhật đơn hàng: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Hàm định dạng số Double thành chuỗi tiền tệ (ví dụ: 100.000 VNĐ).
     * @param price Giá trị cần định dạng.
     * @return Chuỗi tiền tệ đã định dạng.
     */
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }
}
