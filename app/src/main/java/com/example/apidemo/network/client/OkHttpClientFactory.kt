package com.example.apidemo.network.client

import android.content.Context
import com.example.apidemo.BuildConfig
import com.example.apidemo.network.NetworkConfig
import com.example.apidemo.network.cache.CacheManager
import com.example.apidemo.network.interceptor.HeaderInterceptor
import com.example.apidemo.network.interceptor.LoggingInterceptor
import com.example.apidemo.network.interceptor.RetryInterceptor
import com.example.apidemo.network.interceptor.SecurityInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

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
            addInterceptor(SecurityInterceptor()) // 安全拦截器（首先确保HTTPS）
            addInterceptor(HeaderInterceptor()) // 请求头拦截器
            addInterceptor(RetryInterceptor()) // 重试拦截器（在缓存之前，确保重试逻辑优先）
            addInterceptor(cacheManager.getSmartCacheInterceptor()) // 智能缓存拦截器（内存缓存 + 防重复请求）
            
            // 网络拦截器（用于网络层面的处理）
            if (enableLogging) {
                addNetworkInterceptor(LoggingInterceptor.create()) // 日志拦截器
            }
            
            // 连接池配置
            connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
            
            // 重试配置
            retryOnConnectionFailure(true)
            
            // SSL/TLS 安全配置
            configureSSLSecurity(this)
            
            // 协议配置 - 优先使用 HTTP/2，回退到 HTTP/1.1
            protocols(listOf(okhttp3.Protocol.HTTP_2, okhttp3.Protocol.HTTP_1_1))
            
        }.build()
    }
    
    /**
     * 配置 SSL/TLS 安全设置
     * @param builder OkHttpClient.Builder
     */
    private fun configureSSLSecurity(builder: OkHttpClient.Builder) {
        try {
            // 创建信任系统证书的 TrustManager
            val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as java.security.KeyStore?)
            
            val trustManagers = trustManagerFactory.trustManagers
            if (trustManagers.size == 1 && trustManagers[0] is X509TrustManager) {
                val trustManager = trustManagers[0] as X509TrustManager
                
                // 配置 SSL 上下文，使用 TLS 1.2 和 1.3
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustManagers, null)
                
                builder.sslSocketFactory(sslContext.socketFactory, trustManager)
            }
            
            // 配置连接规格，强制使用安全的 TLS 版本
            val connectionSpecs = listOf(
                okhttp3.ConnectionSpec.Builder(okhttp3.ConnectionSpec.MODERN_TLS)
                    .tlsVersions(okhttp3.TlsVersion.TLS_1_3, okhttp3.TlsVersion.TLS_1_2)
                    .build(),
                okhttp3.ConnectionSpec.Builder(okhttp3.ConnectionSpec.COMPATIBLE_TLS)
                    .tlsVersions(okhttp3.TlsVersion.TLS_1_2)
                    .build()
            )
            builder.connectionSpecs(connectionSpecs)
            
            // 强制主机名验证 - 使用默认的主机名验证器
            // 注意：不使用内部API，而是使用标准验证器
            
        } catch (e: Exception) {
            // 记录错误但不影响基本功能
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
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