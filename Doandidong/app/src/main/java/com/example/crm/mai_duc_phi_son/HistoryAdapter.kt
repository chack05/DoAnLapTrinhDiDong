package com.example.crm.mai_duc_phi_son

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Order
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

// Adapter cho RecyclerView hiển thị lịch sử đơn hàng theo kiểu compact
class HistoryAdapter(
    private var orderList: List<Order>, // Danh sách các đơn hàng
    private val onDeleteClick: (Order) -> Unit // Lambda function xử lý khi nút xóa một đơn hàng được click
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    // --- ViewHolder: Ánh xạ các view trong item layout ---
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNgayThangLichSu: TextView = itemView.findViewById(R.id.tvNgayThangLichSu)         // Ngày tháng
        val tvTenKhachHangLichSu: TextView = itemView.findViewById(R.id.tvTenKhachHangLichSu) // Tên khách hàng
        val tvMaDonHangLichSu: TextView = itemView.findViewById(R.id.tvMaDonHangLichSu)     // Mã đơn hàng
        val tvTongTienLichSu: TextView = itemView.findViewById(R.id.tvTongTienLichSu)         // Tổng tiền
        val btnXoaLichSu: ImageButton = itemView.findViewById(R.id.btnXoaLichSu)             // Nút xóa
    }

    // --- Phương thức onCreateViewHolder: Tạo ViewHolder mới ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        // Inflate layout cho từng item từ item_lich_su_compact.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lich_su_compact, parent, false)
        return HistoryViewHolder(view) // Trả về một instance của HistoryViewHolder
    }

    // --- Phương thức onBindViewHolder: Gắn dữ liệu vào ViewHolder ---
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val order = orderList[position] // Lấy đối tượng Order tại vị trí hiện tại

        // Định dạng ngày tháng chỉ hiển thị ngày/tháng
        val dateFormat = SimpleDateFormat("dd/MM", Locale("vi", "VN"))
        // Chuyển đổi String "dd/MM/yyyy" sang Date rồi định dạng lại thành "dd/MM"
        val fullDate = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).parse(order.date)
        holder.tvNgayThangLichSu.text = fullDate?.let { dateFormat.format(it) } ?: order.date.substring(0, 5) // Handle parse error

        holder.tvTenKhachHangLichSu.text = order.customerName // Đặt tên khách hàng
        holder.tvMaDonHangLichSu.text = "#${order.id}" // Đặt mã đơn hàng
        holder.tvTongTienLichSu.text = formatCurrency(order.totalAmount) // Đặt tổng tiền đã định dạng

        // Gán sự kiện click cho nút xóa
        holder.btnXoaLichSu.setOnClickListener {
            onDeleteClick(order) // Gọi lambda function đã được truyền vào adapter
        }
    }

    // --- Phương thức getItemCount: Trả về tổng số item trong danh sách ---
    override fun getItemCount(): Int = orderList.size

    // --- Hàm cập nhật dữ liệu cho Adapter ---
    fun updateData(newList: List<Order>) {
        orderList = newList // Gán danh sách mới
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật lại giao diện
    }

    // Hàm định dạng số Double thành chuỗi tiền tệ (ví dụ: 100.000 VNĐ)
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN")) // Định dạng cho tiền Việt
        return formatter.format(price) // Trả về chuỗi đã định dạng
    }
}
