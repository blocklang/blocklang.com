# `API_SERVICE` -  Service 基本信息

一个 Service 就是一个 RESTful API。本表存储 RESTful API 的基本信息

页面或服务中引用的的是 `code`，而不是 `dbid`。目的是当部件升级时，不用修改页面或服务中的配置，而是直接修改项目依赖的版本号即可。

## 字段

| 字段名              | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键               | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识   | int     |      |        |      | 否   |
| code                | Service 的编码     | char    | 4    |        |      | 否   |
| name                | Service 的名称     | varchar | 64   |        |      | 否   |
| url                 | Service 的访问 url | varchar | 64   |        |      | 否   |
| httpMethod          | http method        | varchar | 32   |        |      | 否   |
| description         | Widget 的详细说明  | varchar | 512  |        |      | 是   |

## 约束

* 主键：`PK_API_SERVICE`
* 外键：(*未设置*)`FK_API_SERVICE_REPO_VERSION`，`API_REPO_VERSION_ID` 对应 `API_REPO_VERSION` 表的 `dbid`
* 索引：`UK_API_SERVICE_ON_API_REPO_VERSION_NAME`，对应字段 `api_repo_version_id`、`name`；`UK_API_SERVICE_ON_API_REPO_VERSION_CODE`，对应字段 `api_repo_version_id`、`code`

## 说明

1. `API_SERVICE` 只与 `API_REPO_VERSION` 表有关联，与 `COMPONENT_REPO_VERSION` 表无关
2. `CODE` 是 Service 的编码，一个 Service 的编码确定后，就不能再变更；在页面中引用 Service 时，使用的不是 `dbid`，而是 `code`
3. 一个组件库对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
4. Service 的每个版本，以及每个版本的属性和每个版本的属性可选值都要在相关表中存一份
5. 不同 Service 的同一个部件，则 `code` 的值必须相同
6. `name` 的值取 Service API 定义中的 `operationId`
7. `url` 是 Service 的访问地址，不包含 ip 和 port，如 `users/{userId}`
8. `httpMethod` 的值为：`GET`、`POST`、`PUT`、`DELETE`、`PATCH`、`HEAD` 和 `OPTIONS` 等
