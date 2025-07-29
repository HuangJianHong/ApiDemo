package com.example.apidemo.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 智能缓存拦截器
 * 实现内存缓存和防重复请求功能
 */
class SmartCacheInterceptor(private val context: Context) : Interceptor {
    
    companion object {
        private const val MEMORY_CACHE_DURATION = 10 * 1000L // 10秒
        private const val DUPLICATE_REQUEST_THRESHOLD = 1 * 1000L // 1秒
    }
    
    // 内存缓存：存储最近10秒的请求结果
    private val memoryCache = ConcurrentHashMap<String, CacheEntry>()
    
    // 防重复请求：记录最近1秒的请求
    private val recentRequests = ConcurrentHashMap<String, Long>()
    
    // 主线程 Handler，用于显示 Toast
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 缓存条目
     */
    private data class CacheEntry(
        val response: Response,
        val body: ByteArray,
        val timestamp: Long
    ) {
        fun isExpired(): Boolean {
            return System.currentTimeMillis() - timestamp > MEMORY_CACHE_DURATION
        }
        
        fun createResponse(request: Request): Response {
            return response.newBuilder()
                .request(request)
                .body(body.toResponseBody(response.body?.contentType()))
                .build()
        }
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val cacheKey = generateCacheKey(request)
        val currentTime = System.currentTimeMillis()
        
        // 1. 检查防重复请求
        val lastRequestTime = recentRequests[cacheKey]
        if (lastRequestTime != null && currentTime - lastRequestTime < DUPLICATE_REQUEST_THRESHOLD) {
            // 频繁请求，显示 Toast 提示
            showToast("请求过于频繁，请稍后再试")
            
            // 检查是否有缓存可以返回
            val cachedEntry = memoryCache[cacheKey]
            if (cachedEntry != null && !cachedEntry.isExpired()) {
                return cachedEntry.createResponse(request)
            }
            
            // 没有可用缓存，返回一个错误响应
            return createErrorResponse(request, "请求过于频繁")
        }
        
        // 2. 检查内存缓存（10秒内）
        val cachedEntry = memoryCache[cacheKey]
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return cachedEntry.createResponse(request)
        }
        
        // 3. 记录当前请求时间
        recentRequests[cacheKey] = currentTime
        
        // 4. 执行网络请求
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            // 网络错误时，尝试使用过期缓存
            if (cachedEntry != null) {
                return cachedEntry.createResponse(request)
            }
            throw e
        }
        
        // 5. 缓存响应（仅缓存成功的 GET 请求）
        val finalResponse = if (request.method == "GET" && response.isSuccessful) {
            cacheResponse(cacheKey, response)
        } else {
            response
        }
        
        // 6. 清理过期缓存
        cleanExpiredCache()
        
        return finalResponse
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(request: Request): String {
        val url = request.url.toString()
        val method = request.method
        val headers = request.headers.toMultimap()
            .filterKeys { it.lowercase() in listOf("authorization", "content-type") }
            .toString()
        
        return "$method:$url:$headers".hashCode().toString()
    }
    
    /**
     * 缓存响应
     */
    private fun cacheResponse(cacheKey: String, response: Response): Response {
        return try {
            val body = response.body
            if (body != null) {
                val bodyBytes = body.bytes()
                val cacheEntry = CacheEntry(
                    response = response,
                    body = bodyBytes,
                    timestamp = System.currentTimeMillis()
                )
                memoryCache[cacheKey] = cacheEntry
                
                // 重新创建响应体，因为原来的已经被读取
                val newBody = bodyBytes.toResponseBody(body.contentType())
                response.newBuilder().body(newBody).build()
            } else {
                response
            }
        } catch (e: Exception) {
            // 缓存失败不影响正常流程
            response
        }
    }
    
    /**
     * 清理过期缓存
     */
    private fun cleanExpiredCache() {
        val currentTime = System.currentTimeMillis()
        
        // 清理过期的内存缓存
        val expiredCacheKeys = memoryCache.filterValues { it.isExpired() }.keys
        expiredCacheKeys.forEach { memoryCache.remove(it) }
        
        // 清理过期的重复请求记录
        val expiredRequestKeys = recentRequests.filterValues { 
            currentTime - it > DUPLICATE_REQUEST_THRESHOLD 
        }.keys
        expiredRequestKeys.forEach { recentRequests.remove(it) }
    }
    
    /**
     * 显示 Toast 提示
     */
    private fun showToast(message: String) {
        mainHandler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 创建错误响应
     */
    private fun createErrorResponse(request: Request, message: String): Response {
        val errorBody = """{"error": "$message", "code": 429}"""
            .toResponseBody("application/json".toMediaType())
        
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(429) // Too Many Requests
            .message("Too Many Requests")
            .body(errorBody)
            .build()
    }
    
    /**
     * 检查网络是否可用
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 清空所有缓存
     */
    fun clearCache() {
        memoryCache.clear()
        recentRequests.clear()
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            memoryCacheSize = memoryCache.size,
            recentRequestsSize = recentRequests.size
        )
    }
    
    /**
     * 缓存统计信息
     */
    data class CacheStats(
        val memoryCacheSize: Int,
        val recentRequestsSize: Int
    )
} 