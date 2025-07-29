package com.example.apidemo.network.client

import android.content.Context
import com.example.apidemo.network.NetworkConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

/**
 * Retrofit 工厂类
 * 负责创建和配置 Retrofit 实例
 */
object RetrofitFactory {
    
    /**
     * JSON 序列化配置
     */
    private val json = Json {
        // 忽略未知字段，避免因 API 返回额外字段导致解析失败
        ignoreUnknownKeys = true
        // 允许空值转换
        coerceInputValues = true
        // 使用默认值
        encodeDefaults = true
        // 美化输出（仅调试模式）
        prettyPrint = true
        // 宽松模式，允许一些不严格的 JSON 格式
        isLenient = true
    }
    
    /**
     * 创建 Retrofit 实例
     * @param context Android 上下文
     * @param baseUrl 基础 URL，默认使用配置中的 URL
     * @return 配置完成的 Retrofit 实例
     */
    fun create(
        context: Context,
        baseUrl: String = NetworkConfig.BASE_URL
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClientFactory.create(context))
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
    
    /**
     * 创建 API 服务实例的便捷方法
     * @param T API 服务接口类型
     * @param context Android 上下文
     * @param serviceClass API 服务接口 Class
     * @param baseUrl 基础 URL，可选
     * @return API 服务实例
     */
    inline fun <reified T> createService(
        context: Context,
        serviceClass: Class<T>,
        baseUrl: String = NetworkConfig.BASE_URL
    ): T {
        return create(context, baseUrl).create(serviceClass)
    }
} 