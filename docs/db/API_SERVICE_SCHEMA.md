# `API_SERVICE_SCHEMA` -  描述数据结构

描述 `requestBody` 和 `responses` 中的数据结构。现在此表中定义结构信息，然后其他表中引用。

页面或服务中引用的的是 `code`，而不是 `dbid`。目的是当部件升级时，不用修改页面或服务中的配置，而是直接修改项目依赖的版本号即可。

## 字段

| 字段名              | 注释             | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ---------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键             | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识 | int     |      |        |      | 否   |
| name                | 属性名           | varchar | 64   |        |      | 否   |
| type                | 数据类型         | varchar | 32   |        |      | 否   |
| description         | 属性的详细说明   | varchar | 512  |        |      | 是   |
| parent_id           | 父属性标识       | int     |      | -1       |      | 否   |

## 约束

* 主键：`PK_API_SERVICE`
* 外键：(*未设置*)`FK_API_SERVICE_REPO_VERSION`，`API_REPO_VERSION_ID` 对应 `API_REPO_VERSION` 表的 `dbid`
* 索引：`UK_API_SERVICE_ON_API_REPO_VERSION_NAME`，对应字段 `api_repo_version_id`、`name`；`UK_API_SERVICE_ON_API_REPO_VERSION_CODE`，对应字段 `api_repo_version_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_SERVICE` 中的值
2. `parent_id` 的默认值为 `-1`，表示根节点
