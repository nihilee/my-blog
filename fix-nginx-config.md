# Nginx配置修复指南

## 问题分析
你看到的URL编码目录名（如`2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/`）**不是问题**！这是Cryogen框架的正常行为，中文部分"博客框架详细使用指南"被编码为`%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97`是HTTP协议的标准要求，所有浏览器和服务器都应该能正确处理这种编码。

**真正的问题是Nginx配置不正确**，导致无法正确处理这些URL编码的中文路径。

## 修复步骤

### 1. 登录远程服务器
通过SSH登录到你的服务器：
```bash
ssh xuh@8.159.137.181
```

### 2. 修改Nginx配置文件
使用vi或nano编辑nginx配置文件：
```bash
sudo vi /etc/nginx/conf.d/my-blog.conf
```

### 3. 应用正确的配置
将配置文件修改为以下内容：
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

### 4. 验证配置语法
修改完成后，验证nginx配置语法是否正确：
```bash
sudo nginx -t
```

### 5. 重启Nginx服务
如果配置语法正确，重启nginx服务：
```bash
sudo systemctl restart nginx
```

### 6. 验证服务状态
确保nginx服务正常运行：
```bash
sudo systemctl status nginx
```

## 验证修复结果

### 1. 本地测试
在远程服务器上使用curl命令测试本地访问：
```bash
curl -I "http://localhost/blog/posts-output/2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/"
```
如果返回200 OK状态码，说明配置修复成功。

### 2. 浏览器访问
清除浏览器缓存后，通过浏览器访问：
```
http://8.159.137.181/blog/posts-output/2026-01-08-cryogen-%E5%8D%9A%E5%AE%A2%E6%A1%86%E6%9E%B6%E8%AF%A6%E7%BB%86%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97/
```

## 其他可能的问题排查

### 检查文件权限
确保文件权限正确，nginx用户（通常是www-data）可以读取文件：
```bash
sudo chown -R www-data:www-data /var/www/my-blog/
sudo chmod -R 755 /var/www/my-blog/
```

### 重新同步文件
如果问题仍然存在，可以重新生成并同步文件：
```bash
# 在本地执行
lein run && rsync -avz --delete public/ xuh@8.159.137.181:/var/www/my-blog/
```

### 查看Nginx日志
如果仍然无法访问，可以查看nginx日志了解具体错误：
```bash
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log
```

## 总结
URL编码的目录名是正常的HTTP协议行为，真正的问题是nginx配置不正确。通过修改nginx配置文件，添加正确的charset设置和try_files指令，就可以解决404错误问题。