package com.example.crm.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

// Đối tượng DataSeeder chứa các hàm để tạo và chèn dữ liệu mẫu vào database
object DataSeeder {

    // Hàm tổng hợp để chèn tất cả dữ liệu mẫu
    suspend fun seedAllData(context: Context) = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)

        // Chèn danh mục trước
        val categories = seedCategories(db)
        // Chèn khách hàng
        val customers = seedCustomers(db)
        // Chèn sản phẩm (cần danh mục đã có)
        val products = seedProducts(db, categories)
        // Chèn đơn hàng (cần khách hàng và sản phẩm đã có)
        seedOrders(db, customers, products)
        // Chèn người dùng mẫu (nếu chưa có)
        seedUsers(db)
    }

    // Hàm chèn dữ liệu người dùng mẫu
    private suspend fun seedUsers(db: AppDatabase) {
        if (db.userDao().getAllUsers().isEmpty()) { // Chỉ chèn nếu chưa có user nào
            val user = User(
                name = "Admin",
                email = "admin@gmail.com",
                password = "Admin123@",
                phone = "0123456789" // Thêm SĐT mẫu
            )
            db.userDao().insert(user) // Sửa tên hàm thành insert
        }
    }

    // Hàm chèn 20 danh mục mẫu
    private suspend fun seedCategories(db: AppDatabase): List<Category> {
        // Xóa tất cả danh mục cũ để đảm bảo chỉ có 20 danh mục mới
        db.categoryDao().deleteAllCategories()

        val categories = mutableListOf<Category>()
        val categoryNames = listOf(
            "Điện tử", "Gia dụng", "Thời trang", "Sách", "Thể thao",
            "Đồ chơi", "Mỹ phẩm", "Thực phẩm", "Đồ uống", "Văn phòng phẩm",
            "Đồ dùng cá nhân", "Phụ kiện", "Nội thất", "Ngoại thất", "Ô tô - Xe máy",
            "Du lịch", "Thú cưng", "Y tế", "Đồ handmade", "Công cụ" // Chỉ lấy 20 tên đầu
        )

        for (i in 0 until 20) { // Thay đổi giới hạn vòng lặp thành 20
            val name = categoryNames[i] // Lấy tên trực tiếp từ list
            categories.add(Category(name = name))
        }

        categories.forEach { db.categoryDao().insertCategory(it) }
        return db.categoryDao().getAllCategories()
    }

    // Hàm chèn 20 khách hàng mẫu
    private suspend fun seedCustomers(db: AppDatabase): List<Customer> {
        if (db.customerDao().getAllCustomers().size >= 20) {
            return db.customerDao().getAllCustomers()
        }
        val customers = mutableListOf<Customer>()
        val firstNames = listOf("Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Đặng", "Bùi")
        val lastNames = listOf("Văn An", "Thị Bình", "Quốc Cường", "Mỹ Duyên", "Đức Huy", "Minh Khai", "Thanh Loan", "Công Minh", "Hữu Nghĩa", "Thúy An")
        val domains = listOf("gmail.com", "outlook.com", "yahoo.com", "example.com")

        for (i in 0 until 20) {
            val firstName = firstNames[Random.nextInt(firstNames.size)]
            val lastName = lastNames[Random.nextInt(lastNames.size)]
            val name = "$firstName $lastName"
            val email = "${name.replace(" ", ".").toLowerCase()}${Random.nextInt(100)}@${domains[Random.nextInt(domains.size)]}"
            val phone = "0${Random.nextInt(100000000, 999999999)}"
            val code = "KH${1000 + i}"
            val password = "password123" // Mật khẩu mẫu
            val userId = 0 // ID người dùng mẫu

            customers.add(Customer(code = code, name = name, email = email, phone = phone, password = password, userId = userId))
        }
        customers.forEach { db.customerDao().insert(it) }
        return db.customerDao().getAllCustomers()
    }

    // Hàm chèn 20 sản phẩm mẫu
    private suspend fun seedProducts(db: AppDatabase, categories: List<Category>): List<Product> {
        db.productDao().deleteAllProducts() // Xóa tất cả sản phẩm cũ

        if (categories.isEmpty()) {
            return emptyList()
        }

        val products = mutableListOf<Product>()
        val units = listOf("Cái", "Hộp", "Ly", "Kg", "Chiếc", "Bộ")
        val productBaseNames = listOf(
            "Điện thoại", "Máy tính bảng", "Tivi", "Tủ lạnh", "Máy giặt",
            "Bàn ủi", "Quạt điện", "Bếp từ", "Nồi cơm điện", "Ấm siêu tốc",
            "Áo thun", "Quần jean", "Váy đầm", "Giày thể thao", "Túi xách da",
            "Sách tiểu thuyết", "Vợt cầu lông", "Bóng đá", "Đồ chơi Lego", "Gấu bông" // Chỉ lấy 20 tên đầu
        )

        for (i in 0 until 20) {
            val baseName = productBaseNames[i]
            val name = baseName // Đã bỏ số ngẫu nhiên ở cuối tên sản phẩm
            val code = "SP${1000 + i}"
            val unit = units[Random.nextInt(units.size)]
            val price = Random.nextDouble(10000.0, 5000000.0)
            val category = categories[Random.nextInt(categories.size)].name
            val description = "Mô tả chi tiết cho $name."
            val imageUri: String? = null

            products.add(Product(code = code, name = name, unit = unit, price = price,
                category = category, description = description, imageUri = imageUri))
        }
        products.forEach { db.productDao().insertProduct(it) }
        return db.productDao().getAllProducts()
    }

    // Hàm chèn 20 đơn hàng mẫu
    private suspend fun seedOrders(db: AppDatabase, customers: List<Customer>, products: List<Product>) {
        // Chỉ chèn đơn hàng mẫu nếu database hiện không có đơn hàng nào
        if (db.orderDao().getAllOrders().isNotEmpty()) {
            return
        }

        // db.orderDao().deleteAllOrders() // Bỏ comment để tránh xóa dữ liệu người dùng
        // db.orderDao().deleteAllOrderDetails() // Bỏ comment để tránh xóa dữ liệu người dùng


        if (customers.isEmpty() || products.isEmpty()) {
            return // Không thể tạo đơn hàng nếu không có khách hàng hoặc sản phẩm
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        val random = Random(System.currentTimeMillis())

        for (i in 0 until 20) { // Tạo 20 đơn hàng
            val customer = customers[random.nextInt(customers.size)]
            var totalAmount = 0.0
            val orderDetailsForThisOrder = mutableListOf<OrderDetail>()

            val numberOfItems = random.nextInt(1, 4) // Mỗi đơn hàng có từ 1 đến 3 sản phẩm

            for (j in 0 until numberOfItems) {
                val product = products[random.nextInt(products.size)]
                val quantity = random.nextInt(1, 5) // Số lượng từ 1 đến 4
                val price = product.price ?: 0.0
                val total = quantity * price
                totalAmount += total

                orderDetailsForThisOrder.add(
                    OrderDetail(
                        orderId = 0, // Sẽ được cập nhật sau khi Order được insert
                        productId = product.id,
                        productName = product.name,
                        quantity = quantity,
                        price = price,
                        total = total
                    )
                )
            }

            val order = Order(
                customerId = customer.id,
                customerName = customer.name,
                date = dateFormat.format(Date(System.currentTimeMillis() - random.nextLong(0, 30L * 24 * 60 * 60 * 1000))), // Ngày trong 30 ngày gần nhất
                totalAmount = totalAmount,
                isVisibleInHistory = true // Mặc định hiển thị
            )

            val newOrderId = db.orderDao().insertOrder(order).toInt()

            orderDetailsForThisOrder.forEach {
                db.orderDao().insertOrderDetail(it.copy(orderId = newOrderId)) // Cập nhật orderId và chèn
            }
        }
    }
}
