# TaskFlow - 任务/项目管理系统

## 技术栈

- **后端**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **前端**: Thymeleaf, Bootstrap 5, Chart.js, Font Awesome
- **数据库**: MySQL
- **构建工具**: Maven

## 功能模块

### 用户端
- 注册/登录（含 Remember-Me 记住我）
- 个人仪表盘（任务统计、图表）
- 项目管理（CRUD、成员管理）
- 任务管理（CRUD、状态流转、优先级、截止日期）
- 任务评论
- 文件附件上传
- 个人资料编辑

### 管理端
- 系统仪表盘（全局统计）
- 用户管理（CRUD、角色变更）
- 项目管理（全局查看、删除）
- 任务管理（全局查看、筛选、删除）
- 审计日志查看

### 高级特性
- 全局异常处理 + 自定义错误页面（403/404/500）
- 分页 + 排序
- AJAX 状态更新
- 文件上传
- Remember-Me 持久登录
- 页面访问统计
- 请求日志记录
- 定时任务（日志自动清理）
- Chart.js 数据可视化

## 快速启动

### 1. 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 2. 创建数据库
```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 修改数据库配置
编辑 `src/main/resources/application.yml`，修改：
```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

### 4. 启动应用
```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run
# 或使用 Maven
mvn spring-boot:run
```

### 5. 访问应用
打开浏览器访问：http://localhost:8080

### 默认账号
| 账号 | 密码 | 角色 |
|------|------|------|
| admin | admin123 | 管理员 |
| demo | demo123 | 普通用户 |

## 项目结构

```
src/main/java/com/taskflow/
├── config/          # 安全、MVC、调度配置
├── controller/      # 控制器
│   ├── admin/       # 管理端控制器
│   └── rest/        # REST API 控制器
├── service/         # 业务逻辑
│   └── impl/        # 业务实现
├── repository/      # 数据访问
├── entity/          # 实体类
│   └── enums/       # 枚举
├── dto/             # 数据传输对象
├── exception/       # 异常处理
├── interceptor/     # 拦截器
└── mapper/          # 对象映射

src/main/resources/
├── templates/       # Thymeleaf 模板
│   ├── layout/      # 布局模板
│   ├── auth/        # 登录/注册
│   ├── user/        # 用户页面
│   ├── admin/       # 管理页面
│   └── error/       # 错误页面
└── static/          # 静态资源
    ├── css/         # 样式文件
    └── js/          # JavaScript
```

## 开发建议

1. 使用 IntelliJ IDEA 打开项目，自动识别 Maven 项目
2. 启动前确保 MySQL 服务已运行
3. 开发时 `application.yml` 中 `jpa.hibernate.ddl-auto=update` 会自动建表
4. 生产环境建议改为 `validate` 并手动管理数据库迁移
