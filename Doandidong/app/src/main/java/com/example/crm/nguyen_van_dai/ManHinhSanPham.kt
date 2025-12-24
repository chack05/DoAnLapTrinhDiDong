package com.example.crm.nguyen_van_dai

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Product
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

// Lớp ManHinhSanPham quản lý màn hình danh sách sản phẩm
class ManHinhSanPham : AppCompatActivity() {

    // Khai báo các view
    private lateinit var edtTimKiemSP: EditText
    private lateinit var spinnerDanhMucLoc: Spinner // Spinner lọc danh mục
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabThemSanPham: FloatingActionButton
    private lateinit var tvTongSoLuongSP: TextView

    // Khai báo các thành phần logic
    private lateinit var productAdapter: ProductAdapter
    private lateinit var db: AppDatabase
    private var allCategories: List<String> = listOf() // Danh sách tất cả danh mục (chưa định dạng)
    private var allProducts: List<Product> = listOf() // Danh sách gốc chứa tất cả sản phẩm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_man_hinh_san_pham)

        db = AppDatabase.getDatabase(this)

        setControl()
        setupRecyclerView() // Cài đặt RecyclerView trước
        setEvent()
        loadInitialData() // Tải dữ liệu ban đầu cho Spinner và sản phẩm
    }

    override fun onResume() {
        super.onResume()
        // Cần tải lại toàn bộ sản phẩm gốc và áp dụng lại bộ lọc hiện tại
        loadInitialData()
    }

    private fun setControl() {
        // --- TOOLBAR ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "QUẢN LÝ SẢN PHẨM"

        // Ánh xạ các view
        edtTimKiemSP = findViewById(R.id.edtTimKiemSP)
        spinnerDanhMucLoc = findViewById(R.id.spinnerDanhMucLoc) // Ánh xạ Spinner lọc
        recyclerView = findViewById(R.id.recyclerViewSanPham)
        fabThemSanPham = findViewById(R.id.fabThemSanPham)
        tvTongSoLuongSP = findViewById(R.id.tvTongSoLuongSP)
    }

    private fun setEvent() {
        // --- TOOLBAR BACK BUTTON ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // --- FAB ---
        fabThemSanPham.setOnClickListener {
            val intent = Intent(this, ManHinhThemSanPham::class.java)
            startActivity(intent)
        }

        // --- TÌM KIẾM THEO TỪ KHÓA ---
        edtTimKiemSP.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyCombinedFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // --- LỌC THEO DANH MỤC ---
        spinnerDanhMucLoc.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyCombinedFilter()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { product ->
            val intent = Intent(this, ManHinhChiTietSanPham::class.java)
            intent.putExtra("PRODUCT_ID", product.id)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter
    }

    // Hàm tải dữ liệu ban đầu (danh mục và toàn bộ sản phẩm)
    private fun loadInitialData() {
        lifecycleScope.launch {
            // Tải toàn bộ sản phẩm gốc trước để có dữ liệu đếm số lượng
            allProducts = db.productDao().getAllProducts()

            // Đếm số lượng sản phẩm cho từng danh mục
            val categoryCounts = allProducts.groupBy { it.category }.mapValues { it.value.size }

            // Lấy danh sách tên danh mục từ DB
            val rawCategories = db.categoryDao().getCategoryNames().toMutableList()
            
            // Tạo danh sách các chuỗi danh mục đã định dạng cho Spinner
            val formattedCategories = mutableListOf<String>()
            // Thêm tùy chọn "Tất cả danh mục" vào đầu
            formattedCategories.add("Tất cả danh mục") 

            rawCategories.forEach { categoryName ->
                val count = categoryCounts[categoryName] ?: 0 // Lấy số lượng, mặc định là 0
                formattedCategories.add("$categoryName ($count)") // Định dạng "Tên danh mục (Số lượng)"
            }

            // Cài đặt Adapter cho Spinner
            val categoryAdapter = ArrayAdapter(this@ManHinhSanPham, android.R.layout.simple_spinner_item, formattedCategories)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDanhMucLoc.adapter = categoryAdapter

            // Áp dụng bộ lọc ban đầu
            applyCombinedFilter()
        }
    }

    // --- HÀM LỌC KẾT HỢP TÌM KIẾM VÀ DANH MỤC ---
    private fun applyCombinedFilter() {
        lifecycleScope.launch {
            val keyword = edtTimKiemSP.text.toString().trim() // Lấy từ khóa tìm kiếm
            val selectedFormattedCategory = spinnerDanhMucLoc.selectedItem?.toString() ?: "Tất cả danh mục" // Lấy danh mục được chọn (đã định dạng)

            // Trích xuất tên danh mục gốc từ chuỗi đã định dạng
            val rawCategory = if (selectedFormattedCategory == "Tất cả danh mục") {
                "Tất cả" // Gửi "Tất cả" cho DAO khi chọn "Tất cả danh mục"
            } else {
                // Sử dụng Regex để trích xuất phần tên trước dấu ngoặc đơn
                selectedFormattedCategory.substringBefore(" (").trim()
            }
            
            // Gọi hàm truy vấn kết hợp từ DAO
            val filteredList = db.productDao().getProductsCombined(keyword, rawCategory)
            
            productAdapter.updateData(filteredList) // Cập nhật adapter
            tvTongSoLuongSP.text = "Tổng số lượng: ${filteredList.size}" // Cập nhật tổng số lượng
        }
    }
}