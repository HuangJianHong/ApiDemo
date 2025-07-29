package com.example.apidemo.network.model

import kotlinx.serialization.Serializable

/**
 * 用户数据模型
 */
@Serializable
data class User(
    val id: Int? = null,
    val name: String,
    val username: String,
    val email: String,
    val phone: String? = null,
    val website: String? = null,
    val address: Address? = null,
    val company: Company? = null
)

/**
 * 地址信息
 */
@Serializable
data class Address(
    val street: String? = null,
    val suite: String? = null,
    val city: String? = null,
    val zipcode: String? = null,
    val geo: Geo? = null
)

/**
 * 地理位置信息
 */
@Serializable
data class Geo(
    val lat: String? = null,
    val lng: String? = null
)

/**
 * 公司信息
 */
@Serializable
data class Company(
    val name: String? = null,
    val catchPhrase: String? = null,
    val bs: String? = null
) 