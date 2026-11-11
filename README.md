# 墨卷书阁 · 图书管理系统（Students Library System）

一个用 **JSP + Servlet + JDBC + MySQL** 实现的图书管理系统，按 **图书板块 → 书架 → 图书** 三层组织藏书。包含用户注册登录、板块管理、图书增删改查、全馆搜索、排序与分页。

> 课程设计项目 · 可直接在 IntelliJ IDEA 中配合 Tomcat 9 运行。

---

## 界面预览

### 登录页
![登录页](docs/screenshots/login.png)

### 主页：图书板块
![主页](docs/screenshots/home.png)

### 板块书架
![板块书架](docs/screenshots/shelf.png)

### 图书详情
![图书详情](docs/screenshots/detail.png)

### 全馆搜索
![全馆搜索](docs/screenshots/search.png)

### 新建板块
![新建板块](docs/screenshots/category-form.png)

---

## 功能一览

| 模块 | 功能 |
| --- | --- |
| 用户 | 注册、登录、退出、PBKDF2 加盐密码、登录拦截 |
| 主页 | 欢迎语、馆藏统计、图书板块卡片、最近上架 |
| 板块 | 新建、编辑、删除板块，自定义 emoji 图标与主题色 |
| 书架 | 点击板块进入对应书架，只显示本板块图书 |
| 图书 | 新增、详情、编辑、删除，价格、库存、ISBN、出版社、简介 |
| 搜索 | 按书名、作者、ISBN、出版社跨板块搜索 |
| 排序 | 最近上架、最早上架、价格升降序、库存、书名 |
| 分页 | 每页 12 条，上一页 / 下一页 |
| 安全 | PreparedStatement 防 SQL 注入、JSTL 转义防 XSS、Session 登录校验、HttpOnly Cookie |

---

## 技术栈

- Java 17（实测 JDK 25 也可运行）
- JSP 2.3 / Servlet 4.0（`javax.servlet`，对应 **Tomcat 9**）
- JSTL 1.2
- MySQL 8.0 + mysql-connector-java 8.0.30
- 原生 JDBC + DAO 分层，不依赖 Spring / Maven
- IntelliJ IDEA 传统 Web 工程（WebContent + artifact exploded war）

> 注意：本项目使用 `javax.servlet`，请使用 **Tomcat 9.x**。Tomcat 10+ 改成了 `jakarta.servlet`，直接部署会报 `ClassNotFoundException`。

---

## 目录结构

```text
jspsweb/
├── src/
│   ├── db.properties                 # 数据库连接配置（改这里）
│   └── com/library/
│       ├── entity/                   # User / Category / Book / Page
│       ├── dao/                      # UserDao / CategoryDao / BookDao
│       ├── servlet/                  # Auth / Home / Category / Book / Search
│       ├── filter/                   # EncodingFilter / AuthFilter
│       └── util/                     # DBUtil / PasswordUtil / WebUtil
├── WebContent/
│   ├── login.jsp                     # 登录
│   ├── register.jsp                  # 注册
│   ├── index.jsp                     # 入口跳转
│   ├── error.jsp                     # 错误页
│   ├── assets/css/app.css            # 全部手写样式
│   ├── assets/js/app.js              # 交互脚本
│   └── WEB-INF/
│       ├── web.xml                   # Servlet / Filter 注册
│       ├── views/                    # 登录后的页面（不可直接访问）
│       │   ├── home.jsp
│       │   ├── shelf.jsp
│       │   ├── search.jsp
│       │   ├── book-form.jsp
│       │   ├── book-detail.jsp
│       │   ├── category-form.jsp
│       │   └── common/               # header / footer / 图书卡片
│       └── lib/
│           ├── jstl-1.2.jar
│           └── mysql-connector-java-8.0.30.jar
├── sql/library_db.sql                # 建库、建表、示例数据
├── lib/servlet-api.jar               # 仅编译用，不会打进 war
├── .idea/                            # IDEA 工程配置
├── jspsweb.iml
└── README.md
```

---

## 快速开始

### 1. 初始化数据库

确保本机 MySQL 8.0 已启动。用 **root** 执行：

```bash
mysql -uroot -p --default-character-set=utf8mb4 < sql/library_db.sql
```

脚本会：

1. 创建数据库 `library_db`（utf8mb4）；
2. 创建 `users`、`category`、`book` 三张表；
3. 写入 8 个板块、40 本示例图书、2 个演示账号；
4. 创建项目专用账号 `library_app`，密码 `123456`，只授予 `library_db` 权限。

> 如果不想创建专用账号，也可以删掉脚本末尾的 `CREATE USER / GRANT` 语句，然后把自己的账号密码填进 `src/db.properties`。

### 2. 配置数据库连接

编辑 `src/db.properties`：

```properties
jdbc.url=jdbc:mysql://localhost:3306/library_db?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
jdbc.username=library_app
jdbc.password=123456
```

也可以用环境变量覆盖，不必改文件：

```text
LIBRARY_JDBC_URL / LIBRARY_JDBC_USERNAME / LIBRARY_JDBC_PASSWORD
```

### 3. 在 IDEA 中运行

1. 用 IDEA 打开 `jspsweb` 目录；
2. `File → Project Structure → Project SDK` 选择 JDK 17 或以上；
3. 确认 `Tomcat 9.0.30` 已在 `Settings → Build, Execution, Deployment → Application Servers` 中配置；
4. 右上角运行配置选择 **Tomcat 9.0.30**，直接启动；
5. 浏览器访问：<http://localhost:8080/library/>。

IDEA 已配置好：

- Web 根目录：`WebContent`（不是空的 `web` 目录）；
- artifact：`jspsweb:Web exploded`；
- 部署上下文：`/library`；
- 编译级别：Java 17；
- `lib/servlet-api.jar` 用于编译，不会部署到 `WEB-INF/lib`。

> `.idea/workspace.xml` 含本机路径，未提交到 GitHub。从仓库克隆后，在 IDEA 里新增一个 **Tomcat 9** 运行配置，部署 `jspsweb:Web exploded`，上下文设为 `/library` 即可。

### 4. 演示账号

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | 管理员 |
| `demo` | `demo123` | 读者 |

也可以直接在注册页创建新账号。

---

## 页面与路由

| 路径 | 说明 |
| --- | --- |
| `/login.jsp` | 登录页 |
| `/register.jsp` | 注册页 |
| `POST /auth` | `action=login` 登录，`action=register` 注册 |
| `/auth?action=logout` | 退出登录 |
| `/home` | 登录后主页（板块总览 + 统计 + 最近上架） |
| `/category?id=1` | 进入某个板块的书架 |
| `/category?action=toAdd` | 新建板块 |
| `/category?action=toEdit&id=1` | 编辑板块 |
| `POST /category` | `action=save` 保存板块，`action=delete` 删除 |
| `/book?action=toAdd&categoryId=1` | 新增图书 |
| `/book?action=toEdit&id=1` | 编辑图书 |
| `/book?action=detail&id=1` | 图书详情 |
| `POST /book` | `action=save` 保存图书，`action=delete` 删除 |
| `/search?keyword=Java` | 全馆搜索 |

---

## 这次修好的环境问题

原工程能打开但基本跑不起来，已修复：

| 问题 | 修复 |
| --- | --- |
| `Book.java` 是空类，`BookDao` / `BookServlet` 无法编译 | 补全实体字段、getter/setter |
| `jspsweb.iml` 的 Web 根目录指向空的 `web/`，页面实际在 `WebContent/` | Web root 改为 `WebContent` |
| 运行配置部署路径是 `/jspsweb_Web_exploded` | 改为 `/library`，启动页指向 `/library/` |
| 只靠 `@WebServlet` 注解，旧版 Tomcat 9.0.30 配合 JDK 25 时注解扫描可能读不懂高版本 class 文件 | 改为 `web.xml` 显式注册，并设置 `metadata-complete="true"` |
| JSP 需要 JSTL，但 `WEB-INF/lib` 只有 MySQL 驱动 | 加入 `jstl-1.2.jar`，并配置 IDEA 库 |
| 项目 JDK 是 25，源码却混入了 Java 25 的 `Main.java` 示例 | 删除示例类，统一编译级别为 Java 17 |
| 中文表单/页面可能乱码 | `EncodingFilter` + JSP `page-encoding=UTF-8` + 数据库 utf8mb4 |
| `db.properties` 直接写 root 密码 | 改为专用账号，可用环境变量覆盖 |

> Tomcat 9.0.30 实测可以在 JDK 25 上启动并编译 JSP；真正需要注意的是 **Servlet 注解扫描** 和 **Jakarta 命名空间**。本项目已通过 `web.xml` 注册规避前者，通过固定 Tomcat 9 规避后者。

---

## 数据库设计

```text
users                          category
┌───────────────┐             ┌───────────────┐
│ id            │             │ id            │
│ username      │             │ name          │
│ password      │             │ icon          │
│ nickname      │             │ description   │
│ email         │             │ color         │
│ role          │             │ sort_order    │
│ created_at    │             │ created_at    │
└───────────────┘             └───────┬───────┘
                                      │ 1
                                      │
                                      │ N
                              ┌───────▼───────┐
                              │ book          │
                              │ id            │
                              │ category_id   │
                              │ name          │
                              │ author        │
                              │ isbn          │
                              │ publisher     │
                              │ price         │
                              │ stock         │
                              │ description   │
                              │ create_time   │
                              │ update_time   │
                              └───────────────┘
```

- `book.category_id` 外键关联 `category.id`，`ON DELETE CASCADE`：删除板块会连带删除板块内图书。
- 密码字段保存 `pbkdf2_sha256$迭代次数$盐$哈希`，不保存明文。
- 所有 SQL 使用 `PreparedStatement`；排序字段走白名单，不拼接用户输入。

---

## 常用命令

```bash
# 导入数据库
mysql -uroot -p --default-character-set=utf8mb4 < sql/library_db.sql

# 重新生成一个密码哈希（可选）
javac -encoding UTF-8 -d out/production/jspsweb src/com/library/util/PasswordUtil.java
java -cp out/production/jspsweb com.library.util.PasswordUtil 你的新密码
```

---

## 已知限制

- 这是课程设计项目，未接入连接池（每次请求新建连接），高并发场景应改用 HikariCP/Druid。
- 所有登录用户都可以管理板块和图书，没有按角色细分权限。
- 删除操作使用 `confirm` 二次确认，没有回收站。
- 未实现借阅/归还记录，仅管理图书库存。

---

## 仓库

GitHub：<https://github.com/The7wa/Students_Library_System>

如果这个项目对你有帮助，欢迎点一个 Star ⭐。
