package com.example.apidemo.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 安全拦截器
 * 添加安全相关的请求头，确保HTTPS连接安全
 */
class SecurityInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // 验证是否使用HTTPS
        if (request.url.scheme != "https") {
            throw SecurityException("仅允许HTTPS连接，当前请求: ${request.url}")
        }
        
        // 添加安全相关的请求头
        val secureRequest = request.newBuilder()
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Cache-Control", "no-cache")
            .build()
        
        val response = chain.proceed(secureRequest)
        
        // 验证响应的安全性
        validateResponse(response)
        
        return response
    }
    
    /**
     * 验证响应的安全性
     */
    private fun validateResponse(response: Response) {
        // 验证是否通过HTTPS接收响应
        if (response.request.url.scheme != "https") {
            throw SecurityException("响应必须通过HTTPS接收")
        }
        
        // 可以添加更多安全验证逻辑
        // 例如验证响应头中的安全策略
    }
} 