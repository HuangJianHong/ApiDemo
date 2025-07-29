package com.example.apidemo.network.utils

import android.util.Log
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * API 扩展函数
 * 提供便捷的网络请求调用和结果处理方法
 */

private const val TAG = "ApiExtensions"

/**
 * 安全执行网络请求
 * 统一处理异常和错误状态
 * @param apiCall 网络请求挂起函数
 * @return NetworkResult 封装的结果
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        handleApiResponse(response)
    } catch (throwable: Throwable) {
        Log.e(TAG, "API call failed", throwable)
        handleApiException(throwable)
    }
}

/**
 * 处理 API 响应
 * @param response Retrofit 响应对象
 * @return NetworkResult
 */
private fun <T> handleApiResponse(response: Response<T>): NetworkResult<T> {
    return if (response.isSuccessful) {
        response.body()?.let { data ->
            NetworkResult.Success(data)
        } ?: NetworkResult.Error(
            code = response.code(),
            message = "响应数据为空"
        )
    } else {
        val errorMessage = when (response.code()) {
            400 -> "请求参数错误"
            401 -> "未授权，请重新登录"
            403 -> "访问被拒绝"
            404 -> "请求的资源不存在"
            408 -> "请求超时"
            429 -> "请求过于频繁，请稍后重试"
            500 -> "服务器内部错误"
            502 -> "网关错误"
            503 -> "服务不可用"
            else -> response.message().ifBlank { "未知错误" }
        }
        
        NetworkResult.Error(
            code = response.code(),
            message = errorMessage
        )
    }
}

/**
 * 处理 API 异常
 * @param throwable 异常对象
 * @return NetworkResult.Error
 */
private fun handleApiException(throwable: Throwable): NetworkResult.Error {
    return when (throwable) {
        is UnknownHostException -> NetworkResult.Error(
            message = "网络连接失败，请检查网络设置",
            throwable = throwable
        )
        is SocketTimeoutException -> NetworkResult.Error(
            message = "网络请求超时，请稍后重试",
            throwable = throwable
        )
        is IOException -> NetworkResult.Error(
            message = "网络异常，请检查网络连接",
            throwable = throwable
        )
        else -> NetworkResult.Error(
            message = throwable.message ?: "未知错误",
            throwable = throwable
        )
    }
}

/**
 * Response 扩展函数：转换为 NetworkResult
 */
fun <T> Response<T>.toNetworkResult(): NetworkResult<T> {
    return handleApiResponse(this)
}

/**
 * NetworkResult 扩展函数：执行成功时的操作
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (data: T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * NetworkResult 扩展函数：执行失败时的操作
 */
inline fun <T> NetworkResult<T>.onError(action: (code: Int?, message: String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(code, message)
    }
    return this
}

/**
 * NetworkResult 扩展函数：执行加载时的操作
 */
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
} 