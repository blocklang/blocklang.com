# `API_COMPONENT_ATTR_FUN_ARG` - 组件属性的函数参数

本表用于存储：

1. UI 部件的事件参数列表

## 字段

| 字段名                | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                  | 主键               | int     |      |        | 是   | 否   |
| api_component_attr_id | API 组件的属性标识 | int     |      |        |      | 否   |
| code                  | 参数编码           | char    | 4    |        |      | 否   |
| name                  | 参数名             | varchar | 32   |        |      | 否   |
| label                 | 参数的显示名       | varchar | 32   |        |      | 是   |
| value_type            | 参数的值类型       | varchar | 32   |        |      | 否   |
| default_value         | 参数的默认值       | varchar | 32   |        |      | 是   |
| description           | 参数描述           | varchar | 512  |        |      | 是   |
| seq                   | 参数顺序           | int     |      |        |      | 否   |

## 约束

* 主键：`PK_API_COMPONENT_ATTR_FUN_ARG`
* 外键：(*未设置*)`FK_API_COMPONENT_ATTR_FUN_ARG`，`API_COMPONENT_ATTR_ID` 对应 `API_COMPONENT_ATTR` 表的 `dbid`
* 索引：`UK_COMPONENT_ATTR_FUN_ARG_ON_CODE`，对应字段 `api_component_attr_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_COMPONENT` 中的值
2. 不同版本的同一个部件的同一个函数的同一个参数值，则 `code` 的值必须相同
3. `CODE` 的值从 `0001` 开始，到 `9999` 结束
4. `value_type` 的值为：`number(数字)`，`string(字符串)`，`boolean(布尔类型)`
5. `seq` 参数的显示顺序，每个函数都是从 1 开始计数
