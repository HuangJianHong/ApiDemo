package com.example.apidemo.network.utils

/**
 * 网络请求结果封装类
 * 统一处理网络请求的各种状态
 * @param T 数据类型
 */
sealed class NetworkResult<out T> {
    
    /**
     * 加载中状态
     */
    object Loading : NetworkResult<Nothing>()
    
    /**
     * 成功状态
     * @param data 返回的数据
     */
    data class Success<out T>(val data: T) : NetworkResult<T>()
    
    /**
     * 错误状态
     * @param code 错误码
     * @param message 错误信息
     * @param throwable 异常对象（可选）
     */
    data class Error(
        val code: Int? = null,
        val message: String,
        val throwable: Throwable? = null
    ) : NetworkResult<Nothing>()
    
    // 便捷的状态判断属性
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    
    /**
     * 获取数据，如果不是成功状态则返回 null
     */
    fun getDataOrNull(): T? = if (this is Success) data else null
    
    /**
     * 获取错误信息，如果不是错误状态则返回 null
     */
    fun getErrorMessageOrNull(): String? = if (this is Error) message else null
} 