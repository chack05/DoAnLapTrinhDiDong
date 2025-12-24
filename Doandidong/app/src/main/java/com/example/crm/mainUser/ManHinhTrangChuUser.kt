package com.example.crm.mainUser // Đổi package thành mainUser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Order
import com.example.crm.database.OrderDetail
import com.example.crm.database.Product
import com.example.crm.le_hong_son.ManHinhDangNhap
import com.example.crm.nguyen_van_dai.ManHinhChiTietSanPham
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lớp này quản lý màn hình chính dành cho người dùng đã đăng nhập (User).
 * Cho phép xem, tìm kiếm sản phẩm và thực hiện mua hàng.
 * Đã được tái cấu trúc và bình luận chi tiết.
 */
class ManHinhTrangChuUser : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // --- Khai báo biến cho View ---
    private lateinit var drawerLayout: DrawerLayout       // Layout chính chứa menu trượt
    private lateinit var navigationView: NavigationView     // View của menu trượt
    private lateinit var toolbar: Toolbar                 // Thanh công cụ trên cùng
    private lateinit var edtSearch: EditText              // Ô tìm kiếm sản phẩm
    private lateinit var recyclerView: RecyclerView         // View hiển thị danh sách sản phẩm

    // --- Khai báo biến cho logic ---
    private lateinit var productAdapter: UserProductAdapter // Adapter cho RecyclerView sản phẩm
    private lateinit var db: AppDatabase                  // Đối tượng truy cập database
    private var allProducts: List<Product> = emptyList()  // Danh sách đầy đủ tất cả sản phẩm

    // --- Vòng đời của Activity ---

    // Hàm được gọi khi Activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Gọi hàm của lớp cha
        // Gắn layout XML vào Activity
        setContentView(R.layout.activity_man_hinh_trang_chu_user)

        // Khởi tạo đối tượng database
        db = AppDatabase.getDatabase(this)

        // Gọi các hàm cài đặt
        setControl()
        setEvent()
        loadProducts()
        loadUserInfo()
    }

    // --- Các hàm cài đặt ---

    /**
     * Hàm ánh xạ các view từ file layout XML.
     */
    private fun setControl() {
        drawerLayout = findViewById(R.id.drawer_layout_user)
        navigationView = findViewById(R.id.nav_view_user)
        toolbar = findViewById(R.id.toolbar_user)
        edtSearch = findViewById(R.id.edtSearchProductUser)
        recyclerView = findViewById(R.id.recyclerViewProductsUser)
    }

    /**
     * Hàm gán sự kiện cho các view và cài đặt các thành phần phụ.
     */
    private fun setEvent() {
        // Cài đặt Toolbar làm Action Bar
        setSupportActionBar(toolbar)
        // Tạo nút hamburger (ba gạch) để mở/đóng menu
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        // Lắng nghe sự kiện kéo mở menu
        drawerLayout.addDrawerListener(toggle)
        // Đồng bộ trạng thái của nút hamburger
        toggle.syncState()
        // Lắng nghe sự kiện click vào các item trong menu
        navigationView.setNavigationItemSelectedListener(this)

        // Gán sự kiện lắng nghe thay đổi văn bản trong ô tìm kiếm
        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            // Khi văn bản thay đổi, gọi hàm lọc sản phẩm
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // --- Các hàm xử lý logic ---

    /**
     * Tải thông tin người dùng (tên) từ SharedPreferences và hiển thị lên header của menu.
     */
    private fun loadUserInfo() {
        lifecycleScope.launch {
            // Lấy file SharedPreferences
            val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
            // Lấy email đã lưu khi đăng nhập
            val email = prefs.getString("EMAIL", "Guest")
            // Tìm user trong database bằng email
            val user = db.userDao().getUserByEmail(email ?: "")

            // Lấy view header của menu
            val headerView = navigationView.getHeaderView(0)
            // Ánh xạ TextView tên người dùng trong header
            val tvUserName: TextView = headerView.findViewById(R.id.nav_header_user_name)
            // Gán tên người dùng, nếu không có thì hiển thị "Guest"
            tvUserName.text = user?.name ?: "Guest"
        }
    }

    /**
     * Tải danh sách sản phẩm từ database và hiển thị lên RecyclerView.
     */
    private fun loadProducts() {
        lifecycleScope.launch {
            // Lấy tất cả sản phẩm từ database
            allProducts = db.productDao().getAllProducts()
            // Khởi tạo adapter với danh sách sản phẩm và lambda xử lý sự kiện "Mua" và "Chi tiết"
            productAdapter = UserProductAdapter(
                products = allProducts,
                onBuyClick = { product ->
                    // Khi nút "Mua" được nhấn, gọi hàm hiển thị dialog xác nhận
                    showConfirmBuyDialog(product)
                },
                onDetailClick = { product ->
                    // Khi nút "Chi tiết" được nhấn, mở màn hình chi tiết sản phẩm
                    val intent = Intent(this@ManHinhTrangChuUser, ManHinhChiTietSanPham::class.java)
                    intent.putExtra("PRODUCT_ID", product.id)
                    startActivity(intent)
                }
            )
            // Cài đặt LayoutManager và gán adapter cho RecyclerView
            recyclerView.layoutManager = LinearLayoutManager(this@ManHinhTrangChuUser)
            recyclerView.adapter = productAdapter
        }
    }

    /**
     * Lọc danh sách sản phẩm dựa trên từ khóa tìm kiếm.
     * @param query Từ khóa người dùng nhập vào.
     */
    private fun filterProducts(query: String) {
        // Nếu từ khóa rỗng, hiển thị lại toàn bộ danh sách
        val filteredList = if (query.isEmpty()) {
            allProducts
        } else {
            // Nếu có từ khóa, lọc danh sách theo tên hoặc mã sản phẩm (không phân biệt hoa thường)
            allProducts.filter {
                it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true)
            }
        }
        // Nếu adapter đã được khởi tạo, cập nhật lại dữ liệu của nó
        if(::productAdapter.isInitialized) {
            productAdapter.updateData(filteredList)
        }
    }

    /**
     * Hiển thị dialog xác nhận mua hàng.
     * @param product Sản phẩm được chọn mua.
     */
    private fun showConfirmBuyDialog(product: Product) {
        // Dùng AlertDialog.Builder để xây dựng dialog
        val builder = AlertDialog.Builder(this)
        // Inflate layout tùy chỉnh cho dialog
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_confirm_buy, null)
        builder.setView(dialogView)

        // Ánh xạ các view trong dialog
        val tvProductName = dialogView.findViewById<TextView>(R.id.tvDialogProductName)
        val tvProductPrice = dialogView.findViewById<TextView>(R.id.tvDialogProductPrice)
        val edtQuantity = dialogView.findViewById<EditText>(R.id.edtDialogQuantity)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnDialogCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnDialogConfirm)

        // Định dạng tiền tệ và hiển thị thông tin sản phẩm
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        tvProductName.text = "Sản phẩm: ${product.name}"
        tvProductPrice.text = "Giá: ${format.format(product.price)}"

        // Tạo dialog
        val dialog = builder.create()

        // Gán sự kiện cho nút "Tạo Hóa Đơn"
        btnConfirm.setOnClickListener {
            val quantityStr = edtQuantity.text.toString()
            // Kiểm tra số lượng hợp lệ
            if (quantityStr.isEmpty() || quantityStr.toIntOrNull() == null || quantityStr.toInt() <= 0) {
                edtQuantity.error = "Số lượng phải là số lớn hơn 0"
                return@setOnClickListener
            }
            val quantity = quantityStr.toInt()
            // Gọi hàm tạo hóa đơn
            createOrder(product, quantity)
            // Đóng dialog
            dialog.dismiss()
        }

        // Gán sự kiện cho nút "Hủy"
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Hiển thị dialog
        dialog.show()
    }

    /**
     * Logic chính để tạo hóa đơn và lưu vào database.
     * @param product Sản phẩm được mua.
     * @param quantity Số lượng mua.
     */
    private fun createOrder(product: Product, quantity: Int) {
        lifecycleScope.launch {
            try {
                // Lấy thông tin người dùng đang đăng nhập
                val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
                val email = prefs.getString("EMAIL", null)
                if (email == null) {
                    Toast.makeText(this@ManHinhTrangChuUser, "Lỗi: Không tìm thấy thông tin đăng nhập.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Tìm User và Customer tương ứng
                val user = db.userDao().getUserByEmail(email)
                if (user == null) {
                    Toast.makeText(this@ManHinhTrangChuUser, "Lỗi: Người dùng không hợp lệ.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val customer = db.customerDao().getCustomerByUserId(user.id)
                if (customer == null) {
                    Toast.makeText(this@ManHinhTrangChuUser, "Lỗi: Không tìm thấy hồ sơ khách hàng.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Tính toán và định dạng ngày tháng
                val totalAmount = product.price * quantity
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))

                // Tạo đối tượng Order mới
                val newOrder = Order(
                    customerId = customer.id,
                    customerName = customer.name,
                    date = dateFormat.format(Date()),
                    totalAmount = totalAmount,
                    isVisibleInHistory = true // Đảm bảo đơn hàng được hiển thị trong lịch sử của admin
                )
                // Ghi log thông tin đơn hàng mới trước khi chèn
                Log.d("ManHinhTrangChuUser", "Đơn hàng mới tạo: customerId=${newOrder.customerId}, customerName=${newOrder.customerName}, totalAmount=${newOrder.totalAmount}, isVisibleInHistory=${newOrder.isVisibleInHistory}")

                // Chèn Order và lấy ID trả về
                val newOrderId = db.orderDao().insertOrder(newOrder).toInt()
                Log.d("ManHinhTrangChuUser", "Đã chèn đơn hàng với ID: $newOrderId")

                // Tạo đối tượng OrderDetail mới
                val newOrderDetail = OrderDetail(
                    orderId = newOrderId,
                    productId = product.id,
                    productName = product.name,
                    quantity = quantity,
                    price = product.price,
                    total = totalAmount
                )
                // Ghi log thông tin chi tiết đơn hàng mới trước khi chèn
                Log.d("ManHinhTrangChuUser", "Chi tiết đơn hàng mới: orderId=${newOrderDetail.orderId}, productId=${newOrderDetail.productId}, quantity=${newOrderDetail.quantity}")

                // Chèn OrderDetail
                db.orderDao().insertOrderDetail(newOrderDetail)

                // Thông báo thành công
                Toast.makeText(this@ManHinhTrangChuUser, "Tạo hóa đơn thành công!", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@ManHinhTrangChuUser, "Tạo hóa đơn thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Xử lý sự kiện khi một item trong menu trượt được chọn.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Nếu chọn "Trang Chủ"
            R.id.nav_user_home -> { /* Không làm gì vì đã ở trang chủ */ }
            // Nếu chọn "Xem hóa đơn"
            R.id.nav_user_orders -> {
                lifecycleScope.launch {
                    val prefs = getSharedPreferences("MY_PREFS", MODE_PRIVATE)
                    val email = prefs.getString("EMAIL", null)
                    if (email != null) {
                        val user = db.userDao().getUserByEmail(email)
                        if (user != null) {
                            val customer = db.customerDao().getCustomerByUserId(user.id)
                            if (customer != null) {
                                // Tạo Intent để mở màn hình lịch sử mua hàng
                                val intent = Intent(this@ManHinhTrangChuUser, ManHinhLichSuMuaHang::class.java)
                                // Truyền ID của khách hàng qua Intent
                                intent.putExtra("CUSTOMER_ID", customer.id)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
            // Nếu chọn "Đăng xuất"
            R.id.nav_user_logout -> {
                xuLyDangXuat()
            }
        }
        // Đóng menu sau khi chọn
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Xử lý logic đăng xuất.
     */
    private fun xuLyDangXuat() {
        // Xóa dữ liệu trong SharedPreferences
        val sharedPreferences = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        // Tạo Intent để quay về màn hình đăng nhập
        val intent = Intent(this, ManHinhDangNhap::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK // Sửa cờ Intent
        startActivity(intent)
        finish() // Đóng Activity hiện tại
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Xử lý khi người dùng nhấn nút back của hệ thống.
     */
    override fun onBackPressed() {
        // Nếu menu đang mở, thì đóng menu lại
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Nếu không, thực hiện hành vi back mặc định
            super.onBackPressed()
        }
    }
}