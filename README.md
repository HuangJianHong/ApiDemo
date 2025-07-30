# ApiDemo - Android Retrofit 网络层架构

这是一个完整的 Android 应用项目，展示了如何使用 **Retrofit** + **OkHttp** + **Kotlinx Serialization** 构建清晰、可维护的网络层架构。

## 🏗️ 架构概述

本项目采用分层架构设计，网络层包含以下组件：

```
app/src/main/java/com/example/apidemo/
├── network/
│   ├── NetworkConfig.kt                 # 网络配置常量
│   ├── api/
│   │   └── ApiService.kt               # API 接口定义
│   ├── client/
│   │   ├── OkHttpClientFactory.kt      # OkHttp 客户端工厂
│   │   └── RetrofitFactory.kt          # Retrofit 实例工厂
│   ├── interceptor/
│   │   ├── LoggingInterceptor.kt       # 日志拦截器
│   │   ├── HeaderInterceptor.kt        # 请求头拦截器
│   │   ├── RetryInterceptor.kt         # 网络重试拦截器
│   │   └── CacheInterceptor.kt         # 缓存拦截器
│   ├── model/
│   │   ├── User.kt                     # 用户数据模型
│   │   ├── Post.kt                     # 文章数据模型
│   │   └── Comment.kt                  # 评论数据模型
│   ├── repository/
│   │   └── ApiRepository.kt            # 数据仓库层
│   └── utils/
│       ├── NetworkResult.kt            # 网络结果封装
│       └── ApiExtensions.kt            # API 扩展函数
├── di/
│   └── NetworkModule.kt                # 依赖注入模块
└── example/
    └── NetworkUsageExample.kt          # 使用示例
```

## 🔧 技术栈

- **Retrofit 2.11.0** - REST API 客户端
- **OkHttp 4.12.0** - HTTP 客户端
- **Kotlinx Serialization 1.7.3** - JSON 序列化
- **Kotlin Coroutines** - 异步编程
- **Android Jetpack Compose** - 现代 UI 工具包

## 📦 核心功能

### 1. 网络配置 (`NetworkConfig`)
- 统一管理 API 基础 URL
- 配置超时时间和缓存大小
- 定义通用请求头常量

### 2. 拦截器系统
- **LoggingInterceptor**: 网络请求日志记录
- **HeaderInterceptor**: 自动添加通用请求头
- **RetryInterceptor**: 网络重试机制（丢包/超时时1秒后重试1次）
- **SmartCacheInterceptor**: 智能缓存策略（内存缓存 + 防重复请求）

### 3. 网络结果封装 (`NetworkResult`)
```kotlin
sealed class NetworkResult<out T> {
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int?, val message: String, val throwable: Throwable?) : NetworkResult<Nothing>()
}
```

### 4. 安全 API 调用
```kotlin
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T>
```

## 🚀 使用方法

### 1. 基本配置

项目已预配置完成，包含：
- 网络权限
- Retrofit 依赖
- JSON 序列化配置

### 2. 简单 API 调用

```kotlin
// 在 ViewModel 或 Repository 中
class MyViewModel(context: Context) : ViewModel() {
    private val repository = ApiRepository.getInstance(context)
    
    fun loadUsers() {
        viewModelScope.launch {
            when (val result = repository.getUsers()) {
                is NetworkResult.Loading -> {
                    // 显示加载状态
                }
                is NetworkResult.Success -> {
                    // 处理成功数据
                    val users = result.data
                }
                is NetworkResult.Error -> {
                    // 处理错误
                    val errorMessage = result.message
                }
            }
        }
    }
}
```

### 3. 链式调用

```kotlin
repository.getPosts()
    .onSuccess { posts ->
        // 处理成功
    }
    .onError { code, message ->
        // 处理错误
    }
```

### 4. 在 Compose 中使用

```kotlin
@Composable
fun UserListScreen(viewModel: MyViewModel) {
    val usersState by viewModel.usersState.collectAsState()
    
    when (usersState) {
        is NetworkResult.Loading -> {
            CircularProgressIndicator()
        }
        is NetworkResult.Success -> {
            LazyColumn {
                items(usersState.data) { user ->
                    UserItem(user = user)
                }
            }
        }
        is NetworkResult.Error -> {
            ErrorMessage(message = usersState.message)
        }
    }
}
```

## 🔍 API 示例

项目使用 [JSONPlaceholder](https://jsonplaceholder.typicode.com/) 作为示例 API，支持：

- **用户管理**: CRUD 操作
- **文章管理**: CRUD 操作
- **评论管理**: 读取操作

### 支持的端点

```kotlin
// 用户相关
GET    /users           # 获取所有用户
GET    /users/{id}      # 获取用户详情
POST   /users           # 创建用户
PUT    /users/{id}      # 更新用户
DELETE /users/{id}      # 删除用户

// 文章相关
GET    /posts           # 获取所有文章
GET    /posts/{id}      # 获取文章详情
GET    /posts?userId={id} # 获取用户文章
POST   /posts           # 创建文章
PUT    /posts/{id}      # 更新文章
DELETE /posts/{id}      # 删除文章

// 评论相关
GET    /comments         # 获取所有评论
GET    /posts/{id}/comments # 获取文章评论
```

## 🔄 网络重试机制

### 重试策略

**自动重试 (1秒延迟)**
- 检测到网络丢包、超时等异常时自动重试1次
- 延迟1秒后进行重试，避免频繁请求
- 支持递增延迟策略（第2次重试延迟2秒）

**支持重试的场景**
- 网络超时 (SocketTimeoutException)
- DNS解析失败 (UnknownHostException)
- 连接重置/拒绝 (Connection reset/refused)
- 网络不可达 (Network unreachable)
- HTTP错误状态码：408, 502, 503, 504

### 重试使用示例

```kotlin
// 重试机制会自动工作，无需额外代码
val result = repository.getUsers()
// 如果第一次请求失败（网络丢包等），会自动延迟1秒后重试1次

// 观察重试行为（通过日志）
// RetryInterceptor: 网络异常，准备重试: https://api.example.com/users
// RetryInterceptor: 延迟 1000ms 后重试...
// RetryInterceptor: 请求重试成功: https://api.example.com/users (重试次数: 1)
```

### 重试配置

可以通过修改 `RetryInterceptor` 中的常量来调整重试策略：

```kotlin
companion object {
    private const val MAX_RETRY_COUNT = 1        // 最大重试次数
    private const val RETRY_DELAY_MS = 1000L     // 重试延迟（毫秒）
}
```

## 🗄️ 智能缓存机制

### 缓存策略

**内存缓存 (10秒)**
- 缓存最近 10 秒的 GET 请求成功响应
- 优先从内存缓存读取，大幅提升响应速度
- 网络异常时可回退到过期缓存

**防重复请求 (1秒)**
- 1秒内相同参数的请求会被自动拦截
- 显示 Toast 提示"请求过于频繁，请稍后再试"
- 如有可用缓存则返回缓存数据，否则返回 429 错误

### 缓存使用示例

```kotlin
// 获取缓存管理器
val cacheManager = CacheManager.getInstance(context)

// 清空所有缓存
cacheManager.clearAllCache()

// 获取缓存统计信息
val stats = cacheManager.getCacheStats()
println("内存缓存条目数: ${stats.memoryCacheSize}")
println("防重复请求记录数: ${stats.recentRequestsSize}")

// 演示内存缓存
// 第一次请求从网络获取，第二次请求从缓存获取
val result1 = repository.getUsers() // 网络请求
delay(1100) // 等待超过防重复请求阈值
val result2 = repository.getUsers() // 缓存返回（速度更快）

// 演示防重复请求
val result1 = repository.getPosts() // 正常请求
val result2 = repository.getPosts() // 立即请求会被拦截并显示 Toast
```

### 缓存配置

可以通过修改 `SmartCacheInterceptor` 中的常量来调整缓存策略：

```kotlin
companion object {
    private const val MEMORY_CACHE_DURATION = 10 * 1000L // 内存缓存时间（毫秒）
    private const val DUPLICATE_REQUEST_THRESHOLD = 1 * 1000L // 防重复请求阈值（毫秒）
}
```

## 🛠️ 扩展指南

### 1. 添加新的 API 端点

1. 在 `ApiService.kt` 中添加新的接口方法
2. 在 `ApiRepository.kt` 中添加对应的仓库方法
3. 创建或更新相应的数据模型类

### 2. 添加认证

在 `HeaderInterceptor.kt` 中取消注释认证相关代码：

```kotlin
val token = getAuthToken()
if (token.isNotEmpty()) {
    requestBuilder.addHeader(NetworkConfig.Headers.AUTHORIZATION, "Bearer $token")
}
```

### 3. 自定义错误处理

在 `ApiExtensions.kt` 中的 `handleApiResponse` 方法中添加自定义错误处理逻辑。

### 4. 添加新的拦截器

1. 创建新的拦截器类实现 `Interceptor` 接口
2. 在 `OkHttpClientFactory.kt` 中添加到拦截器链

## 📝 最佳实践

1. **单一职责**: 每个类都有明确的职责
2. **依赖注入**: 使用单例模式管理网络组件
3. **错误处理**: 统一的错误处理和用户友好的错误信息
4. **线程安全**: 使用双重检查锁定确保线程安全
5. **缓存策略**: 智能缓存减少网络请求
6. **日志记录**: 完整的网络请求日志便于调试

## 🧪 测试

项目结构支持轻松进行单元测试和集成测试：

- Repository 层可以模拟 ApiService
- 网络层与 UI 层解耦
- 使用 `NetworkResult` 便于测试不同状态

## 📚 相关文档

- [Retrofit 官方文档](https://square.github.io/retrofit/)
- [OkHttp 官方文档](https://square.github.io/okhttp/)
- [Kotlinx Serialization 官方文档](https://github.com/Kotlin/kotlinx.serialization)
- [Android 网络安全配置](https://developer.android.com/training/articles/security-config)

---

这个架构为 Android 应用提供了可扩展、可维护的网络层基础，可以根据项目需求进行进一步定制和扩展。 