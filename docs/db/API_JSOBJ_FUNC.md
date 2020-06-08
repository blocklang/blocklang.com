# `API_JSOBJ_FUNC` - JavaScript 对象中的函数

## 字段

| 字段名       | 注释                       | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------ | -------------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid         | 主键                       | int     |      |        | 是   | 否   |
| api_jsobj_id | API JavaScript Object 标识 | int     |      |        |      | 否   |
| code         | 函数的编码                 | char    | 4    |        |      | 否   |
| name         | 函数的名称                 | varchar | 64   |        |      | 否   |
| description  | 函数的详细说明             | varchar | 512  |        |      | 是   |
| return_type  | 函数的返回类型             | varchar | 32   |        |      | 是   |

## 约束

* 主键：`PK_API_JSOBJ_FUNC`
* 外键：(*未设置*)`FK_API_FUNC_JSOBJ`，`API_JSOBJ_ID` 对应 `API_JSOBJ` 表的 `dbid`
* 索引：`UK_API_FUNC_ON_API_JSOBJ_NAME`，对应字段 `api_jsobj_id`、`name`；`UK_API_FUNC_ON_API_JSOBJ_CODE`，对应字段 `api_jsobj_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_JSOBJ` 中的值
2. 不同版本的同一个函数的同一个属性，则 `code` 的值必须相同
3. 一个组件对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
