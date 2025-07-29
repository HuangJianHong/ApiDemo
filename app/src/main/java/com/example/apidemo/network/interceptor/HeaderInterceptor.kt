package com.example.apidemo.network.interceptor

import com.example.apidemo.BuildConfig
import com.example.apidemo.network.NetworkConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 请求头拦截器
 * 自动为所有请求添加通用请求头
 */
class HeaderInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requestBuilder = originalRequest.newBuilder()
            .addHeader(NetworkConfig.Headers.CONTENT_TYPE, NetworkConfig.Headers.APPLICATION_JSON)
            .addHeader(NetworkConfig.Headers.ACCEPT, NetworkConfig.Headers.APPLICATION_JSON)
            .addHeader(NetworkConfig.Headers.USER_AGENT, "ApiDemo/${BuildConfig.VERSION_NAME}")
        
        // 可以在这里添加认证 token
        // val token = getAuthToken()
        // if (token.isNotEmpty()) {
        //     requestBuilder.addHeader(NetworkConfig.Headers.AUTHORIZATION, "Bearer $token")
        // }
        
        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
    
    // 示例：获取认证 token 的方法
    // private fun getAuthToken(): String {
    //     // 从 SharedPreferences 或其他存储中获取 token
    //     return ""
    // }
} 