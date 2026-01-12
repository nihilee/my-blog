;;;; Cryogen 博客框架服务器模块
;;;; 主要功能：提供开发服务器功能，支持实时预览、文件监听和自动重新编译
(ns cryogen.server
  (:require
    [compojure.core :refer [GET defroutes]]      ;; Web路由定义库
    [compojure.route :as route]                  ;; 路由处理辅助函数
    [ring.util.response :refer [redirect file-response]] ;; HTTP响应处理
    [ring.util.codec :refer [url-decode]]        ;; URL解码功能
    [ring.server.standalone :as ring-server]     ;; Ring独立服务器
    [cryogen-core.watcher :refer [start-watcher! start-watcher-for-changes!]] ;; 文件监听
    [cryogen-core.plugins :refer [load-plugins]] ;; 插件加载
    [cryogen-core.compiler :refer [compile-assets-timed]] ;; 资源编译
    [cryogen-core.config :refer [resolve-config]] ;; 配置解析
    [cryogen-core.io :refer [path]]              ;; 文件路径处理
    [clojure.string :as string])                 ;; 字符串处理
  (:import (java.io File)))                     ;; Java文件类

;; 延迟加载的已解析配置，避免启动时立即加载所有配置
(def resolved-config (delay (resolve-config)))

;; 开发环境额外配置
;; 用于在开发时覆盖默认配置，例如 `:hide-future-posts? false` 可以显示未来日期的文章
(def extra-config-dev
  "开发环境配置覆盖，用于调整开发时的行为"
  {})

;; 初始化函数
;; 加载插件、编译资源并启动文件监听器
;; 参数: fast? - 布尔值，是否使用快速编译模式
(defn init [& [fast?]]
  (load-plugins) ;; 加载所有插件
  (compile-assets-timed extra-config-dev) ;; 编译博客资源（带时间统计）
  (let [ignored-files (-> @resolved-config :ignored-files)] ;; 获取需要忽略的文件列表
    (run! ;; 对每个目录执行监听操作
      #(if fast?
         ;; 快速模式：只监听变化的文件
         (start-watcher-for-changes! % ignored-files compile-assets-timed extra-config-dev)
         ;; 普通模式：监听所有文件变化
         (start-watcher! % ignored-files compile-assets-timed))
      ["content" "themes"]))) ;; 监听内容目录和主题目录

;; 中间件：处理子目录请求和URL重写
;; 用于支持不同的URL格式（干净URL、带尾斜杠等）
(defn wrap-subdirectories
  [handler] ;; 传入下一个处理函数
  (fn [request] ;; 返回新的请求处理函数
    (let [{:keys [clean-urls blog-prefix public-dest]} @resolved-config ;; 获取配置
          req-uri (.substring (:uri request) 1) ;; 获取请求URI（去除开头的/）
          ;; 确定资源路径：根据URL格式规则转换请求URI
          res-path (if (or (.endsWith req-uri "/")
                           (.endsWith req-uri ".html")
                           (-> (string/split req-uri #"/") ;; 检查URI是否指向目录
                               last
                               (string/includes? ".") ;; 检查最后部分是否包含扩展名
                               not))
                     ;; 根据clean-urls配置确定资源路径
                     (condp = clean-urls
                       :trailing-slash (path req-uri "index.html") ;; 带尾斜杠格式
                       :no-trailing-slash (if (or (= req-uri "") ;; 无尾斜杠格式
                                                  (= req-uri "/")
                                                  (= req-uri
                                                     (if (string/blank? blog-prefix)
                                                       blog-prefix
                                                       (.substring blog-prefix 1))))
                                            (path req-uri "index.html")
                                            (path (str req-uri ".html")))
                       :dirty (path (str req-uri ".html"))) ;; 带扩展名格式
                     req-uri) ;; 如果是文件请求，直接使用原URI
          full-path (path public-dest res-path) ;; 完整文件路径
          file (File. full-path)] ;; 创建文件对象
      ;; 调试信息输出
      (println "DEBUG: req-uri:" req-uri)
      (println "DEBUG: res-path:" res-path)
      (println "DEBUG: full-path:" full-path)
      (println "DEBUG: file exists:" (.exists file))
      ;; 尝试返回文件响应
      (or (let [rsp (file-response res-path {:root public-dest})
                body (:body rsp)] ;; 获取响应体
            ;; 根据文件类型设置正确的Content-Type头
            (cond-> rsp
                    (and body
                         (instance? File body)
                         (string/ends-with? (.getName body) ".html"))
                    (assoc-in [:headers "Content-Type"] "text/html; charset=utf-8")
                    (and body
                         (instance? File body)
                         (string/ends-with? (.getName body) ".xml"))
                    (assoc-in [:headers "Content-Type"] "application/rss+xml; charset=utf-8")))
          ;; 如果文件不存在，交给下一个处理函数
          (handler request))))) 

;; 定义路由
(defroutes routes
  ;; 根路径重定向到博客首页
  (GET "/" [] (redirect (let [config (resolve-config)]
                          (path (:blog-prefix config)
                                (when (= (:clean-urls config) :dirty)
                                  "index.html")))))
  ;; 404页面处理
  (route/not-found "Page not found"))

;; 定义主处理器：结合子目录中间件和静态文件服务
(def handler (wrap-subdirectories (route/files "/")))

;; 服务器启动函数
;; 用于通过tools-deps运行开发服务器
;; 参数: opts - 选项映射，包括:
;;        :fast - 是否使用快速模式
;;        :join? - 是否阻塞当前线程
(defn serve
  "开发服务器入口点，用于启动博客预览服务器"
  [{:keys [fast join?] :as opts}]
  (ring-server/serve
    handler
    (merge
      {:join? (if (some? join?) join? true) ;; 默认阻塞当前线程
       :init (partial init fast) ;; 初始化函数
       :open-browser? false ;; 不自动打开浏览器
       :auto-refresh? fast ;; 快速模式下启用自动刷新
       :refresh-paths [(:public-dest @resolved-config)]} ;; 刷新路径
      opts))) ;; 合并用户选项

;; 命令行入口点
(defn -main [& args]
  (let [opts (set args)
        ;; 解析端口参数（格式：port=3000）
        port (or (some-> (filter #(re-matches #"port=\d+" %) opts)
                         first
                         (subs 5)
                         Integer/parseInt)
                 3000) ;; 默认端口3000
        fast? (opts "fast")] ;; 检查是否启用快速模式
    (serve {:port port :fast fast?}))) ;; 启动服务器

;; 开发调试示例
(comment
  ;; 启动非阻塞服务器（用于REPL开发）
  (def srv (serve {:join? false, :fast true}))
  ;; 停止服务器
  (.stop srv)

  ,)