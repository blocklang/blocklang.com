# `API_SERVICE_PARAM` - HTTP 请求的输入参数

当前支持的输入参数分 5 类：`path`、`query`、`header`、`cookie` 和 `request body`，本表存储前 4 类输入参数，
`request body` 的信息存在 `API_SERVICE_REQUEST_BODY` 表中。

## 字段

| 字段名                | 注释             | 类型    | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ---------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                  | 主键             | int     |      |        | 是   | 否   |
| api_repo_version_id   | API 仓库版本标识 | int     |      |        |      | 否   |
| api_service_id        | API Service 标识 | int     |      |        |      | 否   |
| code                  | 参数的编码       | char    | 4    |        |      | 否   |
| name                  | 参数的名称       | varchar | 64   |        |      | 否   |
| param_type            | 参数的类型       | varchar | 16   |        |      | 否   |
| description           | 参数的详细说明   | varchar | 512  |        |      | 是   |
| required              | 是否必填         | boolean |      | true   |      | 是   |
| value_type            | 参数的值类型     | varchar | 32   |        |      | 否   |
| api_service_schema_id | 参数的类型信息   | int     |      |        |      | 是   |

## 约束

* 主键：`PK_API_SERVICE_PARAM`
* 外键：(*未设置*)`FK_API_PARAM_SERVICE`，`API_SERVICE_ID` 对应 `API_SERVICE` 表的 `dbid`
* 索引：`UK_API_PARAM_ON_API_SERVICE_NAME`，对应字段 `api_service_id`、`name`；`UK_API_PARAM_ON_API_SERVICE_CODE`，对应字段 `api_service_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_SERVICE` 中的值
2. `api_repo_version_id` 是一个冗余字段，方便批量查询和删除
3. `param_type` 的值为：`path`，`query`，`header` 和 `cookie`
4. 不同版本的同一个部件的同一个属性，则 `code` 的值必须相同
5. 一个组件对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
6. `value_type` 的值为 `string`、`number`、`boolean`、`object` 和 `array`
7. 如果 `value_type` 的值为 `string`、`number`、`boolean` 等基本类型，则 `api_service_schema_id` 值为空，如果 `value_type` 的值为 `object` 或 `array`，则具体的类型信息在 `api_service_schema` 表中定义