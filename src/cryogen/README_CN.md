# Cryogen 核心文件说明

本文档详细说明 Cryogen 博客框架中 `server.clj` 和 `core.clj` 两个核心文件的功能、使用场景、最佳实践和修改方法。

## 1. 文件功能概述

### 1.1 server.clj
**主要功能**：提供开发服务器功能，支持实时预览、文件监听和自动重新编译。

**核心组件**：
- `init`：初始化函数，加载插件、编译资源、启动文件监听器
- `wrap-subdirectories`：URL处理中间件，支持不同的URL格式（干净URL、带尾斜杠等）
- `serve`：服务器启动函数，配置并启动开发服务器
- `-main`：命令行入口，解析参数并启动服务器

### 1.2 core.clj
**主要功能**：提供博客资源编译的命令行入口点。

**核心组件**：
- `-main`：命令行入口，加载插件、编译资源并退出

## 2. 使用场景

### 2.1 server.clj 的使用场景

1. **博客开发阶段**：
   - 实时预览内容变化
   - 自动编译修改的文件
   - 快速验证主题和布局修改

2. **本地调试**：
   - 调试URL路由和中间件
   - 测试不同的URL格式配置

3. **团队协作**：
   - 提供本地开发环境一致的预览体验
   - 快速分享开发进度

### 2.2 core.clj 的使用场景

1. **生产环境构建**：
   - 生成静态博客文件
   - 准备部署到服务器

2. **CI/CD流程**：
   - 自动构建博客
   - 集成到持续集成系统

3. **手动编译**：
   - 不启动服务器的情况下生成博客
   - 批量处理内容更新

## 3. 最佳实践

### 3.1 server.clj 最佳实践

1. **使用配置文件而非硬编码**：
   - 优先通过 `config.edn` 配置服务器行为
   - 避免直接修改源代码中的默认值

2. **合理使用快速模式**：
   - 开发阶段使用 `fast?` 参数提高编译速度
   - 完整编译时使用普通模式确保所有文件都被正确处理

3. **URL处理逻辑**：
   - 理解并保持不同 `clean-urls` 配置的兼容性
   - 修改URL逻辑时测试所有支持的URL格式

4. **中间件管理**：
   - 保持中间件链的清晰顺序
   - 添加自定义中间件时考虑性能影响

### 3.2 core.clj 最佳实践

1. **生产环境编译**：
   - 不添加开发环境配置
   - 确保编译产物适合生产环境

2. **插件管理**：
   - 只加载需要的插件
   - 测试插件兼容性

3. **错误处理**：
   - 生产环境编译时添加适当的错误处理
   - 确保编译失败时提供有用的错误信息

## 4. 修改方法

### 4.1 修改 server.clj 的方法

#### 4.1.1 添加自定义中间件

```clojure
;; 在现有中间件链中添加自定义中间件
(defn custom-middleware [handler]
  (fn [request]
    ;; 自定义中间件逻辑
    (handler request)))

;; 在wrap-subdirectories之后添加自定义中间件
def handler (-> (route/files "/")
               wrap-subdirectories
               custom-middleware)
```

#### 4.1.2 修改URL处理逻辑

```clojure
;; 修改wrap-subdirectories函数中的URL转换逻辑
(defn wrap-subdirectories
  [handler]
  (fn [request]
    (let [{:keys [clean-urls blog-prefix public-dest]} @resolved-config
          req-uri (.substring (:uri request) 1)
          ;; 自定义URL转换逻辑
          res-path (if (or (.endsWith req-uri "/")
                           (.endsWith req-uri ".html")
                           ;; 添加自定义URL条件
                           (.endsWith req-uri ".md"))
                     ;; 自定义URL转换规则
                     (condp = clean-urls
                       :trailing-slash (path req-uri "index.html")
                       :no-trailing-slash (path (str req-uri ".html"))
                       :dirty (path (str req-uri ".html")))
                     req-uri)
          ;; 其余逻辑保持不变
          ...)))
```

#### 4.1.3 添加自定义路由

```clojure
;; 在现有路由中添加自定义路由
defroutes routes
  (GET "/" [] (redirect (let [config (resolve-config)]
                          (path (:blog-prefix config)
                                (when (= (:clean-urls config) :dirty)
                                  "index.html")))))
  ;; 添加自定义路由
  (GET "/custom-page" [] "Custom page content")
  (route/not-found "Page not found"))
```

### 4.2 修改 core.clj 的方法

#### 4.2.1 添加编译前后处理

```clojure
(defn -main []
  (println "开始编译博客...")
  (load-plugins)
  ;; 添加编译前处理
  (println "加载插件完成，开始编译资源...")
  (compile-assets-timed)
  ;; 添加编译后处理
  (println "编译完成！")
  (System/exit 0))
```

#### 4.2.2 调整编译参数

```clojure
(defn -main []
  (load-plugins)
  ;; 添加自定义编译配置
  (compile-assets-timed {:hide-future-posts? true 
                         :minify-css? true})
  (System/exit 0))
```

## 5. 配置说明

### 5.1 server.clj 配置

| 配置项 | 说明 | 默认值 |
|-------|------|-------|
| `extra-config-dev` | 开发环境配置覆盖 | `{}` |
| `resolved-config` | 延迟加载的配置 | `(delay (resolve-config))` |

### 5.2 core.clj 配置

core.clj 没有直接的配置项，它使用项目根目录下的 `config.edn` 文件进行配置。

## 6. 常见问题与解决方案

### 6.1 URL 处理问题
**问题**：某些URL无法正确解析
**解决方案**：检查 `clean-urls` 配置，确保URL转换逻辑与配置一致。

### 6.2 自动编译失败
**问题**：修改文件后自动编译失败
**解决方案**：检查文件监听器配置，确保监听的目录正确，排除被忽略的文件。

### 6.3 编译性能问题
**问题**：编译速度太慢
**解决方案**：使用 `fast?` 参数启动服务器，只监听变化的文件。

## 7. 总结

`server.clj` 和 `core.clj` 是 Cryogen 框架的核心文件，分别负责开发服务器和资源编译功能。在使用和修改这些文件时，应遵循最佳实践，优先使用配置文件而非直接修改源代码，确保兼容性和可维护性。

通过合理使用这两个文件，可以提高博客开发效率，确保生产环境编译的稳定性和性能。