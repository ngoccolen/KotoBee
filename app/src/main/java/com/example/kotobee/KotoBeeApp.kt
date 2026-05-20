package com.example.kotobee

import android.app.Application
import com.example.kotobee.di.AppContainer

class KotoBeeApp : Application() {
    // Khởi tạo container dùng chung cho toàn bộ ứng dụng
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}