# Cryogen博客部署404错误排查指南

## 问题描述
在本地测试环境中，博客文章可以正常访问，但执行部署命令后，通过浏览器访问时出现"404 Not Found nginx/1.24.0 (Ubuntu)"错误。

## 最新分析：nginx配置问题
通过分析服务器上的nginx配置文件，发现以下几个关键问题：

1. **server_name配置错误**：
   - 当前配置：`server_name 1.1.1.1;`
   - 实际访问IP：`8.159.137.181`
   - 这导致nginx无法匹配到正确的server块

2. **缺少charset配置**：
   - 没有设置`charset utf-8;`
   - 导致nginx无法正确处理中文URL路径的编码和解码

3. **缺少try_files指令**：
   - location / 块中没有配置try_files指令
   - 当访问带中文的URL时，nginx无法正确解析和查找文件

4. **URL编码处理不完整**：
   - 没有确保nginx能正确处理URL编码的中文路径

## 修复后的nginx配置文件
```nginx
server {
    listen 80;
    server_name 8.159.137.181;  # 使用实际访问的公网IP

    root /var/www/my-blog;
    index index.html;
    
    # 关键：设置UTF-8编码，确保中文URL正确处理
    charset utf-8;

    # 设置正确的MIME类型
    types {
        text/html   html htm shtml;
        text/css    css;
        text/xml    xml;
        text/plain  txt;
        application/rss+xml rss xml;
        application/json json;
        application/javascript js;
        application/pdf pdf;
        application/octet-stream bin exe dll;
        image/gif   gif;
        image/jpeg  jpeg jpg;
        image/png   png;
        image/tiff  tiff;
        image/svg+xml svg;
    }

    # 关键：添加try_files指令，确保正确解析URL路径
    location / {
        try_files $uri $uri/ =404;
    }

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|xml|rss|pdf)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # 处理404错误
    error_page 404 /404.html;

    # 防止直接访问隐藏文件
    location ~ /\. {
        deny all;
    }
}
```

## 已确认的问题
1. **文章HTML文件已正确生成**：`/home/xuh/my-blog/public/blog/posts-output/2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/index.html`
2. **rsync命令配置正确**：源目录`public/`和目标目录`/var/www/my-blog/`末尾都有斜杠，会将public目录内容正确同步到目标目录
3. **本地文件权限正确**：index.html文件有644权限，所有用户都可以读取
4. **本地系统编码设置正确**：使用UTF-8编码，文件名URL编码是Cryogen框架的正常处理方式

## 可能的原因及解决方案

### 1. nginx配置错误
**检查项目**：
- nginx的root目录是否正确指向`/var/www/my-blog/`
- 是否有正确的location配置
- 是否启用了站点配置

**解决方案**：
在远程服务器上执行以下命令：
```bash
# 检查nginx配置文件
cat /etc/nginx/sites-available/my-blog

# 确保配置文件中包含正确的root目录
# 示例配置：
# server {
#     listen 80;
#     server_name example.com;
#     root /var/www/my-blog;
#     index index.html index.htm;
#     
#     location / {
#         try_files $uri $uri/ =404;
#     }
# }

# 检查是否启用了站点配置
ls -la /etc/nginx/sites-enabled/

# 如果未启用，执行以下命令启用
sudo ln -s /etc/nginx/sites-available/my-blog /etc/nginx/sites-enabled/

# 检查配置文件语法
nginx -t

# 重启nginx服务
sudo systemctl restart nginx
```

### 2. 远程服务器目录结构不正确
**检查项目**：
- 目标目录`/var/www/my-blog/`下是否包含`blog/`目录
- 文章文件是否正确同步到目标位置

**解决方案**：
在远程服务器上执行以下命令：
```bash
# 检查目标目录结构
ls -la /var/www/my-blog/
ls -la /var/www/my-blog/blog/posts-output/

# 确认文章文件是否存在
exists=$(find /var/www/my-blog -name "*.html" | grep -i cryogen)
if [ -z "$exists" ]; then
    echo "文章文件不存在，需要重新同步"
    # 在本地重新执行同步命令
    # lein run && rsync -avz --delete public/ xx@xxxx:/var/www/my-blog/
else
    echo "文章文件已存在：$exists"
fi
```

### 3. 文件权限和所有权问题
**检查项目**：
- 文件所有权是否属于nginx用户（通常是www-data）
- 文件权限是否允许nginx用户读取

**解决方案**：
在远程服务器上执行以下命令：
```bash
# 检查文件所有权
ls -la /var/www/my-blog/

# 更改文件所有权为nginx用户
sudo chown -R www-data:www-data /var/www/my-blog/

# 确保文件权限正确
sudo chmod -R 755 /var/www/my-blog/
```

### 4. 验证文件路径与URL匹配
**检查项目**：
- 确认文件在服务器上的实际路径与URL是否匹配
- 验证URL编码是否正确解析

**解决方案**：
在远程服务器上执行以下命令：
```bash
# 检查文件是否存在于预期路径
expected_path="/var/www/my-blog/blog/posts-output/2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/index.html"
if [ -f "$expected_path" ]; then
    echo "文件存在：$expected_path"
else
    echo "文件不存在：$expected_path"
    echo "检查目录结构："
    ls -la /var/www/my-blog/blog/posts-output/
fi

# 解码URL编码并检查路径
decoded_dir=$(python3 -c "import urllib.parse; print(urllib.parse.unquote('%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97'))")
echo "解码后的目录名：$decoded_dir"
ls -la /var/www/my-blog/blog/posts-output/ | grep -i "cryogen"

# 检查nginx是否能正确解析URL编码
echo "测试URL编码解析："
curl -I "http://localhost/blog/posts-output/2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/"
```

### 5. 中文文件名编码问题
**检查项目**：
- 远程服务器的系统编码是否支持UTF-8
- nginx是否正确处理URL编码的中文路径

**解决方案**：
在远程服务器上执行以下命令：
```bash
# 检查远程服务器的系统编码
locale

# 如果不是UTF-8，修改系统编码
# sudo update-locale LANG=en_US.UTF-8

# 检查nginx是否正确解码URL
# 在nginx配置文件中添加或确保包含以下配置：
# server {
#     charset utf-8;
#     ...
# }
```

### 5. 部署命令执行错误
**检查项目**：
- 确保`lein run`命令成功执行，生成了最新的HTML文件
- 确保rsync命令没有错误输出

**解决方案**：
在本地执行以下命令：
```bash
# 重新生成博客内容
lein run

# 执行rsync同步，查看详细输出
rsync -avz --delete public/ xx@xxxx:/var/www/my-blog/ --progress
```

## 验证部署结果
在完成上述检查和修复后，通过浏览器访问博客文章，如果仍然出现404错误，可以尝试以下步骤：

1. 清除浏览器缓存后重新访问
2. 使用curl命令在远程服务器上测试本地访问：
   ```bash
   curl http://localhost/blog/posts-output/2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/index.html
   ```
3. 检查nginx日志文件：
   ```bash
   sudo tail -f /var/log/nginx/error.log
   sudo tail -f /var/log/nginx/access.log
   ```

## 总结
部署后出现404错误通常是由于配置错误或文件同步问题导致的。通过检查nginx配置、目录结构、文件权限和系统编码，可以定位并修复大多数部署问题。确保在修改配置后重新启动nginx服务，并验证文件是否正确同步到目标位置。