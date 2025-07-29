package com.example.apidemo.example

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apidemo.network.cache.CacheManager
import com.example.apidemo.network.repository.ApiRepository
import com.example.apidemo.network.utils.NetworkResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 缓存使用示例 ViewModel
 * 展示如何使用内存缓存和防重复请求功能
 */
class CacheUsageExampleViewModel(private val context: Context) : ViewModel() {
    
    private val repository = ApiRepository.getInstance(context)
    private val cacheManager = CacheManager.getInstance(context)
    
    private val _cacheStatsState = MutableStateFlow("缓存统计信息将在这里显示")
    val cacheStatsState: StateFlow<String> = _cacheStatsState.asStateFlow()
    
    private val _requestResultState = MutableStateFlow("请求结果将在这里显示")
    val requestResultState: StateFlow<String> = _requestResultState.asStateFlow()
    
    companion object {
        private const val TAG = "CacheUsageExample"
    }
    
    /**
     * 演示内存缓存功能
     * 连续请求同一个 API，第二次请求会从缓存返回
     */
    fun demonstrateMemoryCache() {
        viewModelScope.launch {
            _requestResultState.value = "开始演示内存缓存..."
            
            // 第一次请求 - 从网络获取
            Log.d(TAG, "执行第一次请求...")
            val startTime1 = System.currentTimeMillis()
            val result1 = repository.getUsers()
            val endTime1 = System.currentTimeMillis()
            
            when (result1) {
                is NetworkResult.Success -> {
                    val responseTime1 = endTime1 - startTime1
                    Log.d(TAG, "第一次请求成功，耗时: ${responseTime1}ms")
                    _requestResultState.value = "第一次请求成功，耗时: ${responseTime1}ms\n获取到 ${result1.data.size} 个用户"
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "第一次请求失败: ${result1.message}")
                    _requestResultState.value = "第一次请求失败: ${result1.message}"
                }
                else -> {}
            }
            
            // 等待1秒，避免触发防重复请求
            delay(1100)
            
            // 第二次请求 - 从内存缓存返回（应该很快）
            Log.d(TAG, "执行第二次请求...")
            val startTime2 = System.currentTimeMillis()
            val result2 = repository.getUsers()
            val endTime2 = System.currentTimeMillis()
            
            when (result2) {
                is NetworkResult.Success -> {
                    val responseTime2 = endTime2 - startTime2
                    Log.d(TAG, "第二次请求成功，耗时: ${responseTime2}ms（来自缓存）")
                    _requestResultState.value = _requestResultState.value + 
                            "\n\n第二次请求成功，耗时: ${responseTime2}ms（来自内存缓存）\n获取到 ${result2.data.size} 个用户"
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "第二次请求失败: ${result2.message}")
                    _requestResultState.value = _requestResultState.value + "\n\n第二次请求失败: ${result2.message}"
                }
                else -> {}
            }
            
            updateCacheStats()
        }
    }
    
    /**
     * 演示防重复请求功能
     * 快速连续请求同一个 API，第二次请求会被拦截并显示 Toast
     */
    fun demonstrateDuplicateRequestProtection() {
        viewModelScope.launch {
            _requestResultState.value = "开始演示防重复请求..."
            
            // 第一次请求
            Log.d(TAG, "执行第一次快速请求...")
            val result1 = repository.getPosts()
            
            when (result1) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "第一次快速请求成功")
                    _requestResultState.value = "第一次快速请求成功\n获取到 ${result1.data.size} 篇文章"
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "第一次快速请求失败: ${result1.message}")
                    _requestResultState.value = "第一次快速请求失败: ${result1.message}"
                }
                else -> {}
            }
            
            // 立即发起第二次请求（会被拦截）
            delay(100) // 很短的延迟，模拟用户快速点击
            Log.d(TAG, "立即执行第二次快速请求...")
            val result2 = repository.getPosts()
            
            when (result2) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "第二次快速请求返回缓存数据")
                    _requestResultState.value = _requestResultState.value + 
                            "\n\n第二次快速请求被拦截，返回缓存数据\n获取到 ${result2.data.size} 篇文章"
                }
                is NetworkResult.Error -> {
                    if (result2.code == 429) {
                        Log.d(TAG, "第二次快速请求被拦截: ${result2.message}")
                        _requestResultState.value = _requestResultState.value + 
                                "\n\n第二次快速请求被拦截: ${result2.message}"
                    } else {
                        Log.e(TAG, "第二次快速请求失败: ${result2.message}")
                        _requestResultState.value = _requestResultState.value + 
                                "\n\n第二次快速请求失败: ${result2.message}"
                    }
                }
                else -> {}
            }
            
            updateCacheStats()
        }
    }
    
    /**
     * 清空所有缓存
     */
    fun clearCache() {
        cacheManager.clearAllCache()
        _requestResultState.value = "缓存已清空"
        updateCacheStats()
        Log.d(TAG, "缓存已清空")
    }
    
    /**
     * 更新缓存统计信息
     */
    fun updateCacheStats() {
        val stats = cacheManager.getCacheStats()
        val statsText = """
            缓存统计信息:
            - 内存缓存条目数: ${stats.memoryCacheSize}
            - 防重复请求记录数: ${stats.recentRequestsSize}
            
            缓存策略:
            - 内存缓存: 10秒内的请求结果优先从内存获取
            - 防重复请求: 1秒内相同请求会被拦截并提示
        """.trimIndent()
        
        _cacheStatsState.value = statsText
        Log.d(TAG, "缓存统计: 内存缓存=${stats.memoryCacheSize}, 防重复记录=${stats.recentRequestsSize}")
    }
    
    /**
     * 模拟高频请求场景
     */
    fun simulateHighFrequencyRequests() {
        viewModelScope.launch {
            _requestResultState.value = "开始模拟高频请求场景..."
            
            repeat(5) { index ->
                Log.d(TAG, "执行第 ${index + 1} 次高频请求...")
                val startTime = System.currentTimeMillis()
                val result = repository.getUsers()
                val endTime = System.currentTimeMillis()
                val responseTime = endTime - startTime
                
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "第 ${index + 1} 次请求成功，耗时: ${responseTime}ms")
                        _requestResultState.value = _requestResultState.value + 
                                "\n第 ${index + 1} 次请求成功，耗时: ${responseTime}ms"
                    }
                    is NetworkResult.Error -> {
                        if (result.code == 429) {
                            Log.d(TAG, "第 ${index + 1} 次请求被拦截: ${result.message}")
                            _requestResultState.value = _requestResultState.value + 
                                    "\n第 ${index + 1} 次请求被拦截（频率限制）"
                        } else {
                            Log.e(TAG, "第 ${index + 1} 次请求失败: ${result.message}")
                            _requestResultState.value = _requestResultState.value + 
                                    "\n第 ${index + 1} 次请求失败: ${result.message}"
                        }
                    }
                    else -> {}
                }
                
                // 短暂延迟，模拟快速连续请求
                delay(200)
            }
            
            updateCacheStats()
        }
    }
}

/**
 * 缓存使用指南
 */
object CacheUsageGuide {
    
    /**
     * 缓存机制说明
     */
    const val CACHE_MECHANISM_GUIDE = """
        🚀 智能缓存机制说明:
        
        📦 内存缓存 (10秒)
        - 缓存最近 10 秒的 GET 请求成功响应
        - 优先从内存缓存读取，提升响应速度
        - 网络异常时可回退到过期缓存
        
        🚫 防重复请求 (1秒)
        - 1秒内相同参数的请求会被拦截
        - 显示 Toast 提示 "请求过于频繁，请稍后再试"
        - 如有可用缓存则返回缓存数据，否则返回 429 错误
        
        🔄 缓存清理
        - 自动清理过期的内存缓存
        - 自动清理过期的重复请求记录
        - 支持手动清空所有缓存
        
        ⚡ 性能优化
        - 使用 ConcurrentHashMap 保证线程安全
        - 最小化内存占用
        - 智能缓存键生成算法
    """
    
    /**
     * 使用建议
     */
    const val USAGE_TIPS = """
        💡 使用建议:
        
        ✅ 适合缓存的场景
        - GET 请求的列表数据
        - 用户信息、配置信息等相对稳定的数据
        - 频繁访问的参考数据
        
        ❌ 不适合缓存的场景  
        - 实时性要求高的数据
        - POST/PUT/DELETE 等修改操作
        - 包含敏感信息的响应
        
        🎯 最佳实践
        - 合理设置缓存时间
        - 在适当时机清空缓存
        - 监控缓存命中率
        - 处理缓存异常情况
    """
} 