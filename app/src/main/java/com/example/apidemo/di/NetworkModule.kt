package com.example.apidemo.di

import android.content.Context
import com.example.apidemo.network.api.ApiService
import com.example.apidemo.network.client.RetrofitFactory
import com.example.apidemo.network.repository.ApiRepository

/**
 * 网络模块 - 依赖注入容器
 * 使用简单的单例模式管理网络相关依赖
 */
object NetworkModule {
    
    @Volatile
    private var apiService: ApiService? = null
    
    @Volatile
    private var apiRepository: ApiRepository? = null
    
    /**
     * 提供 API 服务实例
     * 使用双重检查锁定确保线程安全的单例
     * @param context Android 上下文
     * @return ApiService 实例
     */
    fun provideApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: run {
                val service = RetrofitFactory.createService(
                    context = context.applicationContext,
                    serviceClass = ApiService::class.java
                )
                apiService = service
                service
            }
        }
    }
    
    /**
     * 提供 API 仓库实例
     * @param context Android 上下文
     * @return ApiRepository 实例
     */
    fun provideApiRepository(context: Context): ApiRepository {
        return apiRepository ?: synchronized(this) {
            apiRepository ?: run {
                val repository = ApiRepository.getInstance(context.applicationContext)
                apiRepository = repository
                repository
            }
        }
    }
    
    /**
     * 清理所有实例
     * 通常在应用退出时调用，或在测试中使用
     */
    fun clearAll() {
        synchronized(this) {
            apiService = null
            apiRepository = null
            ApiRepository.clearInstance()
        }
    }
} 