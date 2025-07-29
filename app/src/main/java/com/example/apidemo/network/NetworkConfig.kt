package com.example.apidemo.network

/**
 * 网络配置类
 * 包含 API 基础 URL、超时时间等网络相关配置
 */
object NetworkConfig {
    
    // API 基础 URL - 使用 JSONPlaceholder 作为示例
    const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    
    // 超时配置（秒）
    const val CONNECT_TIMEOUT = 10L
    const val READ_TIMEOUT = 10L
    const val WRITE_TIMEOUT = 10L
    
    // 缓存配置
    const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
    
    // 请求头常量
    object Headers {
        const val CONTENT_TYPE = "Content-Type"
        const val APPLICATION_JSON = "application/json"
        const val AUTHORIZATION = "Authorization"
        const val USER_AGENT = "User-Agent"
        const val ACCEPT = "Accept"
        const val CACHE_CONTROL = "Cache-Control"
    }
    
    // API 版本
    const val API_VERSION = "v1"
    
    // 调试模式
    const val ENABLE_LOGGING = true
} 