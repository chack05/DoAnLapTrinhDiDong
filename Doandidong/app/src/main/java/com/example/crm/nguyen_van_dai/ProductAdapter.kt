package com.example.crm.nguyen_van_dai // Khai báo package

import android.net.Uri // Import Uri để làm việc với đường dẫn ảnh
import android.view.LayoutInflater // Import để inflate layout
import android.view.View // Import View
import android.view.ViewGroup // Import ViewGroup
import android.widget.ImageView // Import ImageView
import android.widget.TextView // Import TextView
import androidx.recyclerview.widget.RecyclerView // Import RecyclerView
import com.example.crm.R // Import R
import com.example.crm.database.Product // Import Product entity
import java.text.NumberFormat // Import NumberFormat
import java.util.Locale // Import Locale

// Khai báo lớp Adapter cho RecyclerView danh sách sản phẩm
class ProductAdapter(
    private var productList: List<Product>, // Danh sách sản phẩm
    private val onItemClick: (Product) -> Unit // Lambda function cho sự kiện click vào item
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    // Lớp ViewHolder giữ các view của một item
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val hinhAnh: ImageView = itemView.findViewById(R.id.ivHinhAnhItem)
        val tenSanPham: TextView = itemView.findViewById(R.id.tvTenSanPhamItem)
        val maSanPham: TextView = itemView.findViewById(R.id.tvMaSanPhamItem)
        val gia: TextView = itemView.findViewById(R.id.tvGiaItem)
    }

    // Hàm tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_san_pham, parent, false)
        return ProductViewHolder(view)
    }

    // Hàm hiển thị dữ liệu tại một vị trí
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        // Lấy sản phẩm tại vị trí hiện tại. Đối tượng này chắc chắn không null vì nó từ list.
        val product = productList[position]

        // Gán dữ liệu text
        holder.tenSanPham.text = product.name
        holder.maSanPham.text = "Mã: ${product.code}"
        holder.gia.text = formatCurrency(product.price)

        // SỬA LỖI: Thêm logic xử lý ảnh an toàn tuyệt đối
        // 1. Kiểm tra xem imageUri có bị null hoặc rỗng không
        if (product.imageUri.isNullOrEmpty()) {
            // Nếu không có URI, hiển thị ảnh mặc định
            holder.hinhAnh.setImageResource(R.mipmap.ic_launcher)
        } else {
            // Nếu có URI, thử hiển thị nó
            try {
                // Dùng Uri.parse() để chuyển chuỗi String thành đối tượng Uri
                holder.hinhAnh.setImageURI(Uri.parse(product.imageUri))
            } catch (e: Exception) {
                // 2. Nếu có bất kỳ lỗi nào xảy ra khi parse hoặc set URI (ví dụ: URI không hợp lệ)
                // thì bắt lỗi lại và hiển thị ảnh mặc định để app không bị crash.
                holder.hinhAnh.setImageResource(R.mipmap.ic_launcher)
                e.printStackTrace() // In lỗi ra Logcat để lập trình viên biết và sửa
            }
        }

        // Thiết lập sự kiện click cho toàn bộ item view
        holder.itemView.setOnClickListener {
            // Gọi lambda onItemClick và truyền đối tượng sản phẩm ra ngoài
            onItemClick(product)
        }
    }

    // Hàm trả về tổng số lượng item
    override fun getItemCount(): Int = productList.size
    
    // Hàm định dạng số thành chuỗi tiền tệ VND
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }

    // Hàm cập nhật danh sách dữ liệu
    fun updateData(newProductList: List<Product>) {
        productList = newProductList
        notifyDataSetChanged()
    }
}
