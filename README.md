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
- **CacheInterceptor**: 智能缓存策略

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