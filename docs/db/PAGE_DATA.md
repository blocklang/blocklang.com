# `PAGE_DATA` - 页面数据

本表中同时存储页面中的数据格式和默认数据。

## 字段

| 字段名              | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键         | int     |      |        | 是   | 否   |
| parent_id           | 父标识       | int     |      |        |      | 否   |
| seq                 | 序列         | int     |      |        |      | 否   |
| project_resource_id | 项目资源标识 | int     |      |        |      | 否   |
| name                | 名称         | varchar | 32   |        |      | 否   |
| label               | 显示名称     | varchar | 32   |        |      | 是   |
| type                | 数据类型     | varchar | 10   |        |      | 否   |
| default_value       | 默认值       | varchar | 32   |        |      | 是   |

## 约束

* 主键：`PK_PAGE_DATA`
* 外键：(*未设置*)`FK_PAGE_DATA_ON_RESOURCE_ID`，`project_resource_id` 对应 `PROJECT_RESOURCE` 表的 `dbid`
* 索引：`UK_PAGE_DATA_ON_RESOURCE_PARENT_NAME`(唯一索引)，对应字段 `project_resource_id`、`parent_id`、`name`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `seq` 不是页面级别排序，而是同一层级内排序，每层都是从 1 开始
3. `type` 的值为：`Object` 表示对象，`Array` 表示数组，`Int` 表示整数，`Float` 表示小数，`String` 表示字符串，`Date` 表示日期，`Boolean` 表示布尔值
4. `parent_id` 对应本表中的 `id` 的值，而根节点的值为 `-1`
5. 为 `project_resource_id`、`parent_id`、`name` 添加唯一索引，因为一个页面中，某一层内（如对象的字段）的 `name` 不能重名
