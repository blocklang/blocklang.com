# `PAGE_FUNC` - 页面函数

页面行为，在实现层面对应一个函数。当前只存储一个页面中的事件处理函数。

## 字段

| 字段名              | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键         | varchar | 32   |        | 是   | 否   |
| project_resource_id | 项目资源标识 | int     |      |        |      | 否   |

## 约束

* 主键：`PK_PAGE_FUNC`
* 外键：无
* 索引：`IDX_PAGE_FUNC_ON_PROJECT_RESOURCE_ID`(普通索引)，对应字段 `project_resource_id`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. 事件处理函数的定义存储在 `API_COMPONENT_ATTR` 和 `API_COMPONENT_ATTR_FUNC_ARG` 两张表中
3. 将该表中的 `dbid` 设置为 `PAGE_WIDGET_ATTR_VALUE` 中的 `attr_value`，就在函数定义和部件事件之间建立了关联
4. `project_resource_id` 是一个冗余字段，便于快速查找出一个页面中的所有事件处理函数
