package com.example.apidemo.network.api

import com.example.apidemo.network.model.Post
import com.example.apidemo.network.model.User
import com.example.apidemo.network.model.Comment
import retrofit2.Response
import retrofit2.http.*

/**
 * API 服务接口
 * 定义所有的 REST API 端点
 * 使用 JSONPlaceholder 作为示例 API
 */
interface ApiService {
    
    // ==================== 用户相关 API ====================
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GET("users")
    suspend fun getUsers(): Response<List<User>>
    
    /**
     * 根据 ID 获取用户详情
     * @param userId 用户 ID
     * @return 用户详情
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<User>
    
    /**
     * 创建新用户
     * @param user 用户对象
     * @return 创建的用户
     */
    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>
    
    /**
     * 更新用户信息
     * @param userId 用户 ID
     * @param user 用户对象
     * @return 更新后的用户
     */
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") userId: Int, @Body user: User): Response<User>
    
    /**
     * 删除用户
     * @param userId 用户 ID
     */
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>
    
    // ==================== 文章相关 API ====================
    
    /**
     * 获取所有文章
     * @return 文章列表
     */
    @GET("posts")
    suspend fun getPosts(): Response<List<Post>>
    
    /**
     * 根据 ID 获取文章详情
     * @param postId 文章 ID
     * @return 文章详情
     */
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") postId: Int): Response<Post>
    
    /**
     * 根据用户 ID 获取文章列表
     * @param userId 用户 ID
     * @return 文章列表
     */
    @GET("posts")
    suspend fun getPostsByUserId(@Query("userId") userId: Int): Response<List<Post>>
    
    /**
     * 创建新文章
     * @param post 文章对象
     * @return 创建的文章
     */
    @POST("posts")
    suspend fun createPost(@Body post: Post): Response<Post>
    
    /**
     * 更新文章
     * @param postId 文章 ID
     * @param post 文章对象
     * @return 更新后的文章
     */
    @PUT("posts/{id}")
    suspend fun updatePost(@Path("id") postId: Int, @Body post: Post): Response<Post>
    
    /**
     * 部分更新文章
     * @param postId 文章 ID
     * @param post 文章对象
     * @return 更新后的文章
     */
    @PATCH("posts/{id}")
    suspend fun patchPost(@Path("id") postId: Int, @Body post: Post): Response<Post>
    
    /**
     * 删除文章
     * @param postId 文章 ID
     */
    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int): Response<Unit>
    
    // ==================== 评论相关 API ====================
    
    /**
     * 获取所有评论
     * @return 评论列表
     */
    @GET("comments")
    suspend fun getComments(): Response<List<Comment>>
    
    /**
     * 根据文章 ID 获取评论列表
     * @param postId 文章 ID
     * @return 评论列表
     */
    @GET("posts/{id}/comments")
    suspend fun getCommentsByPostId(@Path("id") postId: Int): Response<List<Comment>>
    
    /**
     * 根据 ID 获取评论详情
     * @param commentId 评论 ID
     * @return 评论详情
     */
    @GET("comments/{id}")
    suspend fun getCommentById(@Path("id") commentId: Int): Response<Comment>
} 