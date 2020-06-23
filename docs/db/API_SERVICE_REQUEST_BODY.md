# `API_SERVICE_REQUEST_BODY` - HTTP 请求体

## 字段

| 字段名                | 注释                         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ---------------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                  | 主键                         | int     |      |        | 是   | 否   |
| api_repo_version_id   | API 仓库版本标识             | int     |      |        |      | 否   |
| api_service_id        | API Service 标识             | int     |      |        |      | 否   |
| code                  | 参数的编码                   | char    | 4    |        |      | 否   |
| name                  | 参数的名称，取 `contentType` | varchar | 64   |        |      | 否   |
| description           | 参数的详细说明               | varchar | 512  |        |      | 是   |
| value_type            | 参数的值类型                 | varchar | 32   |        |      | 否   |
| api_service_schema_id | 参数的类型信息               | int     |      |        |      | 是   |

## 约束

* 主键：`PK_API_SERVICE_REQUEST_BODY`
* 外键：(*未设置*)`FK_API_BODY_SERVICE`，`API_SERVICE_ID` 对应 `API_SERVICE` 表的 `dbid`
* 索引：`UK_API_BODY_ON_API_SERVICE_NAME`，对应字段 `api_service_id`、`name`；`UK_API_BODY_ON_API_SERVICE_CODE`，对应字段 `api_service_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_SERVICE` 中的值
2. `api_repo_version_id` 是一个冗余字段，方便批量查询和删除
3. `name` 的值取 `contentType`：`application/json`、`application/octet-stream`、`application/x-www-form-urlencoded`、`text/plain` 和 `application/xml`
4. 不同版本的同一个部件的同一个属性，则 `code` 的值必须相同
5. 一个组件对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
6. `value_type` 的值为 `object` 和 `array`，具体的类型信息在 `api_service_schema` 表中定义