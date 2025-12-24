package com.example.crm.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productId: Int,
    val userId: Int, // ID của người dùng đã bình luận
    val userName: String, // Tên người dùng tại thời điểm bình luận (để hiển thị)
    val content: String, // Nội dung bình luận
    val timestamp: Long // Thời gian bình luận (epoch milliseconds)
)