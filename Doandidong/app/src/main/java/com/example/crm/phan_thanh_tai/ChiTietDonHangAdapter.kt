package com.example.crm.phan_thanh_tai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Product
import java.text.NumberFormat
import java.util.Locale

// Lớp data class đại diện cho một sản phẩm trong giỏ hàng (giỏ hàng tạm thời)
// Chứa thông tin sản phẩm và số lượng được chọn
data class CartItem(
    val product: Product, // Đối tượng sản phẩm
    var quantity: Int // Số lượng sản phẩm
)

// Adapter cho RecyclerView hiển thị chi tiết các sản phẩm đã thêm vào đơn hàng
class ChiTietDonHangAdapter(
    private val cartItems: MutableList<CartItem>, // Danh sách các sản phẩm trong giỏ hàng
    private val onRemoveItem: (CartItem) -> Unit // Lambda function xử lý khi người dùng muốn xóa một item
) : RecyclerView.Adapter<ChiTietDonHangAdapter.CartItemViewHolder>() {

    // --- ViewHolder: Ánh xạ các view trong item layout ---
    class CartItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTenSanPham: TextView = itemView.findViewById(R.id.tvTenSanPham) // TextView hiển thị tên sản phẩm
        val tvSoLuong: TextView = itemView.findViewById(R.id.tvSoLuong)     // TextView hiển thị số lượng
        val tvThanhTien: TextView = itemView.findViewById(R.id.tvThanhTien) // TextView hiển thị thành tiền
        val btnXoaItem: ImageButton = itemView.findViewById(R.id.btnXoaItem) // Nút xóa item khỏi giỏ
    }

    // --- Phương thức onCreateViewHolder: Tạo ViewHolder mới ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        // Inflate layout cho từng item từ item_chi_tiet_don_hang.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chi_tiet_don_hang, parent, false)
        return CartItemViewHolder(view) // Trả về một instance của CartItemViewHolder
    }

    // --- Phương thức onBindViewHolder: Gắn dữ liệu vào ViewHolder ---
    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val cartItem = cartItems[position] // Lấy đối tượng CartItem tại vị trí hiện tại

        holder.tvTenSanPham.text = cartItem.product.name // Đặt tên sản phẩm
        holder.tvSoLuong.text = "x${cartItem.quantity}" // Đặt số lượng (ví dụ: x1, x2)

        // Tính toán và định dạng thành tiền cho item này
        val itemTotalPrice = cartItem.quantity * (cartItem.product.price ?: 0.0)
        holder.tvThanhTien.text = formatCurrency(itemTotalPrice) // Đặt thành tiền

        // Gán sự kiện cho nút xóa item
        holder.btnXoaItem.setOnClickListener {
            onRemoveItem(cartItem) // Gọi lambda function đã được truyền vào adapter
        }
    }

    // --- Phương thức getItemCount: Trả về tổng số item trong danh sách ---
    override fun getItemCount(): Int = cartItems.size

    // --- Các hàm hỗ trợ thêm/xóa/cập nhật item trong giỏ hàng ---

    // Hàm thêm một sản phẩm mới vào giỏ hàng
    fun addItem(product: Product, quantity: Int) {
        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            // Nếu có, tăng số lượng của sản phẩm đó
            existingItem.quantity += quantity
        } else {
            // Nếu chưa có, thêm mới một CartItem vào danh sách
            cartItems.add(CartItem(product, quantity))
        }
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    // Hàm xóa một item khỏi giỏ hàng
    fun removeItem(item: CartItem) {
        cartItems.remove(item) // Xóa item khỏi danh sách
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    // Hàm lấy tổng tiền của tất cả các sản phẩm trong giỏ hàng
    fun getTotalAmount(): Double {
        return cartItems.sumOf { it.quantity * (it.product.price ?: 0.0) } // Tính tổng
    }

    // Hàm lấy danh sách các item trong giỏ hàng (để chuẩn bị lưu vào DB)
    fun getItems(): List<CartItem> {
        return cartItems.toList() // Trả về một bản sao của danh sách
    }

    // Hàm định dạng số Double thành chuỗi tiền tệ (ví dụ: 100.000 VNĐ)
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN")) // Định dạng cho tiền Việt
        return formatter.format(price) // Trả về chuỗi đã định dạng
    }
}