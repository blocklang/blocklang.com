# `API_COMPONENT_ATTR` - 组件属性

一个组件的属性可细分为三类：

1. UI 部件的属性
2. UI 部件的事件（事件也是一种特殊的属性）
3. 功能组件的 API（对应一个函数或方法）

本表统一存储此三类组件，当每一类属性有不同的子属性时，要存储到不同的子表中。

WIP: **在开发完服务器端组件后，本表才能稳定**。

## 字段

| 字段名           | 注释           | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ---------------- | -------------- | ------- | ---- | ------ | ---- | ---- |
| dbid             | 主键           | int     |      |        | 是   | 否   |
| api_component_id | API 组件标识   | int     |      |        |      | 否   |
| code             | 属性的编码     | char    | 4    |        |      | 否   |
| name             | 属性的名称     | varchar | 64   |        |      | 否   |
| label            | 属性的显示名   | varchar | 64   |        |      | 是   |
| description      | 属性的详细说明 | varchar | 512  |        |      | 是   |
| value_type       | 属性的值类型   | varchar | 32   |        |      | 否   |
| default_value    | 默认值         | varchar | 32   |        |      | 是   |

## 约束

* 主键：`PK_API_COMPONENT_ATTR`
* 外键：(*未设置*)`FK_API_COMP_ATTR_COMP`，`API_COMPONENT_ID` 对应 `API_COMPONENT` 表的 `dbid`
* 索引：`UK_API_COMP_ATTR_ON_API_COMPONENT_NAME`，对应字段 `api_component_id`、`name`；`UK_API_COMP_ATTR_ON_API_COMPONENT_CODE`，对应字段 `api_component_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_COMPONENT` 中的值
2. `value_type` 的值为：`number(数字)`，`string(字符串)`，`boolean(布尔类型)`，`function(函数)`
3. 不同版本的同一个部件的同一个属性，则 `code` 的值必须相同
4. 一个组件对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
