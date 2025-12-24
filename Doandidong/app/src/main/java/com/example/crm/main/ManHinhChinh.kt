package com.example.crm.main // Khai báo package chứa file

import android.content.Context
import android.content.Intent // Import lớp Intent để thực hiện việc chuyển màn hình
import android.os.Bundle // Import Bundle để xử lý trạng thái của activity
import android.view.MenuItem // Import lớp MenuItem để xử lý các mục trong menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle // Import lớp để tạo nút hamburger
import androidx.appcompat.app.AppCompatActivity // Import lớp AppCompatActivity làm lớp cơ sở
import androidx.appcompat.widget.Toolbar // Import Toolbar widget
import androidx.core.view.GravityCompat // Import để sử dụng hằng số cho gravity
import androidx.drawerlayout.widget.DrawerLayout // Import DrawerLayout widget
import com.example.crm.do_duc_anh.ManHinhDanhSachKhach // Import màn hình danh sách khách hàng
import com.example.crm.le_hong_son.ManHinhDangNhap // Import màn hình đăng nhập
import com.example.crm.le_hong_son.ManHinhDanhSachDanhMuc
import com.example.crm.mai_duc_phi_son.ManHinhLichSu
import com.example.crm.nguyen_van_dai.ManHinhSanPham
import com.example.crm.phan_thanh_tai.ManHinhTaoDonHang
import com.example.crm.phan_thanh_tai.ManHinhQuanLyDonHang
import com.example.crm.R
import com.example.crm.database.DataSeeder // Import DataSeeder
import com.example.crm.main.ManHinhDashboard
import com.google.android.material.navigation.NavigationView // Import NavigationView widget
import kotlinx.coroutines.launch // Import launch để sử dụng coroutine
import androidx.lifecycle.lifecycleScope // Import lifecycleScope để sử dụng trong Activity


// Lớp ManHinhChinh kế thừa từ AppCompatActivity và triển khai NavigationView.OnNavigationItemSelectedListener
class ManHinhChinh : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Khai báo biến cho DrawerLayout và NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // Hàm onCreate được gọi khi activity được tạo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Gọi hàm onCreate của lớp cha
        // Thiết lập layout cho activity từ file XML đã được thiết kế lại
        setContentView(R.layout.layout_man_hinh_chinh)

        // Gọi các hàm để khởi tạo view và thiết lập sự kiện
        setControl()
        setEvent()

        // Tải và hiển thị thông tin người dùng lên header của menu
        loadUserInfo()

        // --- KHỐI TẠO DỮ LIỆU MẪU ĐÃ BỊ XÓA ĐỂ TRÁNH GHI ĐÈ DỮ LIỆU NGƯỜI DÙNG ---
        // Dữ liệu mẫu sẽ không còn được tự động tạo mỗi khi vào màn hình chính.
         lifecycleScope.launch {
             DataSeeder.seedAllData(applicationContext)
             Toast.makeText(this@ManHinhChinh, "Đã kiểm tra và seed dữ liệu mẫu!", Toast.LENGTH_SHORT).show()
         }
        // --- KẾT THÚC ---
    }

    // Hàm setControl để ánh xạ và thiết lập các view
    private fun setControl() {
        // Ánh xạ DrawerLayout từ layout
        drawerLayout = findViewById(R.id.drawer_layout)
        // Ánh xạ Toolbar từ layout
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        // Thiết lập Toolbar làm ActionBar cho activity
        setSupportActionBar(toolbar)

        // Ánh xạ NavigationView từ layout
        navigationView = findViewById(R.id.nav_view)
    }

    // Hàm setEvent để thiết lập các listener
    private fun setEvent() {
        // Tạo đối tượng ActionBarDrawerToggle để liên kết DrawerLayout và Toolbar
        // Nó sẽ tạo ra icon hamburger và xử lý việc mở/đóng menu
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById(R.id.toolbar),
            R.string.navigation_drawer_open, // Chuỗi mô tả cho accessbility khi mở
            R.string.navigation_drawer_close // Chuỗi mô tả cho accessbility khi đóng
        )
        // Thêm toggle làm listener cho DrawerLayout
        drawerLayout.addDrawerListener(toggle)
        // Đồng bộ trạng thái của toggle (ví dụ: xoay icon hamburger)
        toggle.syncState()

        // Đăng ký listener cho các mục menu trong NavigationView
        navigationView.setNavigationItemSelectedListener(this)
    }

    // Hàm tải thông tin người dùng từ SharedPreferences và hiển thị lên UI
    private fun loadUserInfo() {
        // Mở file SharedPreferences với tên "CRM_USER_PREFS"
        val sharedPreferences = getSharedPreferences("CRM_USER_PREFS", Context.MODE_PRIVATE)
        // Lấy tên người dùng đã lưu, nếu không có thì mặc định là "Guest"
        val userName = sharedPreferences.getString("USER_NAME", "Guest")

        // Lấy view header từ NavigationView
        val headerView = navigationView.getHeaderView(0)
        // Tìm TextView trong header
        val tvUserName: TextView = headerView.findViewById(R.id.nav_header_user_name)
        // Thiết lập tên người dùng cho TextView
        tvUserName.text = userName
    }

    // Hàm được gọi khi một mục trong NavigationView được chọn
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Xử lý các sự kiện click vào từng mục menu dựa vào ID
        when (item.itemId) {
            R.id.nav_dashboard -> startActivity(Intent(this, ManHinhDashboard::class.java))
            R.id.nav_khach_hang -> startActivity(Intent(this, ManHinhDanhSachKhach::class.java))
            R.id.nav_san_pham -> startActivity(Intent(this, ManHinhSanPham::class.java))
            R.id.nav_danh_muc -> startActivity(Intent(this, ManHinhDanhSachDanhMuc::class.java))
            R.id.nav_don_hang -> startActivity(Intent(this, ManHinhQuanLyDonHang::class.java))
            R.id.nav_lich_su -> startActivity(Intent(this, ManHinhLichSu::class.java))
            R.id.nav_dang_xuat -> xuLyDangXuat() // Gọi hàm xử lý đăng xuất
        }
        // Đóng menu trượt sau khi một mục được chọn
        drawerLayout.closeDrawer(GravityCompat.START)
        return true // Trả về true để chỉ ra rằng sự kiện đã được xử lý
    }
    
    // Hàm xử lý logic đăng xuất
    private fun xuLyDangXuat() {
        // Mở SharedPreferences
        val sharedPreferences = getSharedPreferences("CRM_USER_PREFS", Context.MODE_PRIVATE)
        // Lấy editor để xóa dữ liệu
        val editor = sharedPreferences.edit()
        // Xóa tất cả dữ liệu đã lưu trong file prefs này
        editor.clear()
        // Áp dụng thay đổi
        editor.apply()

        // Hiển thị thông báo đăng xuất thành công
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

        // Quay về màn hình đăng nhập
        val intent = Intent(this, ManHinhDangNhap::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Đóng màn hình chính
    }

    // Ghi đè hàm onBackPressed để xử lý khi người dùng nhấn nút back
    override fun onBackPressed() {
        // Nếu menu đang mở, thì đóng nó lại
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            // Nếu không, thực hiện hành vi mặc định của nút back (thoát ứng dụng từ màn hình chính)
            super.onBackPressed()
        }
    }
}
