package com.example.apidemo.network.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

/**
 * 网络重试拦截器
 * 在网络丢包、超时等情况下延迟1秒后重试1次
 */
class RetryInterceptor : Interceptor {
    
    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRY_COUNT = 1
        private const val RETRY_DELAY_MS = 1000L
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var lastException: IOException? = null
        var retryCount = 0
        
        while (retryCount <= MAX_RETRY_COUNT) {
            try {
                val response = chain.proceed(originalRequest)
                
                // 如果响应成功，直接返回
                if (response.isSuccessful) {
                    if (retryCount > 0) {
                        Log.i(TAG, "请求重试成功: ${originalRequest.url} (重试次数: $retryCount)")
                    }
                    return response
                }
                
                // 对于HTTP错误状态码，检查是否需要重试
                if (shouldRetryForHttpError(response.code)) {
                    response.close() // 关闭当前响应
                    if (retryCount < MAX_RETRY_COUNT) {
                        Log.w(TAG, "HTTP错误 ${response.code}，准备重试: ${originalRequest.url}")
                        delayRetry(retryCount)
                        retryCount++
                        continue
                    }
                }
                
                // 不需要重试或已达到最大重试次数
                return response
                
            } catch (e: IOException) {
                lastException = e
                
                // 检查是否是可重试的异常
                if (shouldRetryForException(e) && retryCount < MAX_RETRY_COUNT) {
                    Log.w(TAG, "网络异常，准备重试: ${originalRequest.url}, 异常: ${e.javaClass.simpleName}, 消息: ${e.message}")
                    delayRetry(retryCount)
                    retryCount++
                } else {
                    // 不可重试的异常或已达到最大重试次数
                    Log.e(TAG, "网络请求失败，不再重试: ${originalRequest.url}, 重试次数: $retryCount", e)
                    throw e
                }
            } catch (e: Exception) {
                // 非IOException类型的异常，不进行重试
                Log.e(TAG, "请求发生非网络异常，不进行重试: ${originalRequest.url}", e)
                throw e
            }
        }
        
        // 如果执行到这里，说明重试次数已达上限
        Log.e(TAG, "请求重试次数已达上限 ($MAX_RETRY_COUNT)，放弃重试: ${originalRequest.url}")
        throw lastException ?: IOException("请求重试失败")
    }
    
    /**
     * 判断是否应该针对HTTP错误状态码进行重试
     */
    private fun shouldRetryForHttpError(httpCode: Int): Boolean {
        return when (httpCode) {
            408, // Request Timeout
            502, // Bad Gateway
            503, // Service Unavailable
            504  // Gateway Timeout
            -> true
            else -> false
        }
    }
    
    /**
     * 判断是否应该针对异常进行重试
     */
    private fun shouldRetryForException(exception: IOException): Boolean {
        return when (exception) {
            is SocketTimeoutException -> {
                // 网络超时，可以重试
                true
            }
            is UnknownHostException -> {
                // DNS解析失败，可以重试（可能是临时网络问题）
                true
            }
            is SSLException -> {
                // SSL异常通常不应该重试，可能是证书问题
                false
            }
            else -> {
                // 其他IO异常，根据消息判断
                val message = exception.message?.lowercase() ?: ""
                when {
                    message.contains("timeout") -> true
                    message.contains("connection reset") -> true
                    message.contains("connection refused") -> true
                    message.contains("network is unreachable") -> true
                    message.contains("no route to host") -> true
                    message.contains("software caused connection abort") -> true
                    else -> false
                }
            }
        }
    }
    
    /**
     * 延迟重试
     */
    private fun delayRetry(retryCount: Int) {
        try {
            // 固定延迟1秒，符合需求：1秒后重试1次
            Log.d(TAG, "延迟 ${RETRY_DELAY_MS}ms 后重试...")
            TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("重试延迟被中断", e)
        }
    }
} 