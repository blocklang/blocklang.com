# `API_SCHEMA` -  定义复杂数据类型

在各种 API 定义中的 `valueType` 或 `type` 除了是 `string`、`number` 等基本数据类型外，还需要自定义的复杂数据类型。
在本表中就是存储自定义的复杂数据类型的结构。

Widget、Service 和 WebApi 的 Schema 都存在此表中。

## 字段

| 字段名              | 注释             | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ---------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键             | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识 | int     |      |        |      | 否   |
| name                | 属性名           | varchar | 64   |        |      | 是   |
| type                | 数据类型         | varchar | 32   |        |      | 否   |
| description         | 属性的详细说明   | varchar | 512  |        |      | 是   |
| parent_id           | 父属性标识       | int     |      | -1     |      | 否   |

## 约束

* 主键：`PK_API_SCHEMA`
* 外键：(*未设置*)`FK_API_SCHEMA_REPO_VERSION`，`API_REPO_VERSION_ID` 对应 `API_REPO_VERSION` 表的 `dbid`
* 索引：`UK_API_SCHEMA_ON_REPO_VERSION_PARENT_NAME`，对应字段 `api_repo_version_id`、 `parent_id`、`name`

## 说明

1. `parent_id` 的默认值为 `-1`，表示根节点
2. `type` 的值为 `string`、`number`、`boolean`、`object` 和 `array`
