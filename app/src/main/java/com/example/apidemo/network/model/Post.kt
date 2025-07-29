package com.example.apidemo.network.model

import kotlinx.serialization.Serializable

/**
 * 文章数据模型
 */
@Serializable
data class Post(
    val id: Int? = null,
    val userId: Int,
    val title: String,
    val body: String
) 