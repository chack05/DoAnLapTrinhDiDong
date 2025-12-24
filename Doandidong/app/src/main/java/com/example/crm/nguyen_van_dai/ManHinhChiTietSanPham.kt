package com.example.crm.nguyen_van_dai

import android.content.Context // Import Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText // Import EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.AppDatabase
import com.example.crm.database.Comment // Import Comment
import com.example.crm.database.Product
import com.example.crm.mainUser.CommentAdapter // Import CommentAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat // Import SimpleDateFormat
import java.util.Date // Import Date
import java.util.Locale

// Lớp Activity quản lý màn hình hiển thị chi tiết một sản phẩm
class ManHinhChiTietSanPham : AppCompatActivity() {

    // --- Khai báo View ---
    private lateinit var tvTenSanPhamDetail: TextView
    private lateinit var ivHinhAnhDetail: ImageView
    private lateinit var tvGiaDetail: TextView
    private lateinit var tvMaSanPhamDetail: TextView
    private lateinit var tvDonViDetail: TextView
    private lateinit var tvDanhMucDetail: TextView
    private lateinit var tvMoTaDetail: TextView
    private lateinit var btnXoaSanPham: Button
    private lateinit var btnSuaSanPham: Button

    // Views cho phần bình luận
    private lateinit var edtCommentContent: EditText
    private lateinit var btnSubmitComment: Button
    private lateinit var rvComments: RecyclerView

    // --- Khai báo biến logic ---
    private lateinit var db: AppDatabase
    private var productId: Int = -1
    private var currentProduct: Product? = null
    private lateinit var commentAdapter: CommentAdapter // Adapter cho bình luận

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_man_hinh_chi_tiet_san_pham)

        db = AppDatabase.getDatabase(this)
        productId = intent.getIntExtra("PRODUCT_ID", -1)

        setControl()
        setupCommentRecyclerView() // Cài đặt RecyclerView cho bình luận
        setEvent()

        if (productId == -1) {
            Toast.makeText(this, "ID sản phẩm không hợp lệ.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (productId != -1) {
            loadProductDetails()
            loadComments() // Tải bình luận mỗi khi Activity quay lại foreground
        }
    }

    // Hàm kiểm tra xem người dùng hiện tại có phải là Admin không
    private fun isAdmin(): Boolean {
        val sharedPreferences = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("EMAIL", null)
        // Giả định email admin là "admin@gmail.com". Có thể mở rộng bằng cách lưu role trong SharedPreferences.
        return userEmail == "admin@gmail.com"
    }

    // Hàm ánh xạ View
    private fun setControl() {
        // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
        // Chỉ cần ánh xạ các view khác, toolbar sẽ được xử lý trong loadProductDetails và setEvent
        // --- KẾT THÚC ĐỒNG BỘ ---
        
        tvTenSanPhamDetail = findViewById(R.id.tvTenSanPhamDetail)
        ivHinhAnhDetail = findViewById(R.id.ivHinhAnhDetail)
        tvGiaDetail = findViewById(R.id.tvGiaDetail)
        tvMaSanPhamDetail = findViewById(R.id.tvMaSanPhamDetail)
        tvDonViDetail = findViewById(R.id.tvDonViDetail)
        tvDanhMucDetail = findViewById(R.id.tvDanhMucDetail)
        tvMoTaDetail = findViewById(R.id.tvMoTaDetail)
        btnXoaSanPham = findViewById(R.id.btnXoaSanPham)
        btnSuaSanPham = findViewById(R.id.btnSuaSanPham)

        // Ánh xạ views cho phần bình luận
        edtCommentContent = findViewById(R.id.edtCommentContent)
        btnSubmitComment = findViewById(R.id.btnSubmitComment)
        rvComments = findViewById(R.id.rvComments)

        // Ẩn/hiện nút xóa và sửa sản phẩm dựa trên quyền admin
        if (isAdmin()) {
            btnXoaSanPham.visibility = View.VISIBLE
            btnSuaSanPham.visibility = View.VISIBLE
        } else {
            btnXoaSanPham.visibility = View.GONE
            btnSuaSanPham.visibility = View.GONE
        }
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
        
        btnSuaSanPham.setOnClickListener {
            val intent = Intent(this, ManHinhSuaSanPham::class.java)
            intent.putExtra("PRODUCT_ID", productId)
            startActivity(intent)
        }

        btnXoaSanPham.setOnClickListener {
            currentProduct?.let { showDeleteConfirmationDialog(it) }
        }

        // Gán sự kiện cho nút gửi bình luận
        btnSubmitComment.setOnClickListener {
            submitComment()
        }
    }

    // Hàm cài đặt RecyclerView cho bình luận
    private fun setupCommentRecyclerView() {
        commentAdapter = CommentAdapter(emptyList()) // Khởi tạo adapter với danh sách rỗng
        rvComments.layoutManager = LinearLayoutManager(this) // Sử dụng LinearLayoutManager
        rvComments.adapter = commentAdapter // Gán adapter
    }
    
    // Hàm tải chi tiết sản phẩm
    private fun loadProductDetails() {
        lifecycleScope.launch {
            val product = db.productDao().getProductById(productId)
            
            if (product == null) {
                Toast.makeText(this@ManHinhChiTietSanPham, "Không tìm thấy sản phẩm. Có thể đã bị xóa.", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            currentProduct = product

            // --- ĐỒNG BỘ TOOLBAR THEO CHUẨN MỚI ---
            // Cập nhật tiêu đề của toolbar với tên sản phẩm
            val header = findViewById<View>(R.id.layoutHeader)
            val tvTitle = header.findViewById<TextView>(R.id.tvTieuDe)
            tvTitle.text = product.name
            // --- KẾT THÚC ĐỒNG BỘ ---
            
            // Đổ dữ liệu vào các view
            tvTenSanPhamDetail.text = product.name // Set product name in the detail section
            tvGiaDetail.text = formatCurrency(product.price)
            tvMaSanPhamDetail.text = "Mã sản phẩm: ${product.code}"
            tvDonViDetail.text = "Đơn vị: ${product.unit}"
            tvDanhMucDetail.text = "Danh mục: ${product.category}"
            tvMoTaDetail.text = product.description
            
            if (product.imageUri.isNullOrEmpty()) {
                ivHinhAnhDetail.setImageResource(R.mipmap.ic_launcher)
            } else {
                try {
                    ivHinhAnhDetail.setImageURI(Uri.parse(product.imageUri))
                } catch (e: Exception) {
                    ivHinhAnhDetail.setImageResource(R.mipmap.ic_launcher)
                }
            }
        }
    }

    // Hàm gửi bình luận
    private fun submitComment() {
        val commentContent = edtCommentContent.text.toString().trim() // Lấy nội dung bình luận
        if (commentContent.isEmpty()) {
            Toast.makeText(this, "Nội dung bình luận không được để trống.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Lấy thông tin người dùng hiện tại
            val sharedPreferences = getSharedPreferences("MY_PREFS", Context.MODE_PRIVATE)
            val userEmail = sharedPreferences.getString("EMAIL", null)
            val user = userEmail?.let { db.userDao().getUserByEmail(it) }

            if (user == null) {
                Toast.makeText(this@ManHinhChiTietSanPham, "Bạn cần đăng nhập để bình luận.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val newComment = Comment(
                productId = productId,
                userId = user.id,
                userName = user.name, // Lấy tên người dùng
                content = commentContent,
                timestamp = System.currentTimeMillis() // Thời gian hiện tại
            )

            try {
                db.commentDao().insertComment(newComment) // Chèn bình luận vào DB
                edtCommentContent.setText("") // Xóa nội dung EditText
                Toast.makeText(this@ManHinhChiTietSanPham, "Bình luận của bạn đã được gửi!", Toast.LENGTH_SHORT).show()
                loadComments() // Tải lại danh sách bình luận
            } catch (e: Exception) {
                Toast.makeText(this@ManHinhChiTietSanPham, "Lỗi khi gửi bình luận: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Hàm tải danh sách bình luận cho sản phẩm hiện tại
    private fun loadComments() {
        lifecycleScope.launch {
            val comments = db.commentDao().getCommentsByProductId(productId) // Lấy bình luận từ DB
            commentAdapter.updateData(comments) // Cập nhật adapter
        }
    }

    // Hàm hiển thị hộp thoại xác nhận xóa
    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận xóa?")
            .setMessage("Xóa vĩnh viễn sản phẩm [${product.name}] không thể phục hồi.")
            .setPositiveButton("Đồng ý xóa") { _, _ ->
                lifecycleScope.launch {
                    db.productDao().deleteProduct(product)
                    // Xóa tất cả bình luận liên quan đến sản phẩm này
                    db.commentDao().deleteCommentsByProductId(product.id)
                    Toast.makeText(this@ManHinhChiTietSanPham, "Đã xóa sản phẩm và các bình luận liên quan.", Toast.LENGTH_SHORT).show()
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
}

