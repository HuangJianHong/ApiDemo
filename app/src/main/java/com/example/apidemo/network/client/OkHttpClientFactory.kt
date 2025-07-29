package com.example.apidemo.network.client

import android.content.Context
import com.example.apidemo.BuildConfig
import com.example.apidemo.network.NetworkConfig
import com.example.apidemo.network.cache.CacheManager
import com.example.apidemo.network.interceptor.HeaderInterceptor
import com.example.apidemo.network.interceptor.LoggingInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * OkHttp 客户端工厂
 * 负责创建和配置 OkHttpClient 实例
 */
object OkHttpClientFactory {
    
    /**
     * 创建配置完整的 OkHttpClient 实例
     * @param context Android 上下文
     * @param enableLogging 是否启用日志，默认根据 BuildConfig.DEBUG 决定
     * @return 配置完成的 OkHttpClient
     */
    fun create(context: Context, enableLogging: Boolean = BuildConfig.DEBUG): OkHttpClient {
        val cacheManager = CacheManager.getInstance(context)
        
        return OkHttpClient.Builder().apply {
            // 超时配置
            connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            
            // HTTP 缓存配置（用于网络缓存）
            cache(createCache(context))
            
            // 添加拦截器（注意顺序很重要）
            addInterceptor(HeaderInterceptor()) // 请求头拦截器
            addInterceptor(cacheManager.getSmartCacheInterceptor()) // 智能缓存拦截器（内存缓存 + 防重复请求）
            
            // 网络拦截器（用于网络层面的处理）
            if (enableLogging) {
                addNetworkInterceptor(LoggingInterceptor.create()) // 日志拦截器
            }
            
            // 连接池配置
            connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
            
            // 重试配置
            retryOnConnectionFailure(true)
            
            // SSL 配置（生产环境需要更严格的配置）
            // 可以在这里添加 SSL 证书验证相关配置
            
        }.build()
    }
    
    /**
     * 创建网络缓存
     * @param context Android 上下文
     * @return Cache 实例
     */
    private fun createCache(context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, NetworkConfig.CACHE_SIZE)
    }
} 