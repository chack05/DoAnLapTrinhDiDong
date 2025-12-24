package com.example.crm.mai_duc_phi_son

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Order
import java.text.NumberFormat
import java.util.Locale

// Adapter cho RecyclerView hiển thị lịch sử đơn hàng
class LichSuDonHangAdapter(
    private var orderList: List<Order>, // Danh sách các đơn hàng
) : RecyclerView.Adapter<LichSuDonHangAdapter.ViewHolder>() {

    // ViewHolder giữ các view của một item trong item_don_hang.xml
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderId: TextView = view.findViewById(R.id.tvOrderId)         // TextView hiển thị ID đơn hàng
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName) // TextView hiển thị tên khách hàng
        val tvOrderDate: TextView = view.findViewById(R.id.tvOrderDate)     // TextView hiển thị ngày đặt hàng
        val tvTotalAmount: TextView = view.findViewById(R.id.tvTotalAmount) // TextView hiển thị tổng tiền
        // Đã xóa tvOrderStatus vì item_don_hang.xml không còn view này và Order entity không còn trường status
    }

    // Tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout cho một item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_don_hang, parent, false)
        return ViewHolder(view) // Trả về ViewHolder mới
    }

    // Gắn dữ liệu vào ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Lấy đơn hàng tại vị trí hiện tại
        val order = orderList[position]

        // Gán các thông tin từ đối tượng Order vào các TextView
        holder.tvOrderId.text = "Mã đơn: #${order.id}" // Gán mã đơn hàng
        holder.tvCustomerName.text = "Khách hàng: ${order.customerName}" // Tên khách hàng đã có sẵn
        holder.tvOrderDate.text = "Ngày: ${order.date}" // Ngày đặt hàng đã có sẵn (đã đổi từ orderDate sang date)
        holder.tvTotalAmount.text = formatCurrency(order.totalAmount) // Định dạng và gán tổng tiền

        // Đã xóa phần đặt trạng thái và background vì Order entity không còn trường status và item_don_hang.xml không còn tvOrderStatus
    }

    // Trả về số lượng item
    override fun getItemCount(): Int = orderList.size

    // Hàm cập nhật dữ liệu cho adapter
    fun updateData(newOrderList: List<Order>) {
        orderList = newOrderList // Gán danh sách mới
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật
    }

    // Hàm định dạng số thành chuỗi tiền tệ
    private fun formatCurrency(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        return formatter.format(price) // Trả về chuỗi đã định dạng
    }
}
