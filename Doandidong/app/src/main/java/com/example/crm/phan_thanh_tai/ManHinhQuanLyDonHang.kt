package com.example.crm.phan_thanh_tai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.util.Log
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Order
import com.example.crm.database.Product // Import Product entity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

// Lớp Activity quản lý màn hình hiển thị danh sách các đơn hàng
class ManHinhQuanLyDonHang : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var recyclerViewDonHang: RecyclerView
    private lateinit var fabThemDonHang: FloatingActionButton
    private lateinit var edtTimKiemDon: EditText
    private lateinit var tvTongSoLuongDon: TextView
    private lateinit var spinnerLocTheoSanPham: Spinner // Khai báo Spinner lọc theo sản phẩm

    // --- Khai báo biến logic ---
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var db: AppDatabase
    private var allOrders: List<Order> = emptyList() // Danh sách gốc chứa tất cả đơn hàng
    private var allProducts: List<Product> = emptyList() // Danh sách tất cả sản phẩm
    private var formattedProductsForSpinner: List<String> = emptyList() // Danh sách sản phẩm đã định dạng cho Spinner (tên + count)
    private var selectedProductId: Int = -1 // ID sản phẩm đang được chọn để lọc, -1 là "Tất cả sản phẩm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_quan_ly_don_hang)

        db = AppDatabase.getDatabase(this)

        setControl()          // Ánh xạ các View
        setEvent()            // Gán các sự kiện
        setupRecyclerView()   // Cài đặt RecyclerView
        loadInitialData()     // Tải dữ liệu ban đầu cho Spinner và đơn hàng
    }

    override fun onResume() {
        super.onResume()
        // Cần tải lại toàn bộ đơn hàng gốc và áp dụng lại bộ lọc hiện tại
        loadInitialData()
    }

    // --- Các hàm cài đặt ---

    // Hàm ánh xạ các View từ layout XML vào biến trong code
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "QUẢN LÝ ĐƠN HÀNG" // Đặt tiêu đề cho màn hình
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ RecyclerView, FAB, EditText tìm kiếm và Spinner lọc
        recyclerViewDonHang = findViewById(R.id.recyclerViewDonHang)
        fabThemDonHang = findViewById(R.id.fabThemDonHang)
        edtTimKiemDon = findViewById(R.id.edtTimKiemDon)
        tvTongSoLuongDon = findViewById(R.id.tvTongSoLuongDon)
        spinnerLocTheoSanPham = findViewById(R.id.spinnerLocTheoSanPham) // Ánh xạ Spinner lọc
    }

    // Hàm cài đặt RecyclerView
    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(emptyList()) { order ->
            val intent = Intent(this, ManHinhChiTietDon::class.java)
            intent.putExtra("ORDER_ID", order.id) // Truyền ID của đơn hàng
            startActivity(intent)
        }
        recyclerViewDonHang.layoutManager = LinearLayoutManager(this) // Đặt LayoutManager
        recyclerViewDonHang.adapter = orderAdapter // Gán Adapter
    }

    // Hàm tải dữ liệu ban đầu (sản phẩm cho Spinner và toàn bộ đơn hàng)
    private fun loadInitialData() {
        lifecycleScope.launch {
            // Tải tất cả sản phẩm
            allProducts = db.productDao().getAllProducts()

            // Tạo danh sách sản phẩm đã định dạng cho Spinner
            val productNamesWithCount = mutableListOf<String>()
            productNamesWithCount.add("Tất cả sản phẩm") // Thêm tùy chọn "Tất cả sản phẩm" vào đầu

            allProducts.forEach { product ->
                // Đếm số lượng đơn hàng chứa sản phẩm này
                val orderCount = db.orderDao().countOrdersByProductId(product.id)
                productNamesWithCount.add("${product.name} ($orderCount)")
            }
            formattedProductsForSpinner = productNamesWithCount

            // Cài đặt Adapter cho Spinner lọc sản phẩm
            val productFilterAdapter = ArrayAdapter(this@ManHinhQuanLyDonHang, android.R.layout.simple_spinner_item, formattedProductsForSpinner)
            productFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerLocTheoSanPham.adapter = productFilterAdapter

            // Áp dụng bộ lọc ban đầu (sẽ tải đơn hàng)
            applyCombinedFilter()
        }
    }

    // Hàm gán các sự kiện cho các View
    private fun setEvent() {
        // --- TOOLBAR BACK BUTTON ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // --- FAB ---
        fabThemDonHang.setOnClickListener {
            val intent = Intent(this, ManHinhTaoDonHang::class.java)
            startActivity(intent)
        }

        // --- TÌM KIẾM ĐƠN HÀNG ---
        edtTimKiemDon.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyCombinedFilter() // Khi text thay đổi, gọi hàm lọc kết hợp
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // --- LỌC THEO SẢN PHẨM ---
        spinnerLocTheoSanPham.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Lấy tên sản phẩm đã định dạng từ Spinner
                val selectedFormattedProduct = formattedProductsForSpinner[position]
                // Trích xuất ID sản phẩm
                selectedProductId = if (position == 0) { // "Tất cả sản phẩm"
                    -1
                } else {
                    // Tìm sản phẩm gốc dựa vào tên đã định dạng
                    val productName = selectedFormattedProduct.substringBefore(" (").trim()
                    allProducts.find { it.name == productName }?.id ?: -1
                }
                applyCombinedFilter() // Khi sản phẩm được chọn thay đổi, gọi hàm lọc kết hợp
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // --- Các hàm xử lý Logic nghiệp vụ ---

    // Hàm này sẽ được thay thế bằng applyCombinedFilter()
    private fun loadAllOrders() {
        lifecycleScope.launch {
            allOrders = db.orderDao().getAllOrders() // Tải tất cả đơn hàng (cho trường hợp không lọc)
            applyCombinedFilter() // Áp dụng bộ lọc hiện tại
        }
    }

    // --- HÀM LỌC KẾT HỢP TÌM KIẾM VÀ SẢN PHẨM ---
    private fun applyCombinedFilter() {
        lifecycleScope.launch {
            val keyword = edtTimKiemDon.text.toString().trim() // Lấy từ khóa tìm kiếm
            
            // Gọi hàm truy vấn kết hợp từ DAO
            val filteredList = db.orderDao().getOrdersCombined(keyword, selectedProductId)

            // Ghi log số lượng và thông tin các đơn hàng đã lọc
            Log.d("ManHinhQuanLyDonHang", "Số lượng đơn hàng được lọc: ${filteredList.size}")
            filteredList.take(5).forEachIndexed { index, order ->
                Log.d("ManHinhQuanLyDonHang", "Đơn hàng ${index + 1}: ID=${order.id}, Khách hàng=${order.customerName}, Tổng tiền=${order.totalAmount}, Hiển thị=${order.isVisibleInHistory}")
            }
            
            orderAdapter.updateData(filteredList) // Cập nhật adapter
            tvTongSoLuongDon.text = "Tổng số lượng: ${filteredList.size}" // Cập nhật tổng số lượng
        }
    }
}