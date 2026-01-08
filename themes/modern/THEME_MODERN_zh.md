# 现代主题使用说明

## 主题概述

现代主题是为 Cryogen 博客框架设计的一款现代化、响应式主题，采用了简洁的布局结构、和谐的色彩搭配和良好的用户体验设计。

### 核心特性

- **现代化设计**：采用当前流行的设计语言和视觉风格
- **响应式布局**：完美适配桌面、平板和移动设备
- **深色/浅色模式**：支持切换主题模式，提升阅读体验
- **简洁排版**：使用 Inter 字体，清晰的层级结构
- **动画效果**：适当的过渡和动画，增强用户体验
- **易用性**：直观的导航和交互设计

## 目录结构

```
modern/
├── css/
│   └── style.css         # 主题样式文件
├── html/
│   ├── base.html         # 基础模板
│   ├── home.html         # 首页模板
│   ├── post.html         # 文章模板
│   ├── post-content.html # 文章内容模板
│   ├── tags.html         # 标签页面模板
│   ├── archives.html     # 归档页面模板
│   ├── author.html       # 作者页面模板
│   ├── authors.html      # 作者列表模板
│   └── previews.html     # 预览页面模板
├── js/
│   └── script.js         # JavaScript 交互功能
└── THEME_MODERN_zh.md    # 主题使用说明
```

## 配置与使用

### 启用主题

在 `content/config.edn` 文件中设置：

```edn
:theme "modern"
```

### 核心配置选项

在 `content/config.edn` 文件中可以配置以下与主题相关的选项：

```edn
{:site-title "你的博客标题"
 :site-description "你的博客描述"
 :author "作者名称"
 :blog-prefix "/blog"
 :rss-name "rss.xml"
 :rss-filters ["cryogen"]
 :share-buttons? true
 :disqus-shortname ""
 :google-analytics ""
 :theme "modern"
 :post-root "posts"
 :page-root "pages"
 :tag-root "tags"
 :author-root "authors"
 :previews? true
 :posts-per-page 5
 :clean-urls :trailing-slash
 :hide-future-posts? false
 :readmore-links? true
 :scheme "http"
 :host "localhost"
 :port 3000
 :highlightjs? true
 :highlightjs-theme "github-gist"
 :prismjs? false
 :prismjs-theme "prism-tomorrow"}
```

## 定制化

### 颜色定制

在 `themes/modern/css/style.css` 文件中，可以通过修改 CSS 变量来自定义主题颜色：

```css
:root {
  --primary-color: #4f46e5;      /* 主色调 */
  --primary-hover: #4338ca;      /* 主色调悬停 */
  --primary-light: #eef2ff;      /* 主色调浅色 */
  --text-primary: #1f2937;       /* 主要文字颜色 */
  --text-secondary: #6b7280;     /* 次要文字颜色 */
  --text-muted: #9ca3af;         /* -muted文字颜色 */
  --bg-primary: #ffffff;         /* 主要背景色 */
  --bg-secondary: #f9fafb;       /* 次要背景色 */
  --bg-tertiary: #f3f4f6;        /* 第三级背景色 */
  --border-color: #e5e7eb;       /* 边框颜色 */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}
```

### 字体定制

主题默认使用 Inter 字体，可通过修改 CSS 变量更改：

```css
:root {
  --font-family: 'Inter', sans-serif;
  --font-size-xs: 0.75rem;
  --font-size-sm: 0.875rem;
  --font-size-base: 1rem;
  --font-size-lg: 1.125rem;
  --font-size-xl: 1.25rem;
  --font-size-2xl: 1.5rem;
  --font-size-3xl: 1.875rem;
  --font-size-4xl: 2.25rem;
}
```

### 导航菜单定制

导航菜单在 `base.html` 模板中定义：

```html
<nav class="nav">
  <a href="{{uri}}/" class="nav-link">首页</a>
  <a href="{{uri}}/archives/" class="nav-link">归档</a>
  <a href="{{uri}}/tags/" class="nav-link">标签</a>
  <a href="{{uri}}/pages/about/" class="nav-link">关于</a>
</nav>
```

### 页脚定制

页脚内容在 `base.html` 模板中定义：

```html
<footer class="footer">
  <div class="container">
    <div class="footer-content">
      <p>&copy; {{year}} {{site-title}}. 保留所有权利.</p>
      <div class="footer-links">
        <a href="{{uri}}/" class="footer-link">首页</a>
        <a href="{{uri}}/archives/" class="footer-link">归档</a>
        <a href="{{uri}}/tags/" class="footer-link">标签</a>
      </div>
    </div>
  </div>
</footer>
```