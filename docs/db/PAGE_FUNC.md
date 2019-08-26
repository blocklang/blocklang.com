# `PAGE_FUNC` - 页面函数

页面行为，在实现层面对应一个函数。本表用于定义一个函数，存储函数名和返回类型。

## 字段

| 字段名              | 注释           | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | -------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键           | int     |      |        | 是   | 否   |
| project_resource_id | 项目资源标识   | int     |      |        |      | 否   |
| name                | 函数名         | varchar | 32   |        |      | 否   |
| return_type         | 函数的返回类型 | varchar | 32   |        |      | 否   |
| seq                 | 序列           | int     |      |        |      | 否   |

## 约束

* 主键：`PK_PAGE_FUNC`
* 外键：(*未设置*)`FK_PAGE_FUNC_ON_RESOURCE_ID`，`project_resource_id` 对应 `PROJECT_RESOURCE` 表的 `dbid`
* 索引：`UK_PAGE_FUNC_ON_RESOURCE_ID_NAME`(唯一索引)，对应字段 `project_resource_id`、`name`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `return_type` 的值为：`Void` 表示无返回值，`Number` 表示数字，`String` 表示字符串，`Date` 表示日期，`Boolean` 表示布尔值，`Object` 表示对象，`Array` 表示数组，以及在 `PAGE_DATA` 中的自定义数据类型(**TODO:待验证**)
3. `seq` 不是全表排序，而是页面级别排序，每个页面都是从 1 开始
