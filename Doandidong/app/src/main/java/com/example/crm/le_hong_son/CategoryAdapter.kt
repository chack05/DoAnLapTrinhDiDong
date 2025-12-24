package com.example.crm.le_hong_son

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Category

// Adapter cho RecyclerView hiển thị danh sách danh mục
class CategoryAdapter(
    private var categories: List<Category>, // Danh sách các danh mục
    private val onEditClick: (Category) -> Unit, // Lambda function để xử lý sự kiện click nút Sửa
    private val onDeleteClick: (Category) -> Unit // Lambda function để xử lý sự kiện click nút Xóa
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    // ViewHolder chứa các view của một item
    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val editButton: ImageButton = itemView.findViewById(R.id.btnEditCategory)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteCategory)
    }

    // Tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        // Inflate layout của một item từ XML
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_danh_muc, parent, false)
        return CategoryViewHolder(view)
    }

    // Gán dữ liệu cho một ViewHolder tại vị trí nhất định
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // Lấy danh mục tại vị trí hiện tại
        val category = categories[position]
        // Gán tên danh mục cho TextView
        holder.categoryName.text = category.name

        // Thiết lập sự kiện click cho nút Sửa
        holder.editButton.setOnClickListener { onEditClick(category) }
        // Thiết lập sự kiện click cho nút Xóa
        holder.deleteButton.setOnClickListener { onDeleteClick(category) }
    }

    // Trả về số lượng item trong danh sách
    override fun getItemCount() = categories.size

    // Hàm để cập nhật dữ liệu cho adapter và thông báo cho RecyclerView
    fun updateData(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged() // Báo cho RecyclerView vẽ lại toàn bộ danh sách
    }
}
