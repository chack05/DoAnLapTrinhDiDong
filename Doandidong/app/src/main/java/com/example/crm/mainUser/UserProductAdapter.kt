package com.example.crm.mainUser // Đổi package thành mainUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Product
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter cho RecyclerView hiển thị danh sách sản phẩm ở trang chủ của User.
 * Mỗi item sẽ có nút "Mua" để bắt đầu quy trình tạo hóa đơn.
 * Đã được tái cấu trúc và bình luận chi tiết.
 */
class UserProductAdapter(
    // Danh sách các sản phẩm sẽ được hiển thị
    private var products: List<Product>,
    // Một lambda function (hàm) sẽ được gọi khi người dùng nhấn nút "Mua"
    private val onBuyClick: (Product) -> Unit,
    // Một lambda function sẽ được gọi khi người dùng nhấn nút "Chi tiết"
    private val onDetailClick: (Product) -> Unit
) : RecyclerView.Adapter<UserProductAdapter.ProductViewHolder>() {

    /**
     * Lớp ViewHolder chịu trách nhiệm giữ các tham chiếu đến các View trong một item.
     * Việc này giúp tránh phải gọi findViewById() nhiều lần, tăng hiệu suất.
     */
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ các View từ layout item_san_pham_user.xml
        val tenSanPham: TextView = itemView.findViewById(R.id.tvTenSanPhamItem)
        val maSanPham: TextView = itemView.findViewById(R.id.tvMaSanPhamItem)
        val gia: TextView = itemView.findViewById(R.id.tvGiaItem)
        val hinhAnh: ImageView = itemView.findViewById(R.id.ivHinhAnhItem)
        val btnBuy: Button = itemView.findViewById(R.id.btnBuyProduct)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetailProduct) // Ánh xạ nút Chi tiết
    }

    /**
     * Được gọi khi RecyclerView cần một ViewHolder mới.
     * Hàm này "thổi phồng" (inflate) layout của một item từ XML.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        // Tạo một đối tượng View từ file layout item_san_pham_user.xml
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_san_pham_user, parent, false)
        // Trả về một ViewHolder mới chứa View vừa tạo
        return ProductViewHolder(view)
    }

    /**
     * Được gọi bởi RecyclerView để hiển thị dữ liệu tại một vị trí cụ thể.
     * Hàm này cập nhật nội dung của ViewHolder để phản ánh dữ liệu của item tại vị trí đó.
     */
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        // Lấy đối tượng Product tại vị trí hiện tại trong danh sách
        val product = products[position]

        // Gán dữ liệu của sản phẩm vào các View trong ViewHolder
        holder.tenSanPham.text = product.name
        holder.maSanPham.text = "Mã: ${product.code}"

        // Định dạng giá tiền theo đơn vị tiền tệ của Việt Nam (VNĐ)
        val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        holder.gia.text = format.format(product.price)

        // TODO: Xử lý hiển thị hình ảnh từ URI nếu có
        // Hiện tại đang dùng ảnh mặc định
        holder.hinhAnh.setImageResource(R.mipmap.ic_launcher)

        // Gán sự kiện click cho nút "Mua"
        holder.btnBuy.setOnClickListener {
            // Khi nút được nhấn, gọi lambda onBuyClick và truyền vào đối tượng sản phẩm hiện tại
            onBuyClick(product)
        }

        // Gán sự kiện click cho nút "Chi tiết"
        holder.btnDetail.setOnClickListener {
            // Khi nút được nhấn, gọi lambda onDetailClick và truyền vào đối tượng sản phẩm hiện tại
            onDetailClick(product)
        }
    }

    /**
     * Trả về tổng số lượng item trong danh sách dữ liệu.
     */
    override fun getItemCount() = products.size

    /**
     * Hàm công khai để cập nhật danh sách sản phẩm của adapter từ bên ngoài.
     * @param newProducts Danh sách sản phẩm mới.
     */
    fun updateData(newProducts: List<Product>) {
        // Gán danh sách sản phẩm mới cho adapter
        products = newProducts
        // Thông báo cho RecyclerView rằng dữ liệu đã thay đổi và cần vẽ lại toàn bộ danh sách
        notifyDataSetChanged()
    }
}