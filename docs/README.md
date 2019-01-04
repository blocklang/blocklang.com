# 帮助

## 项目结构

### src/main/java

1. controller - 存放本项目专用的 HTTP 服务
2. api - 存放共享的 HTTP 服务
3. service - 存放业务逻辑接口
4. service.impl - 存放业务逻辑实现类
5. dao - 存放数据访问接口
6. dao.impl - 存放数据访问实现类
7. model - 存放实体对象
8. data - 存放 POJO 对象，是对 model 实体对象的组合

### src/main/resources

1. db/changelog/data - 存放初始化数据
2. db/changelog/table - 存放变更的表脚本
3. db/db.sql - 存放建库脚本