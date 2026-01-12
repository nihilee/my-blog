;;;; Cryogen 博客框架核心模块
;;;; 主要功能：提供博客资源编译的命令行入口点
(ns cryogen.core
  (:require [cryogen-core.compiler :refer [compile-assets-timed]] ;; 带时间统计的资源编译函数
            [cryogen-core.plugins :refer [load-plugins]])) ;; 插件加载函数

;; 命令行入口函数
;; 用于编译博客资源并退出
;; 主要使用场景：生产环境构建、CI/CD流程、手动编译
(defn -main []
  (load-plugins) ;; 加载所有已配置的插件
  (compile-assets-timed) ;; 编译所有博客资源（带时间统计）
  (System/exit 0)) ;; 编译完成后退出程序
