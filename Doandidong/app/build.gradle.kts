plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Plugin để xử lý annotation cho Room
}

android {
    // Sửa namespace thành "com.example.crm" để khớp với cấu trúc thư mục code
    namespace = "com.example.crm"
    compileSdk = 34

    defaultConfig {
        // Sửa applicationId thành "com.example.crm"
        applicationId = "com.example.crm"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    // Kích hoạt ViewBinding để tự động tạo lớp binding cho các layout XML.
    viewBinding {
        enable = true
    }
}

dependencies {
    // Các thư viện mặc định
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Thư viện này có thể không dùng nhưng cứ để
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // --- Thư viện Room Database ---
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version") // Bắt buộc cho Kotlin
    implementation("androidx.room:room-ktx:$room_version") // Hỗ trợ Coroutines cho Room

    // --- Thư viện Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // --- Thư viện cho RecyclerView ---
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // --- Thư viện ViewModel KTX để sử dụng "by viewModels()" ---
    implementation("androidx.activity:activity-ktx:1.8.2")
}
