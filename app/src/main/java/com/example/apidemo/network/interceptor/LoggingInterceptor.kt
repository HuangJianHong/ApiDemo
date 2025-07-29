package com.example.apidemo.network.interceptor

import android.util.Log
import com.example.apidemo.BuildConfig
import okhttp3.logging.HttpLoggingInterceptor

/**
 * 自定义日志拦截器
 * 提供格式化的网络请求日志输出
 */
object LoggingInterceptor {
    
    private const val TAG = "ApiDemo_Network"
    
    /**
     * 创建 HTTP 日志拦截器
     * @return HttpLoggingInterceptor
     */
    fun create(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            // 使用 Android Log 输出，方便在 Logcat 中查看
            Log.d(TAG, message)
        }.apply {
            // 根据构建类型设置日志级别
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY // 显示完整的请求和响应内容
            } else {
                HttpLoggingInterceptor.Level.NONE // 生产环境不显示日志
            }
        }
    }
} 