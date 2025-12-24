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

// Lớp Activity quản lý màn hình sửa thông tin sản phẩm
class ManHinhSuaSanPham : AppCompatActivity() {

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
    private var productId: Int = -1
    private val units = arrayOf("Cái", "Hộp", "Ly", "Kg", "Chiếc", "Bộ")
    private var categoryNames: List<String> = listOf()
    private var currentProduct: Product? = null // Thêm biến để lưu sản phẩm hiện tại

    // --- ActivityResultLauncher ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) openImagePicker()
        else Toast.makeText(this, "Bạn cần cấp quyền để thay đổi ảnh.", Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.activity_man_hinh_sua_san_pham)

        db = AppDatabase.getDatabase(this)
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        setControl()
        setupSpinners()
        setEvent()
    }

    // Hàm ánh xạ View
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        val header = findViewById<View>(R.id.layoutHeader)
        val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
        tvTitle.text = "CẬP NHẬT SẢN PHẨM" // Đặt tiêu đề
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

        ivHinhAnhSanPham.setOnClickListener { checkPermissionAndOpenPicker() }
        btnLuu.setOnClickListener { xuLyLuuSanPham() }
    }

    // Hàm cài đặt Spinner
    private fun setupSpinners() {
        val unitAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDonViTinh.adapter = unitAdapter

        lifecycleScope.launch {
            categoryNames = db.categoryDao().getCategoryNames().ifEmpty { listOf("Chưa có danh mục") }
            val categoryAdapter = ArrayAdapter(this@ManHinhSuaSanPham, android.R.layout.simple_spinner_item, categoryNames)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDanhMuc.adapter = categoryAdapter

            if (productId != -1) {
                loadProductData()
            } else {
                Toast.makeText(this@ManHinhSuaSanPham, "Lỗi: Không tìm thấy sản phẩm.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // Hàm kiểm tra quyền và mở thư viện ảnh
    private fun checkPermissionAndOpenPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }
    
    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    // Hàm tải dữ liệu cũ của sản phẩm
    private fun loadProductData() {
        lifecycleScope.launch {
            currentProduct = db.productDao().getProductById(productId) // Lưu vào biến currentProduct
            if (currentProduct == null) {
                Toast.makeText(this@ManHinhSuaSanPham, "Không thể tải thông tin sản phẩm.", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            edtTenSanPham.setText(currentProduct!!.name)
            edtMaSanPham.setText(currentProduct!!.code)
            edtGiaBan.setText(currentProduct!!.price.toString())
            edtMoTa.setText(currentProduct!!.description)
            
            spinnerDonViTinh.setSelection(units.indexOf(currentProduct!!.unit).coerceAtLeast(0))
            spinnerDanhMuc.setSelection(categoryNames.indexOf(currentProduct!!.category).coerceAtLeast(0))
            
            currentProduct!!.imageUri?.let {
                val imageUri = Uri.parse(it)
                selectedImageUri = imageUri
                ivHinhAnhSanPham.setImageURI(imageUri)
            }
        }
    }

    // Hàm xử lý logic lưu (cập nhật) sản phẩm
    private fun xuLyLuuSanPham() {
        val name = edtTenSanPham.text.toString().trim()
        val code = edtMaSanPham.text.toString().trim()
        val priceStr = edtGiaBan.text.toString().trim()
        val description = edtMoTa.text.toString().trim()
        val unit = spinnerDonViTinh.selectedItem.toString()
        val category = spinnerDanhMuc.selectedItem.toString()

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
        // Category đã được lấy ở trên

        lifecycleScope.launch {
            // 5. Kiểm tra Mã SP trùng lặp (chỉ khi mã thay đổi)
            if (currentProduct != null && code != currentProduct!!.code) { // Chỉ kiểm tra nếu mã đã được thay đổi
                if (db.productDao().checkCodeExist(code) > 0) {
                    edtMaSanPham.error = "Mã sản phẩm đã tồn tại."
                    return@launch
                }
            }

            val updatedProduct = Product(
                id = productId, code = code, name = name, unit = unit, price = price, 
                category = category, description = description, imageUri = selectedImageUri?.toString()
            )

            db.productDao().updateProduct(updatedProduct)
            Toast.makeText(this@ManHinhSuaSanPham, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}