# `CM_PROPERTY` - 系统属性

存储系统参数

## 字段

| 字段名     | 注释           | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ---------- | -------------- | ------- | ---- | ------ | ---- | ---- |
| dbid       | 主键           | int     |      |        | 是   | 否   |
| prop_key   | 属性名称       | varchar | 32   |        |      | 否   |
| prop_value | 属性值         | varchar | 128  |        |      | 否   |
| prop_desc  | 属性描述       | varchar | 32   |        |      | 是   |
| parent_id  | 父属性标识     | int     |      |        |      | 否   |
| data_type  | 属性值数据类型 | char    | 2    | 01     |      | 否   |
| is_valid   | 是否有效       | boolean |      | true   |      | 否   |

## 约束

* 主键：`PK_CM_PROPERTY`
* 外键：无
* 索引：`UK_PROP_KEY_PARENT_ID`，对应字段 `prop_key` 和 `parent_id`

## 说明

1. `data_type` 的值为：`01` 表示字符串，`02` 表示数字
2. `dbid` 的值为 -1 时，表示顶级属性
