# IM System — 仿微信即时通讯系统

基于 Spring Boot + Vue 3 + WebSocket 的即时通讯系统。

> 🌐 **在线地址**：[https://im-system-one.vercel.app](https://im-system-one.vercel.app)

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.0 (Java 8) |
| 前端框架 | Vue 3 + Vite 5 |
| 数据库 | H2 (嵌入式文件数据库) |
| 实时通讯 | WebSocket (STOMP) |
| 认证 | JWT + Spring Security |
| 构建工具 | Maven |

## 项目结构

```
txxt/
├── src/main/java/com/im/    # 后端 Java 源码
├── src/main/resources/       # 后端配置 + 静态资源
│   ├── application.properties  # 应用配置
│   └── static/                 # 前端构建产物（打包后放置于此）
├── frontend/                   # 前端 Vue 3 源码
│   ├── src/
│   └── package.json
├── Dockerfile                  # Docker 构建文件
├── railway.toml                # Railway 部署配置
├── vercel.json                 # Vercel 前端部署配置
└── pom.xml                     # Maven 项目配置
```

---

## 部署方法

本项目支持三种部署方式，任选其一即可。

---

### 方式一：本地 / 服务器直接部署

#### 1. 环境要求

| 软件 | 最低版本 |
|------|----------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| Node.js (仅构建前端时需) | 18+ |
| npm | 9+ |

#### 2. 克隆项目并进入目录

```bash
git clone <仓库地址>
cd txxt
```

#### 3. 构建前端（如已构建可跳过）

前端需要先构建为静态文件，然后放入后端资源目录：

```bash
cd frontend
npm install
npm run build
cd ..
```

构建产物位于 `frontend/dist/` 目录。将构建产物复制到后端静态资源目录：

```bash
# Windows (PowerShell)
Copy-Item -Path frontend\dist\* -Destination src\main\resources\static\ -Recurse -Force

# Linux / macOS
cp -r frontend/dist/* src/main/resources/static/
```

#### 4. 构建后端并打包

在项目根目录下执行 Maven 打包（跳过测试以加速）：

```bash
mvn clean package -DskipTests
```

打包成功后，JAR 文件位于 `target/im-system-1.0.0.jar`。

#### 5. 运行应用

```bash
java -jar target/im-system-1.0.0.jar --server.port=8080
```

#### 6. 验证部署

- 浏览器打开 `http://localhost:8080`，应看到登录/注册页面
- 访问健康检查接口 `http://localhost:8080/api/health`，返回 `{"status":"UP",...}` 表示部署成功
- 可使用 H2 控制台查看数据库：`http://localhost:8080/h2-console`

#### 7. 可配置环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `PORT` | 服务端口 | `8080` |
| `DATA_DIR` | H2 数据库文件存放目录 | `/tmp/imdb` |
| `UPLOAD_DIR` | 文件上传目录 | `/tmp/uploads` |

例如指定端口和数据库目录：

```bash
java -jar target/im-system-1.0.0.jar --server.port=9090 --DATA_DIR=./data --UPLOAD_DIR=./uploads
```

---

### 方式二：Docker 部署

#### 1. 环境要求

- Docker 已安装并正常运行

#### 2. 构建 Docker 镜像

在项目根目录下（包含 Dockerfile 的目录）执行：

```bash
docker build -t im-system:1.0.0 .
```

构建过程说明：
- 第一阶段：使用 `maven:3.9.16-eclipse-temurin-8` 镜像编译打包
- 第二阶段：使用 `eclipse-temurin:8-jre` 运行镜像，仅保留 JAR 文件，镜像体积更小

#### 3. 运行容器

```bash
docker run -d \
  --name im-system \
  -p 8080:8080 \
  -v im-data:/app/data \
  -v im-uploads:/app/uploads \
  -e PORT=8080 \
  -e DATA_DIR=/app/data \
  -e UPLOAD_DIR=/app/uploads \
  im-system:1.0.0
```

参数说明：
- `-d`：后台运行
- `-p 8080:8080`：将容器的 8080 端口映射到宿主机
- `-v im-data:/app/data`：持久化数据库文件
- `-v im-uploads:/app/uploads`：持久化上传文件
- `-e`：设置环境变量

#### 4. 验证部署

- 浏览器打开 `http://localhost:8080`
- 访问 `http://localhost:8080/api/health`，返回 `{"status":"UP",...}` 即部署成功

#### 5. 常用 Docker 命令

```bash
# 查看容器状态
docker ps

# 查看日志
docker logs -f im-system

# 停止容器
docker stop im-system

# 启动已停止的容器
docker start im-system

# 删除容器（需先停止）
docker rm im-system
```

---

### 方式三：Railway 部署（推荐用于云端）

Railway 是一个支持 Dockerfile 一键部署的云平台。

#### 1. 准备工作

- [Railway](https://railway.app/) 账号
- 项目代码已推送到 GitHub 仓库

#### 2. 部署步骤

1. 登录 [Railway Dashboard](https://railway.app/dashboard)
2. 点击 **New Project** → 选择 **Deploy from GitHub repo**
3. 授权并选择本项目的 GitHub 仓库
4. Railway 会自动检测项目根目录的 `Dockerfile` 和 `railway.toml`，开始构建和部署
5. 等待构建完成（约 3-5 分钟），Railway 会自动分配一个公网域名

#### 3. 配置环境变量

在 Railway 项目的 **Variables** 页面添加以下变量（均已有默认值，可按需修改）：

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `PORT` | `8080` | Railway 会自动注入此变量，无需手动设置 |
| `DATA_DIR` | `/app/data` | 数据库文件路径 |
| `UPLOAD_DIR` | `/app/uploads` | 上传文件路径 |

#### 4. 配置持久化存储

Railway 容器重启后文件系统会重置，建议挂载 Volume：

1. 在项目页面选择 **Service** → **Settings** → **Volumes**
2. 添加两个挂载：
   - 挂载路径 `/app/data`（数据库文件）
   - 挂载路径 `/app/uploads`（上传文件）

#### 5. 验证部署

- 浏览器打开 Railway 分配的公网域名
- 访问 `https://<你的域名>/api/health`，返回 `{"status":"UP",...}` 表示部署成功

---

### 方式四：Vercel 部署（仅前端）

如果需要将前端和后端分离部署，可使用 Vercel 部署前端。

#### 1. 准备工作

- [Vercel](https://vercel.com/) 账号
- 后端已单独部署并可通过公网访问

#### 2. 部署步骤

1. 登录 [Vercel Dashboard](https://vercel.com/dashboard)
2. 点击 **Add New** → **Project**
3. 导入 GitHub 仓库
4. **关键配置**：将 **Root Directory** 设置为 `frontend`
5. 框架会自动识别为 Vite，保持默认设置
6. 点击 **Deploy**

#### 3. 配置 API 地址

前端需要知道后端 API 地址。部署完成后，在 Vercel 项目设置中添加环境变量或在 [config.js](frontend/dist/config.js) 中配置 `apiBase`：

```javascript
window.__APP_CONFIG__ = {
  apiBase: "https://你的后端域名"  // 后端部署地址
};
```

#### 4. 验证部署

浏览器打开 Vercel 分配的域名（如 `https://xxx.vercel.app`），应能看到登录页面并能正常调用后端 API。

> **注意**：Vercel 仅托管前端静态资源，需要保证后端服务已独立运行且允许前端域名的跨域请求（本项目已配置 `CorsConfiguration` 允许所有来源）。

---

## 部署验证清单

部署完成后，按以下项目逐一检查：

- [ ] `/api/health` 返回 `{"status":"UP",...}`
- [ ] `/` 首页能正常显示登录/注册页面
- [ ] `/api/info` 返回系统信息（含 WebSocket 地址）
- [ ] 可以注册新用户
- [ ] 可以用已注册的用户登录
- [ ] WebSocket 连接正常（登录后实时消息功能可用）
- [ ] H2 控制台可访问 `/h2-console`

---

## 常见问题

### Q: 启动时报端口被占用
```bash
# 查看占用端口的进程（Linux/macOS）
lsof -i :8080
# Windows
netstat -ano | findstr :8080

# 换一个端口启动
java -jar target/im-system-1.0.0.jar --server.port=9090
```

### Q: 数据库文件在哪里？
默认在 `/tmp/imdb` 目录下（Linux/macOS），Windows 下在 `C:\Users\<用户名>\AppData\Local\Temp\imdb`。可通过 `DATA_DIR` 环境变量自定义。

### Q: Docker 容器内数据如何持久化？
使用 Docker Volume 挂载 `/app/data` 和 `/app/uploads` 目录，参见方式二的运行命令。

### Q: 前端修改后如何生效？
修改前端代码后需重新构建并复制到 `src/main/resources/static/`，然后重新打包后端。

### Q: Railway 部署后访问 502/503
检查 Railway 日志输出，常见原因是：
- 端口监听不正确 — 确保使用 `--server.port=${PORT:-8080}`
- 健康检查失败 — Railway 会检测 `/api/health`，确保它返回 200

---

## 一键部署命令汇总

```bash
# ===== 本地部署 =====
cd txxt
cd frontend && npm install && npm run build && cd ..
# 将 frontend/dist/* 复制到 src/main/resources/static/
mvn clean package -DskipTests
java -jar target/im-system-1.0.0.jar

# ===== Docker 部署 =====
docker build -t im-system:1.0.0 .
docker run -d --name im-system -p 8080:8080 im-system:1.0.0

# ===== 本地验证 =====
curl http://localhost:8080/api/health

# ===== 线上验证 =====
curl https://im-system-one.vercel.app/api/health
```
