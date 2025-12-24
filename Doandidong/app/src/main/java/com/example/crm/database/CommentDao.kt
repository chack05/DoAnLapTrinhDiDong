package com.example.crm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CommentDao {
    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Query("SELECT * FROM comments WHERE productId = :productId ORDER BY timestamp DESC")
    suspend fun getCommentsByProductId(productId: Int): List<Comment>

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: Int)

    // Bổ sung để xóa tất cả bình luận liên quan đến một sản phẩm (khi xóa sản phẩm)
    @Query("DELETE FROM comments WHERE productId = :productId")
    suspend fun deleteCommentsByProductId(productId: Int)

    // Bổ sung để xóa tất cả bình luận của một người dùng (khi xóa người dùng)
    @Query("DELETE FROM comments WHERE userId = :userId")
    suspend fun deleteCommentsByUserId(userId: Int)
}
