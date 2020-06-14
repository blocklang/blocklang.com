# `API_WIDGET_EVENT_ARG` - WIDGET 事件的输入参数

## 字段

| 字段名              | 注释                  | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | --------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键                  | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识      | int     |      |        |      | 否   |
| api_widget_prop_id  | API Widget 的属性标识 | int     |      |        |      | 否   |
| code                | 参数编码              | char    | 4    |        |      | 否   |
| name                | 参数名                | varchar | 32   |        |      | 否   |
| label               | 参数的显示名          | varchar | 32   |        |      | 是   |
| value_type          | 参数的值类型          | varchar | 32   |        |      | 否   |
| default_value       | 参数的默认值          | varchar | 32   |        |      | 是   |
| description         | 参数描述              | varchar | 512  |        |      | 是   |
| seq                 | 参数顺序              | int     |      |        |      | 否   |

## 约束

* 主键：`PK_API_WIDGET_EVENT_ARG`
* 外键：(*未设置*)`FK_API_WIDGET_EVENT_ARG`，`API_WIDGET_PROP_ID` 对应 `API_WIDGET_PROP` 表的 `dbid`
* 索引：`UK_API_WIDGET_EVENT_ARG_ON_PROP_CODE`，对应字段 `api_widget_prop_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_WIDGET` 中的值
2. `api_repo_version_id` 是一个冗余字段，方便批量查询和删除
3. 不同版本的同一个部件的同一个事件的同一个参数值，则 `code` 的值必须相同
4. `CODE` 的值从 `0001` 开始，到 `9999` 结束
5. `value_type` 的值为：`number(数字)`，`string(字符串)`，`boolean(布尔类型)`
6. `seq` 参数的显示顺序，每个函数都是从 1 开始计数
