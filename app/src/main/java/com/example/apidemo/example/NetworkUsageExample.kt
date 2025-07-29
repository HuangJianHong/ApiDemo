package com.example.apidemo.example

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apidemo.di.NetworkModule
import com.example.apidemo.network.model.Post
import com.example.apidemo.network.model.User
import com.example.apidemo.network.repository.ApiRepository
import com.example.apidemo.network.utils.NetworkResult
import com.example.apidemo.network.utils.onError
import com.example.apidemo.network.utils.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 网络使用示例 ViewModel
 * 展示如何在 ViewModel 中使用配置好的网络层
 */
class NetworkExampleViewModel(context: Context) : ViewModel() {
    
    private val repository = NetworkModule.provideApiRepository(context)
    
    // UI 状态
    private val _usersState = MutableStateFlow<NetworkResult<List<User>>>(NetworkResult.Loading)
    val usersState: StateFlow<NetworkResult<List<User>>> = _usersState.asStateFlow()
    
    private val _postsState = MutableStateFlow<NetworkResult<List<Post>>>(NetworkResult.Loading)
    val postsState: StateFlow<NetworkResult<List<Post>>> = _postsState.asStateFlow()
    
    private val _createPostState = MutableStateFlow<NetworkResult<Post>?>(null)
    val createPostState: StateFlow<NetworkResult<Post>?> = _createPostState.asStateFlow()
    
    /**
     * 获取所有用户
     */
    fun loadUsers() {
        viewModelScope.launch {
            _usersState.value = NetworkResult.Loading
            _usersState.value = repository.getUsers()
        }
    }
    
    /**
     * 获取所有文章
     */
    fun loadPosts() {
        viewModelScope.launch {
            _postsState.value = NetworkResult.Loading
            _postsState.value = repository.getPosts()
        }
    }
    
    /**
     * 根据用户 ID 获取文章
     */
    fun loadPostsByUserId(userId: Int) {
        viewModelScope.launch {
            _postsState.value = NetworkResult.Loading
            _postsState.value = repository.getPostsByUserId(userId)
        }
    }
    
    /**
     * 创建新文章
     */
    fun createPost(title: String, body: String, userId: Int) {
        viewModelScope.launch {
            _createPostState.value = NetworkResult.Loading
            val newPost = Post(
                id = null,
                userId = userId,
                title = title,
                body = body
            )
            _createPostState.value = repository.createPost(newPost)
        }
    }
    
    /**
     * 清除创建文章状态
     */
    fun clearCreatePostState() {
        _createPostState.value = null
    }
    
    /**
     * 示例：组合多个网络请求
     */
    fun loadUserWithPosts(userId: Int) {
        viewModelScope.launch {
            // 先获取用户信息
            val userResult = repository.getUserById(userId)
            userResult.onSuccess { user ->
                Log.d("NetworkExample", "获取到用户: ${user.name}")
                // 再获取该用户的文章
                loadPostsByUserId(userId)
            }.onError { code, message ->
                Log.e("NetworkExample", "获取用户失败: $message")
            }
        }
    }
}

/**
 * 网络使用示例类
 * 展示各种使用场景和最佳实践
 */
object NetworkUsageExample {
    
    private const val TAG = "NetworkUsageExample"
    
    /**
     * 示例1：简单的 API 调用
     */
    suspend fun simpleApiCall(context: Context) {
        val repository = ApiRepository.getInstance(context)
        
        // 获取所有用户
        when (val result = repository.getUsers()) {
            is NetworkResult.Loading -> {
                Log.d(TAG, "正在加载用户...")
            }
            is NetworkResult.Success -> {
                Log.d(TAG, "成功获取 ${result.data.size} 个用户")
                result.data.forEach { user ->
                    Log.d(TAG, "用户: ${user.name} (${user.email})")
                }
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "获取用户失败: ${result.message}")
            }
        }
    }
    
    /**
     * 示例2：使用扩展函数链式调用
     */
    suspend fun chainedApiCall(context: Context) {
        val repository = ApiRepository.getInstance(context)
        
        repository.getPosts()
            .onSuccess { posts ->
                Log.d(TAG, "成功获取 ${posts.size} 篇文章")
            }
            .onError { code, message ->
                Log.e(TAG, "获取文章失败 ($code): $message")
            }
    }
    
    /**
     * 示例3：创建新资源
     */
    suspend fun createResourceExample(context: Context) {
        val repository = ApiRepository.getInstance(context)
        
        val newPost = Post(
            id = null,
            userId = 1,
            title = "我的新文章",
            body = "这是文章内容..."
        )
        
        repository.createPost(newPost)
            .onSuccess { createdPost ->
                Log.d(TAG, "文章创建成功，ID: ${createdPost.id}")
            }
            .onError { code, message ->
                Log.e(TAG, "文章创建失败: $message")
            }
    }
    
    /**
     * 示例4：批量操作
     */
    suspend fun batchOperationsExample(context: Context) {
        val repository = ApiRepository.getInstance(context)
        
        try {
            // 并发执行多个请求
            val usersResult = repository.getUsers()
            val postsResult = repository.getPosts()
            
            if (usersResult.isSuccess && postsResult.isSuccess) {
                val users = usersResult.getDataOrNull() ?: emptyList()
                val posts = postsResult.getDataOrNull() ?: emptyList()
                
                Log.d(TAG, "成功获取 ${users.size} 个用户和 ${posts.size} 篇文章")
                
                // 处理数据关联
                users.forEach { user ->
                    val userPosts = posts.filter { it.userId == user.id }
                    Log.d(TAG, "用户 ${user.name} 有 ${userPosts.size} 篇文章")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "批量操作失败", e)
        }
    }
}

/**
 * 在 Activity/Fragment 中使用 ViewModel 的示例代码
 */
class NetworkUsageActivity {
    
    companion object {
        /**
         * 在 Activity 中观察网络状态的示例代码
         * 注意：这只是示例代码结构，实际使用时需要在 Activity 中实现
         */
        fun observeNetworkStateExample() {
            /*
            // 在 Activity 的 onCreate 或 Fragment 的 onViewCreated 中
            
            // 初始化 ViewModel
            val viewModel = NetworkExampleViewModel(this)
            
            // 观察用户列表状态
            lifecycleScope.launch {
                viewModel.usersState.collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            // 显示加载指示器
                            showLoading(true)
                        }
                        is NetworkResult.Success -> {
                            // 隐藏加载指示器，显示数据
                            showLoading(false)
                            displayUsers(result.data)
                        }
                        is NetworkResult.Error -> {
                            // 隐藏加载指示器，显示错误
                            showLoading(false)
                            showError(result.message)
                        }
                    }
                }
            }
            
            // 观察文章列表状态
            lifecycleScope.launch {
                viewModel.postsState.collect { result ->
                    when (result) {
                        is NetworkResult.Loading -> {
                            showPostsLoading(true)
                        }
                        is NetworkResult.Success -> {
                            showPostsLoading(false)
                            displayPosts(result.data)
                        }
                        is NetworkResult.Error -> {
                            showPostsLoading(false)
                            showPostsError(result.message)
                        }
                    }
                }
            }
            
            // 观察创建文章状态
            lifecycleScope.launch {
                viewModel.createPostState.collect { result ->
                    result?.let {
                        when (it) {
                            is NetworkResult.Loading -> {
                                showCreateLoading(true)
                            }
                            is NetworkResult.Success -> {
                                showCreateLoading(false)
                                showSuccess("文章创建成功!")
                                viewModel.clearCreatePostState()
                                // 刷新文章列表
                                viewModel.loadPosts()
                            }
                            is NetworkResult.Error -> {
                                showCreateLoading(false)
                                showError("创建失败: ${it.message}")
                            }
                        }
                    }
                }
            }
            
            // 初始加载数据
            viewModel.loadUsers()
            viewModel.loadPosts()
            */
        }
    }
} 