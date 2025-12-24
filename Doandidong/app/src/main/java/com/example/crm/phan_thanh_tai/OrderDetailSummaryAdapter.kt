package com.example.crm.phan_thanh_tai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.OrderDetail // Import OrderDetail
import java.text.NumberFormat
import java.util.Locale

// Adapter cho RecyclerView hiển thị chi tiết các sản phẩm trong một đơn hàng đã tạo
class OrderDetailSummaryAdapter(
    private var orderDetails: List<OrderDetail> // Danh sách các chi tiết đơn hàng
) : RecyclerView.Adapter<OrderDetailSummaryAdapter.OrderDetailViewHolder>() {

    // --- ViewHolder: Ánh xạ các view trong item layout ---
    class OrderDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTenSanPham: TextView = itemView.findViewById(R.id.tvTenSanPham) // Tên sản phẩm
        val tvSoLuong: TextView = itemView.findViewById(R.id.tvSoLuong)     // Số lượng
        val tvThanhTien: TextView = itemView.findViewById(R.id.tvThanhTien) // Thành tiền
    }

    // --- Phương thức onCreateViewHolder: Tạo ViewHolder mới ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        // Inflate layout cho từng item từ item_chi_tiet_don_hang_summary.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chi_tiet_don_hang_summary, parent, false)
        return OrderDetailViewHolder(view) // Trả về một instance của OrderDetailViewHolder
    }

    // --- Phương thức onBindViewHolder: Gắn dữ liệu vào ViewHolder ---
    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        val detail = orderDetails[position] // Lấy đối tượng OrderDetail tại vị trí hiện tại

        holder.tvTenSanPham.text = detail.productName // Đặt tên sản phẩm
        holder.tvSoLuong.text = "x${detail.quantity}" // Đặt số lượng
        holder.tvThanhTien.text = formatCurrency(detail.total) // Đặt thành tiền đã định dạng (sử dụng detail.total)
    }

    // --- Phương thức getItemCount: Trả về tổng số item trong danh sách ---
    override fun getItemCount(): Int = orderDetails.size

    // --- Hàm cập nhật dữ liệu cho Adapter ---
    fun updateData(newList: List<OrderDetail>) {
        orderDetails = newList // Gán danh sách mới
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    // Hàm định dạng số Double thành chuỗi tiền tệ
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price)
    }

    // Hàm lấy danh sách chi tiết đơn hàng hiện tại
    fun getCurrentList(): List<OrderDetail> {
        return orderDetails
    }
}
