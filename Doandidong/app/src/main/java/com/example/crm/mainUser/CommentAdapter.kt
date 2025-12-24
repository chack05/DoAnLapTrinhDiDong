package com.example.crm.mainUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.crm.R
import com.example.crm.database.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Adapter cho RecyclerView hiển thị danh sách các bình luận
class CommentAdapter(
    private var commentList: List<Comment> // Danh sách các đối tượng Comment
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    // --- ViewHolder: Ánh xạ các view trong item layout ---
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCommentUserName: TextView = itemView.findViewById(R.id.tvCommentUserName)
        val tvCommentTimestamp: TextView = itemView.findViewById(R.id.tvCommentTimestamp)
        val tvCommentContent: TextView = itemView.findViewById(R.id.tvCommentContent)
    }

    // --- Phương thức onCreateViewHolder: Tạo ViewHolder mới ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate layout cho từng item từ item_comment.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view) // Trả về một instance của CommentViewHolder
    }

    // --- Phương thức onBindViewHolder: Gắn dữ liệu vào ViewHolder ---
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position] // Lấy đối tượng Comment tại vị trí hiện tại

        holder.tvCommentUserName.text = comment.userName // Đặt tên người dùng bình luận
        // Định dạng thời gian
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        holder.tvCommentTimestamp.text = sdf.format(Date(comment.timestamp)) // Đặt thời gian bình luận
        holder.tvCommentContent.text = comment.content // Đặt nội dung bình luận
    }

    // --- Phương thức getItemCount: Trả về tổng số item trong danh sách ---
    override fun getItemCount(): Int = commentList.size

    // --- Hàm cập nhật dữ liệu cho Adapter ---
    fun updateData(newList: List<Comment>) {
        commentList = newList // Gán danh sách mới
        notifyDataSetChanged() // Thông báo cho RecyclerView cập nhật lại giao diện
    }
}