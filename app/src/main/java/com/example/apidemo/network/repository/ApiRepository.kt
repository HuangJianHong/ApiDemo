package com.example.apidemo.network.repository

import android.content.Context
import com.example.apidemo.network.api.ApiService
import com.example.apidemo.network.client.RetrofitFactory
import com.example.apidemo.network.model.Comment
import com.example.apidemo.network.model.Post
import com.example.apidemo.network.model.User
import com.example.apidemo.network.utils.NetworkResult
import com.example.apidemo.network.utils.safeApiCall

/**
 * API 仓库类
 * 封装网络请求逻辑，提供统一的数据访问接口
 * 使用仓库模式隔离数据源，便于测试和维护
 */
class ApiRepository private constructor(private val apiService: ApiService) {
    
    companion object {
        @Volatile
        private var INSTANCE: ApiRepository? = null
        
        /**
         * 获取仓库单例实例
         * 使用双重检查锁定确保线程安全
         * @param context Android 上下文
         * @return ApiRepository 实例
         */
        fun getInstance(context: Context): ApiRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val apiService = RetrofitFactory.createService(
                        context = context.applicationContext,
                        serviceClass = ApiService::class.java
                    )
                    val repository = ApiRepository(apiService)
                    INSTANCE = repository
                    repository
                }
            }
        }
        
        /**
         * 清理实例（主要用于测试）
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
    
    // ==================== 用户相关操作 ====================
    
    /**
     * 获取所有用户
     * @return NetworkResult<List<User>>
     */
    suspend fun getUsers(): NetworkResult<List<User>> = safeApiCall {
        apiService.getUsers()
    }
    
    /**
     * 根据 ID 获取用户详情
     * @param userId 用户 ID
     * @return NetworkResult<User>
     */
    suspend fun getUserById(userId: Int): NetworkResult<User> = safeApiCall {
        apiService.getUserById(userId)
    }
    
    /**
     * 创建新用户
     * @param user 用户对象
     * @return NetworkResult<User>
     */
    suspend fun createUser(user: User): NetworkResult<User> = safeApiCall {
        apiService.createUser(user)
    }
    
    /**
     * 更新用户信息
     * @param userId 用户 ID
     * @param user 用户对象
     * @return NetworkResult<User>
     */
    suspend fun updateUser(userId: Int, user: User): NetworkResult<User> = safeApiCall {
        apiService.updateUser(userId, user)
    }
    
    /**
     * 删除用户
     * @param userId 用户 ID
     * @return NetworkResult<Unit>
     */
    suspend fun deleteUser(userId: Int): NetworkResult<Unit> = safeApiCall {
        apiService.deleteUser(userId)
    }
    
    // ==================== 文章相关操作 ====================
    
    /**
     * 获取所有文章
     * @return NetworkResult<List<Post>>
     */
    suspend fun getPosts(): NetworkResult<List<Post>> = safeApiCall {
        apiService.getPosts()
    }
    
    /**
     * 根据 ID 获取文章详情
     * @param postId 文章 ID
     * @return NetworkResult<Post>
     */
    suspend fun getPostById(postId: Int): NetworkResult<Post> = safeApiCall {
        apiService.getPostById(postId)
    }
    
    /**
     * 根据用户 ID 获取文章列表
     * @param userId 用户 ID
     * @return NetworkResult<List<Post>>
     */
    suspend fun getPostsByUserId(userId: Int): NetworkResult<List<Post>> = safeApiCall {
        apiService.getPostsByUserId(userId)
    }
    
    /**
     * 创建新文章
     * @param post 文章对象
     * @return NetworkResult<Post>
     */
    suspend fun createPost(post: Post): NetworkResult<Post> = safeApiCall {
        apiService.createPost(post)
    }
    
    /**
     * 更新文章
     * @param postId 文章 ID
     * @param post 文章对象
     * @return NetworkResult<Post>
     */
    suspend fun updatePost(postId: Int, post: Post): NetworkResult<Post> = safeApiCall {
        apiService.updatePost(postId, post)
    }
    
    /**
     * 部分更新文章
     * @param postId 文章 ID
     * @param post 文章对象
     * @return NetworkResult<Post>
     */
    suspend fun patchPost(postId: Int, post: Post): NetworkResult<Post> = safeApiCall {
        apiService.patchPost(postId, post)
    }
    
    /**
     * 删除文章
     * @param postId 文章 ID
     * @return NetworkResult<Unit>
     */
    suspend fun deletePost(postId: Int): NetworkResult<Unit> = safeApiCall {
        apiService.deletePost(postId)
    }
    
    // ==================== 评论相关操作 ====================
    
    /**
     * 获取所有评论
     * @return NetworkResult<List<Comment>>
     */
    suspend fun getComments(): NetworkResult<List<Comment>> = safeApiCall {
        apiService.getComments()
    }
    
    /**
     * 根据文章 ID 获取评论列表
     * @param postId 文章 ID
     * @return NetworkResult<List<Comment>>
     */
    suspend fun getCommentsByPostId(postId: Int): NetworkResult<List<Comment>> = safeApiCall {
        apiService.getCommentsByPostId(postId)
    }
    
    /**
     * 根据 ID 获取评论详情
     * @param commentId 评论 ID
     * @return NetworkResult<Comment>
     */
    suspend fun getCommentById(commentId: Int): NetworkResult<Comment> = safeApiCall {
        apiService.getCommentById(commentId)
    }
} 