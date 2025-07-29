package com.example.apidemo.network.cache

import android.content.Context
import com.example.apidemo.network.interceptor.SmartCacheInterceptor

/**
 * 缓存管理器
 * 提供统一的缓存控制接口
 */
class CacheManager private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: CacheManager? = null
        
        /**
         * 获取缓存管理器单例实例
         */
        fun getInstance(context: Context): CacheManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val manager = CacheManager(context.applicationContext)
                    INSTANCE = manager
                    manager
                }
            }
        }
    }
    
    private val smartCacheInterceptor = SmartCacheInterceptor(context)
    
    /**
     * 清空所有缓存
     */
    fun clearAllCache() {
        smartCacheInterceptor.clearCache()
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): SmartCacheInterceptor.CacheStats {
        return smartCacheInterceptor.getCacheStats()
    }
    
    /**
     * 获取智能缓存拦截器实例
     * 内部使用，用于 OkHttpClient 配置
     */
    internal fun getSmartCacheInterceptor(): SmartCacheInterceptor {
        return smartCacheInterceptor
    }
} 