# My Blog

## 博客概述

这是一个基于 [Cryogen](http://cryogenweb.org/) 静态博客框架搭建的个人技术博客，用于分享技术知识、编程经验和学习心得。博客使用 Clojure 构建，支持 Markdown 格式编写内容，生成纯静态 HTML 文件，便于部署和维护。

## 核心特点

- **静态生成**：将 Markdown 内容转换为纯静态 HTML 文件，部署简单快速
- **多主题支持**：内置多种主题，支持自定义样式
- **实时预览**：提供本地开发服务器，支持热重载功能
- **内容组织**：支持文章、页面、标签、归档等完整的博客功能
- **RSS 订阅**：自动生成 RSS 订阅源，方便读者订阅更新
- **评论系统**：支持集成 Disqus 评论系统（可配置）
- **社交媒体集成**：支持 Twitter 和 Mastodon 链接

## 内容结构

### 主要文章

- **[Cryogen 博客框架详细使用指南](/blog/posts-output/2026-01-08-cryogen-博客框架详细使用指南/)**
  - 框架概述与核心功能
  - 环境搭建与配置说明
  - 创建新博客流程
  - 主题系统与自定义
  - 高级功能与常见问题

### 技术分类

博客内容主要涵盖以下技术领域：
- Clojure 编程
- 静态网站开发
- 博客框架使用
- 技术文档撰写
- 软件开发实践

## 目录结构

```
my-blog/
├── content/                # 内容目录
│   ├── md/                 # Markdown 格式内容
│   │   ├── posts/          # 博客文章
│   │   └── pages/          # 独立页面
│   ├── css/                # 自定义 CSS
│   ├── img/                # 图片资源
│   └── config.edn          # 配置文件
├── themes/                 # 主题目录
├── src/                    # 源代码
├── public/                 # 生成的静态文件
└── README.md               # 博客说明文档
```

## 使用说明

### 环境要求

- Java 8 或更高版本
- Leiningen（Clojure 构建工具）

### 快速开始

1. **克隆或下载项目**

2. **启动本地预览服务器**
   ```bash
   lein serve:fast
   ```
   在浏览器中访问 `http://localhost:3000` 查看博客

3. **创建新文章**
   在 `content/md/posts/` 目录下创建新文件，命名格式为 `YYYY-MM-DD-文章标题.md`

4. **编译生成静态文件**
   ```bash
   lein run
   ```
   生成的静态文件将保存在 `public/` 目录中

### 常用命令

```bash
# 启动本地预览服务器（快速模式，支持热重载）
lein serve:fast

# 启动本地预览服务器（普通模式）
lein serve

# 编译生成静态文件
lein run

# 清理生成的文件
lein clean
```

## 配置说明

博客的主要配置文件是 `content/config.edn`，支持以下核心配置：

- 网站标题和描述
- 作者信息
- 主题选择
- RSS 订阅设置
- Disqus 评论配置
- 社交媒体链接

详细配置说明请参考 [Cryogen 官方文档](http://cryogenweb.org/)。

## 主题定制

### 使用现有主题

修改 `content/config.edn` 文件中的 `:theme` 配置项即可切换主题：

```edn
:theme "modern"  ; 可选：blue, blue_centered, nucleus, lotus, modern
```

### 自定义主题

1. 在 `themes/` 目录下创建新的主题目录
2. 复制现有主题的结构和文件
3. 修改 HTML 模板、CSS 和 JavaScript 文件
4. 在配置文件中启用新主题

## 部署方式

1. **编译生成静态文件**
   ```bash
   lein run
   ```

2. **部署到服务器**
   - 将 `public/` 目录中的所有文件上传到 Web 服务器
   - 支持部署到 GitHub Pages、Netlify、Vercel 等平台
   - 也可以使用 rsync 等工具部署到自己的服务器

   示例（使用 rsync 部署）：
   ```bash
   rsync -avz --delete public/ user@your-server.com:/var/www/blog/
   ```

## 更新日志

### 2026-01-08
- 博客初始化
- 创建 Cryogen 博客框架详细使用指南
- 配置 RSS 订阅功能
- 优化主题样式

## 联系方式

如果有任何问题或建议，欢迎通过以下方式联系：

- GitHub: [your-github-username](https://github.com/your-github-username)
- Email: your-email@example.com

## 许可证

本博客内容采用 [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/) 许可证。

---

**感谢阅读！** 希望本博客能为您提供有价值的技术内容。