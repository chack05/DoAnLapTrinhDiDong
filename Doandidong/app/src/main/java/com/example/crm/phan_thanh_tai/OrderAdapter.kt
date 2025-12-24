package com.example.crm.phan_thanh_tai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Order
import java.text.NumberFormat
import java.util.Locale

// Adapter cho RecyclerView hiển thị danh sách các đơn hàng
class OrderAdapter(
    private var orderList: List<Order>, // Danh sách các đơn hàng
    private val onItemClick: (Order) -> Unit // Lambda function xử lý khi một item đơn hàng được click
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    // --- ViewHolder: Ánh xạ các view trong item layout ---
    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)         // TextView hiển thị ID đơn hàng
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName) // TextView hiển thị tên khách hàng
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)     // TextView hiển thị ngày đặt hàng
        val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount) // TextView hiển thị tổng tiền
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus) // TextView hiển thị trạng thái đơn hàng
    }

    // --- Phương thức onCreateViewHolder: Tạo ViewHolder mới ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        // Inflate layout cho từng item từ item_don_hang.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_don_hang, parent, false)
        return OrderViewHolder(view) // Trả về một instance của OrderViewHolder
    }

    // --- Phương thức onBindViewHolder: Gắn dữ liệu vào ViewHolder ---
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position] // Lấy đối tượng Order tại vị trí hiện tại

        holder.tvOrderId.text = "Mã đơn: #${order.id}" // Đặt ID đơn hàng
        holder.tvCustomerName.text = "Khách hàng: ${order.customerName}" // Đặt tên khách hàng
        holder.tvOrderDate.text = "Ngày: ${order.date}" // Đặt ngày đặt hàng
        holder.tvTotalAmount.text = formatCurrency(order.totalAmount) // Đặt tổng tiền đã định dạng

        // Hiển thị trạng thái và màu sắc
        when (order.status) {
            0 -> { // Chờ xác nhận
                holder.tvOrderStatus.text = "Trạng thái: 🔴 Chờ xác nhận"
                holder.tvOrderStatus.setTextColor(holder.itemView.context.resources.getColor(R.color.orange_status))
            }
            1 -> { // Đang giao
                holder.tvOrderStatus.text = "Trạng thái: 🔵 Đang giao"
                holder.tvOrderStatus.setTextColor(holder.itemView.context.resources.getColor(R.color.blue_status))
            }
            2 -> { // Thành công
                holder.tvOrderStatus.text = "Trạng thái: 🟢 Thành công"
                holder.tvOrderStatus.setTextColor(holder.itemView.context.resources.getColor(R.color.green_status))
            }
            3 -> { // Đã hủy
                holder.tvOrderStatus.text = "Trạng thái: ⚫ Đã hủy"
                holder.tvOrderStatus.setTextColor(holder.itemView.context.resources.getColor(R.color.red_status))
            }
            else -> { // Trạng thái không xác định
                holder.tvOrderStatus.text = "Trạng thái: Không xác định"
                holder.tvOrderStatus.setTextColor(holder.itemView.context.resources.getColor(R.color.gray_status))
            }
        }

        // Gán sự kiện click cho toàn bộ item
        holder.itemView.setOnClickListener {
            onItemClick(order) // Gọi lambda function đã được truyền vào adapter
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
