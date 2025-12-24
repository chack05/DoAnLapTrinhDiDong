package com.example.crm.nguyen_van_dai

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Product
import kotlinx.coroutines.launch

// Lớp Activity quản lý màn hình thêm mới sản phẩm
class ManHinhThemSanPham : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var edtTenSanPham: EditText
    private lateinit var edtMaSanPham: EditText
    private lateinit var spinnerDonViTinh: Spinner
    private lateinit var edtGiaBan: EditText
    private lateinit var spinnerDanhMuc: Spinner
    private lateinit var ivHinhAnhSanPham: ImageView
    private lateinit var edtMoTa: EditText
    private lateinit var btnLuu: Button

    // --- Khai báo biến logic ---
    private var selectedImageUri: Uri? = null
    private lateinit var db: AppDatabase

    // --- ActivityResultLauncher cho việc xin quyền và chọn ảnh ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền để chọn ảnh.", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            ivHinhAnhSanPham.setImageURI(uri)
            selectedImageUri = uri
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_man_hinh_them_san_pham)

        db = AppDatabase.getDatabase(this)

        setControl()
        setupSpinners()
        setEvent()
    }

    // Hàm ánh xạ View
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "THÊM SẢN PHẨM" // Đặt tiêu đề
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Ánh xạ các view còn lại
        edtTenSanPham = findViewById(R.id.edtTenSanPham)
        edtMaSanPham = findViewById(R.id.edtMaSanPham)
        spinnerDonViTinh = findViewById(R.id.spinnerDonViTinh)
        edtGiaBan = findViewById(R.id.edtGiaBan)
        spinnerDanhMuc = findViewById(R.id.spinnerDanhMuc)
        ivHinhAnhSanPham = findViewById(R.id.ivHinhAnhSanPham)
        edtMoTa = findViewById(R.id.edtMoTa)
        btnLuu = findViewById(R.id.btnLuu)
    }

    // Hàm gán sự kiện
    private fun setEvent() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val btnBack = header.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Đóng màn hình
        }
        // --- KẾT THÚC ĐỒNG BỘ ---

        // Sự kiện click vào ảnh để mở thư viện
        ivHinhAnhSanPham.setOnClickListener {
            checkPermissionAndOpenPicker()
        }
        // Sự kiện click nút Lưu
        btnLuu.setOnClickListener { xuLyLuuSanPham() }
    }

    // Xóa onSupportNavigateUp()

    // Hàm cài đặt dữ liệu cho các Spinner
    private fun setupSpinners() {
        // Cài đặt spinner đơn vị tính
        val units = arrayOf("Cái", "Hộp", "Ly", "Kg", "Chiếc", "Bộ")
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDonViTinh.adapter = unitAdapter

        // Cài đặt spinner danh mục (lấy từ DB)
        lifecycleScope.launch {
            // Giả sử có hàm getCategoryNames() trong CategoryDao trả về List<String>
            var categories = try {
                db.categoryDao().getCategoryNames()
            } catch (e: Exception) {
                emptyList()
            }
            if (categories.isEmpty()) {
                categories = listOf("Chưa có danh mục")
                Toast.makeText(
                    this@ManHinhThemSanPham,
                    "Vui lòng tạo danh mục trước",
                    Toast.LENGTH_LONG
                ).show()
            }
            val categoryAdapter = ArrayAdapter(
                this@ManHinhThemSanPham,
                android.R.layout.simple_spinner_item,
                categories
            )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDanhMuc.adapter = categoryAdapter
        }
    }

    // Hàm kiểm tra quyền và mở thư viện ảnh
    private fun checkPermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openImagePicker()
            }

            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Hàm mở thư viện ảnh
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    // Hàm xử lý logic lưu sản phẩm
    private fun xuLyLuuSanPham() {
        val name = edtTenSanPham.text.toString().trim()
        val code = edtMaSanPham.text.toString().trim()
        val priceStr = edtGiaBan.text.toString().trim()
        val description = edtMoTa.text.toString().trim()
        val unit = spinnerDonViTinh.selectedItem.toString()

        // Xóa lỗi cũ (nếu có)
        edtTenSanPham.error = null
        edtMaSanPham.error = null
        edtGiaBan.error = null

        // 1. Kiểm tra Tên sản phẩm
        if (name.isEmpty()) {
            edtTenSanPham.error = "Tên sản phẩm không được để trống."
            return
        }

        // 2. Kiểm tra Mã sản phẩm
        if (code.isEmpty()) {
            edtMaSanPham.error = "Mã sản phẩm không được để trống."
            return
        }

        // 3. Kiểm tra Giá bán
        if (priceStr.isEmpty()) {
            edtGiaBan.error = "Giá bán không được để trống."
            return
        }
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            edtGiaBan.error = "Giá bán không hợp lệ (phải là số lớn hơn 0)."
            return
        }

        // 4. Kiểm tra Danh mục
        if (spinnerDanhMuc.selectedItem == null || spinnerDanhMuc.selectedItem.toString() == "Chưa có danh mục") {
            Toast.makeText(this, "Vui lòng chọn danh mục hợp lệ.", Toast.LENGTH_SHORT).show()
            return
        }
        val category = spinnerDanhMuc.selectedItem.toString()

        lifecycleScope.launch {
            // 5. Kiểm tra Mã SP trùng lặp
            if (db.productDao().checkCodeExist(code) > 0) {
                edtMaSanPham.error = "Mã sản phẩm đã tồn tại."
                return@launch
            }

            val newProduct = Product(
                code = code, name = name, unit = unit, price = price, category = category,
                description = description, imageUri = selectedImageUri?.toString()
            )

            lifecycleScope.launch {
                db.productDao().insertProduct(newProduct)
                Toast.makeText(
                    this@ManHinhThemSanPham,
                    "Lưu sản phẩm thành công!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
