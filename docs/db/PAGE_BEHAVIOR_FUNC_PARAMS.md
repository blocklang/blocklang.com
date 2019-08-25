# `PAGE_BEHAVIOR_FUNC_PARAMS` - 页面函数的输入参数

一个函数可以包含零到多个参数，一个参数包含参数名和参数的数据类型。

## 字段

| 字段名                | 注释           | 类型    | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | -------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                  | 主键           | int     |      |        | 是   | 否   |
| page_behavior_func_id | 页面函数标识   | int     |      |        |      | 否   |
| name                  | 参数名         | varchar | 32   |        |      | 否   |
| type                  | 参数的数据类型 | varchar | 32   |        |      | 否   |
| seq                   | 序列           | int     |      |        |      | 否   |

## 约束

* 主键：`PK_PAGE_BEHAVIOR_FUNC_PARAMS`
* 外键：(*未设置*)`FK_PAGE_FUNC_PARAMS_ON_FUNC_ID`，`page_behavior_func_id` 对应 `PAGE_BEHAVIOR_FUNC` 表的 `dbid`
* 索引：`UK_PAGE_FUNC_PARAMS_ON_FUNC_ID_NAME`(唯一索引)，对应字段 `page_behavior_func_id`、`name`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `type` 的值为：`Int` 表示整数，`Float` 表示小数，`String` 表示字符串，`Date` 表示日期，`Boolean` 表示布尔值，`Object` 表示对象，`Array` 表示数组，以及在 `PAGE_DATA` 中的自定义数据类型(**TODO:待验证**)
3. `seq` 不是全表排序，而是函数级别排序，每个函数都是从 1 开始
