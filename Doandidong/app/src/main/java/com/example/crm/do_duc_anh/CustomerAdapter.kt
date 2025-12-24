package com.example.crm.do_duc_anh // Khai báo file này thuộc package do_duc_anh

import android.view.LayoutInflater // Import lớp LayoutInflater để "thổi phồng" (inflate) layout XML
import android.view.View // Import lớp View
import android.view.ViewGroup // Import lớp ViewGroup
import android.widget.Button // Import lớp Button
import android.widget.TextView // Import lớp TextView
import androidx.recyclerview.widget.RecyclerView // Import lớp RecyclerView
import com.example.crm.R // Import file R để truy cập tài nguyên
import com.example.crm.database.Customer // Import lớp entity Customer

// Khai báo lớp Adapter cho RecyclerView
class CustomerAdapter(
    private var customerList: List<Customer>, // Danh sách khách hàng
    private val onEditClick: (Customer) -> Unit, // Lambda function cho sự kiện click nút Sửa
    private val onDeleteClick: (Customer) -> Unit // Lambda function cho sự kiện click nút Xóa
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    // Lớp ViewHolder đại diện cho một item view trong RecyclerView.
    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // SỬA LỖI: Ánh xạ lại tất cả các TextView để khớp với layout item mới
        val tenKhachHang: TextView = itemView.findViewById(R.id.tvTenKhachHang) // Ánh xạ TextView tên khách hàng
        val maKhachHang: TextView = itemView.findViewById(R.id.tvMaKhachHang) // Ánh xạ TextView mã khách hàng
        val soDienThoai: TextView = itemView.findViewById(R.id.tvSoDienThoai) // Ánh xạ TextView số điện thoại
        val email: TextView = itemView.findViewById(R.id.tvEmail) // Ánh xạ TextView email
        val btnSua: Button = itemView.findViewById(R.id.btnSua) // Ánh xạ Button Sửa
        val btnXoa: Button = itemView.findViewById(R.id.btnXoa) // Ánh xạ Button Xóa
    }

    // Hàm này được gọi khi RecyclerView cần một ViewHolder mới.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        // Inflate layout 'item_khach_hang.xml' để tạo ra một View cho item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_khach_hang, parent, false)
        // Trả về một instance của CustomerViewHolder chứa View vừa tạo
        return CustomerViewHolder(view)
    }

    // Hàm này được gọi để hiển thị dữ liệu tại một vị trí (position) cụ thể.
    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        // Lấy đối tượng Customer tại vị trí 'position'
        val customer = customerList[position]
        
        // SỬA LỖI: Gán dữ liệu từ entity Customer mới (có 'code' và 'email', không có 'address')
        // vào các view tương ứng.
        holder.tenKhachHang.text = customer.name // Gán tên khách hàng
        holder.maKhachHang.text = "Mã: ${customer.code}" // Gán mã khách hàng
        holder.soDienThoai.text = "SĐT: ${customer.phone}" // Gán số điện thoại
        holder.email.text = "Email: ${customer.email}" // Gán email

        // Thiết lập sự kiện click cho nút "Sửa"
        holder.btnSua.setOnClickListener {
            // Gọi lambda function onEditClick và truyền đối tượng customer ra ngoài
            onEditClick(customer)
        }

        // Thiết lập sự kiện click cho nút "Xóa"
        holder.btnXoa.setOnClickListener {
            // Gọi lambda function onDeleteClick và truyền đối tượng customer ra ngoài
            onDeleteClick(customer)
        }
    }

    // Hàm trả về tổng số lượng item trong danh sách.
    override fun getItemCount(): Int {
        return customerList.size // Trả về kích thước của danh sách khách hàng
    }

    // Hàm tùy chỉnh để cập nhật danh sách dữ liệu cho adapter
    fun updateData(newCustomerList: List<Customer>) {
        customerList = newCustomerList // Gán danh sách mới
        notifyDataSetChanged() // Thông báo cho adapter rằng dữ liệu đã thay đổi để RecyclerView vẽ lại
    }
}