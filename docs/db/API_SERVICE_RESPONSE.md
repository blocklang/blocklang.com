# `API_SERVICE_RESPONSE` - HTTP 响应信息

## 字段

| 字段名                | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                  | 主键               | int     |      |        | 是   | 否   |
| api_service_id        | API Service 标识   | int     |      |        |      | 否   |
| code                  | 响应信息的编码     | char    | 4    |        |      | 否   |
| name                  | 响应信息的名称     | varchar | 64   |        |      | 否   |
| status_code            | http 状态码        | char    | 3    |        |      | 否   |
| content_type           | 内容类型           | varchar | 64   |        |      | 否   |
| description           | 响应信息的详细说明 | varchar | 512  |        |      | 是   |
| value_type            | 响应信息的值类型   | varchar | 32   |        |      | 否   |
| api_service_schema_id | 响应信息的类型信息 | int     |      |        |      | 否   |

## 约束

* 主键：`PK_API_API_SERVICE_RESPONSE`
* 外键：(*未设置*)`FK_API_RESPONSE_SERVICE`，`API_SERVICE_ID` 对应 `API_SERVICE` 表的 `dbid`
* 索引：`UK_API_RESP_ON_API_SERVICE_NAME`，对应字段 `api_service_id`、`name`；`UK_API_RESP_ON_API_SERVICE_CODE`，对应字段 `api_service_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_SERVICE` 中的值
2. `name` 的值取 `{statusCode}_{contentType}`
3. `statusCode` 的值为 ：`200`、`201`、`202`、`204`、`400`、`401`、`403`、`404`、`406`、`410`、`422`、`500`
4. `contentType` 的值为：`application/json`、`application/octet-stream`、`application/x-www-form-urlencoded`、`text/plain` 和 `application/xml`
5. 不同版本的同一个部件的同一个属性，则 `code` 的值必须相同
6. 一个组件对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
7. `value_type` 的值为 `string`、`number`、`boolean`、`object` 和 `array`
8. 如果 `value_type` 的值为 `string`、`number`、`boolean` 等基本类型，则 `api_service_schema_id` 值为空，如果 `value_type` 的值为 `object` 或 `array`，则具体的类型信息在 `api_service_schema` 表中定义