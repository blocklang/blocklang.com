# `API_WIDGET_PROP` - WIDGET 属性

一个组件的属性可细分为三类：

1. UI 部件的属性
2. UI 部件的事件
3. UI 部件的子部件，在其中存 slot 名称，如果不需要通过 slot 指定位置，则 slot 的值默认为 `default`

## 字段

| 字段名              | 注释             | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ---------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键             | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识 | int     |      |        |      | 否   |
| api_widget_id       | API Widget 标识  | int     |      |        |      | 否   |
| code                | 属性的编码       | char    | 4    |        |      | 否   |
| name                | 属性的名称       | varchar | 64   |        |      | 否   |
| label               | 属性的显示名     | varchar | 64   |        |      | 是   |
| description         | 属性的详细说明   | varchar | 512  |        |      | 是   |
| value_type          | 属性的值类型     | varchar | 32   |        |      | 否   |
| default_value       | 默认值           | varchar | 32   |        |      | 是   |
| required            | 是否必填         | boolean |      | false  |      | 是   |

## 约束

* 主键：`PK_API_WIDGET_PROP`
* 外键：(*未设置*)`FK_API_PROP_WIDGET`，`API_WIDGET_ID` 对应 `API_WIDGET` 表的 `dbid`
* 索引：`UK_API_PROP_ON_API_WIDGET_NAME`，对应字段 `api_widget_id`、`name`；`UK_API_PROP_ON_API_WIDGET_CODE`，对应字段 `api_widget_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_WIDGET` 中的值
2. `api_repo_version_id` 是一个冗余字段，方便批量查询和删除
3. `value_type` 的值为：`number(数字)`，`string(字符串)`，`boolean(布尔类型)`，`function(函数)` 和 `slot(插槽)`
4. 不同版本的同一个部件的同一个属性，则 `code` 的值必须相同
5. 一个组件对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束



TODO: 是否需要增加一个表，存储父部件往子部件传入的参数？