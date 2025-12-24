package com.example.crm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// SỬA LỖI: Tăng version từ 7 lên 8 để Room nhận biết có sự thay đổi trong schema (thêm isVisibleInHistory vào Order entity)
@Database(entities = [User::class, Customer::class, Product::class, Order::class, OrderDetail::class, Category::class, Comment::class], version = 12)
@TypeConverters(Converters::class) // Đăng ký lớp chuyển đổi kiểu dữ liệu
abstract class AppDatabase : RoomDatabase() {

    // Khai báo các hàm trừu tượng để lấy các đối tượng DAO
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun categoryDao(): CategoryDao
    abstract fun commentDao(): CommentDao

    // Companion object để triển khai mẫu Singleton
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crm_database"
                )
                // Dòng này quan trọng: cho phép Room xóa và tạo lại DB nếu schema thay đổi
                // Điều này giúp trong quá trình phát triển, tránh lỗi IllegalStateException khi update schema
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
