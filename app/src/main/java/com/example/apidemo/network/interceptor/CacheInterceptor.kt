package com.example.apidemo.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.apidemo.network.NetworkConfig
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * 缓存拦截器
 * 处理网络缓存策略，在网络不可用时使用缓存
 */
class CacheInterceptor(private val context: Context) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        // 检查网络连接状态
        if (!isNetworkAvailable()) {
            // 无网络时，强制使用缓存
            val cacheControl = CacheControl.Builder()
                .onlyIfCached()
                .maxStale(7, TimeUnit.DAYS) // 缓存过期后仍可使用 7 天
                .build()
            
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }
        
        val response = chain.proceed(request)
        
        // 有网络时，设置缓存策略
        if (isNetworkAvailable()) {
            val cacheControl = CacheControl.Builder()
                .maxAge(1, TimeUnit.MINUTES) // 缓存有效期 1 分钟
                .build()
            
            return response.newBuilder()
                .header(NetworkConfig.Headers.CACHE_CONTROL, cacheControl.toString())
                .removeHeader("Pragma") // 移除可能影响缓存的头
                .build()
        } else {
            // 无网络时，使用较长的缓存时间
            val cacheControl = CacheControl.Builder()
                .maxStale(7, TimeUnit.DAYS)
                .build()
            
            return response.newBuilder()
                .header(NetworkConfig.Headers.CACHE_CONTROL, cacheControl.toString())
                .removeHeader("Pragma")
                .build()
        }
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
} 