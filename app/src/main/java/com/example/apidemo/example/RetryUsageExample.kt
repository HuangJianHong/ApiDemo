package com.example.apidemo.example

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apidemo.network.repository.ApiRepository
import com.example.apidemo.network.utils.NetworkResult
import com.example.apidemo.network.utils.onError
import com.example.apidemo.network.utils.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 重试功能使用示例
 * 展示如何测试和观察网络重试机制
 */
class RetryExampleViewModel(context: Context) : ViewModel() {
    
    private val repository = ApiRepository.getInstance(context)
    
    // 重试测试状态
    private val _retryTestState = MutableStateFlow<NetworkResult<String>?>(null)
    val retryTestState: StateFlow<NetworkResult<String>?> = _retryTestState.asStateFlow()
    
    /**
     * 测试网络重试功能
     * 这个方法会尝试访问一个可能失败的接口来观察重试行为
     */
    fun testNetworkRetry() {
        viewModelScope.launch {
            _retryTestState.value = NetworkResult.Loading
            
            Log.i("RetryExample", "开始测试网络重试功能...")
            
            // 使用真实的API调用来测试重试
            repository.getUsers()
                .onSuccess { users ->
                    val message = "重试测试成功，获取到 ${users.size} 个用户"
                    Log.i("RetryExample", message)
                    _retryTestState.value = NetworkResult.Success(message)
                }
                .onError { code, message ->
                    val errorMessage = "重试测试失败: $message (错误码: $code)"
                    Log.e("RetryExample", errorMessage)
                    _retryTestState.value = NetworkResult.Error(code = code, message = errorMessage)
                }
        }
    }
    
    /**
     * 测试超时重试
     * 通过获取大量数据来模拟可能的超时情况
     */
    fun testTimeoutRetry() {
        viewModelScope.launch {
            _retryTestState.value = NetworkResult.Loading
            
            Log.i("RetryExample", "开始测试超时重试功能...")
            
            // 同时发送多个请求来测试超时和重试
            repository.getPosts()
                .onSuccess { posts ->
                    val message = "超时重试测试成功，获取到 ${posts.size} 篇文章"
                    Log.i("RetryExample", message)
                    _retryTestState.value = NetworkResult.Success(message)
                }
                .onError { code, message ->
                    val errorMessage = "超时重试测试失败: $message (错误码: $code)"
                    Log.e("RetryExample", errorMessage)
                    _retryTestState.value = NetworkResult.Error(code = code, message = errorMessage)
                }
        }
    }
    
    /**
     * 清除测试状态
     */
    fun clearTestState() {
        _retryTestState.value = null
    }
}

/**
 * 重试使用示例对象
 * 提供静态方法来演示重试功能
 */
object RetryUsageExample {
    
    private const val TAG = "RetryUsageExample"
    
    /**
     * 演示如何观察重试行为
     * 通过日志可以观察到重试拦截器的工作过程
     */
    suspend fun demonstrateRetryBehavior(context: Context) {
        val repository = ApiRepository.getInstance(context)
        
        Log.i(TAG, "=== 开始演示重试功能 ===")
        
        try {
            // 正常情况下的请求（不会触发重试）
            Log.i(TAG, "1. 测试正常请求（不会触发重试）")
            repository.getUsers()
                .onSuccess { users ->
                    Log.i(TAG, "✅ 正常请求成功，获取到 ${users.size} 个用户")
                }
                .onError { code, message ->
                    Log.w(TAG, "❌ 正常请求失败: $message")
                }
            
            // 可能触发重试的请求（如果网络不稳定）
            Log.i(TAG, "2. 测试可能触发重试的请求")
            repository.getPosts()
                .onSuccess { posts ->
                    Log.i(TAG, "✅ 重试测试成功，获取到 ${posts.size} 篇文章")
                }
                .onError { code, message ->
                    Log.w(TAG, "❌ 重试测试失败: $message")
                }
                
        } catch (e: Exception) {
            Log.e(TAG, "演示过程中发生异常", e)
        }
        
        Log.i(TAG, "=== 重试功能演示结束 ===")
    }
    
    /**
     * 获取重试功能的配置信息
     */
    fun getRetryConfiguration(): String {
        return """
            📡 网络重试配置:
            • 最大重试次数: 1次
            • 重试延迟: 1秒后重试
            • 支持重试的异常:
              - SocketTimeoutException (网络超时)
              - UnknownHostException (DNS解析失败)
              - Connection reset/refused
              - Network unreachable
            • 支持重试的HTTP状态码:
              - 408 (Request Timeout)
              - 502 (Bad Gateway)  
              - 503 (Service Unavailable)
              - 504 (Gateway Timeout)
            • 重试策略: 递增延迟 (1s, 2s)
        """.trimIndent()
    }
    
    /**
     * 模拟网络问题来观察重试行为
     * 注意：这是一个教学示例，实际项目中不建议故意制造网络错误
     */
    fun simulateNetworkIssue(): String {
        return """
            🔧 如何观察重试行为:
            
            1. 开启调试日志，查看 'RetryInterceptor' 标签的日志
            2. 在网络不稳定的环境下运行应用
            3. 关注以下日志信息:
               • "网络异常，准备重试" - 表示检测到网络问题
               • "延迟 Xms 后重试" - 表示正在延迟重试
               • "请求重试成功" - 表示重试成功
               • "请求重试次数已达上限" - 表示重试失败
            
            4. 可以通过以下方式测试:
               • 临时断开WiFi然后快速重连
               • 在信号较弱的网络环境下使用
               • 使用网络代理工具模拟网络延迟/丢包
        """.trimIndent()
    }
} 