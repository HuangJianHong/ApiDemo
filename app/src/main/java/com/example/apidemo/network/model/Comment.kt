package com.example.apidemo.network.model

import kotlinx.serialization.Serializable

/**
 * 评论数据模型
 */
@Serializable
data class Comment(
    val id: Int? = null,
    val postId: Int,
    val name: String,
    val email: String,
    val body: String
) 