# `PAGE_BEHAVIOR_FUNC_BODY` - 页面函数的函数体

存储函数体中的调用语句，支持嵌套调用。

## 字段

| 字段名                | 注释         | 类型 | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ------------ | ---- | ---- | ------ | ---- | ---- |
| dbid                  | 主键         | int  |      |        | 是   | 否   |
| page_behavior_func_id | 定义函数标识 | int  |      |        |      | 否   |
| called_func_type      | 被调函数类型 | char | 2    |        |      | 否   |
| called_func_id        | 被调函数标识 | int  |      |        |      | 否   |
| parent_id             | 父标识       | int  |      |        |      | 否   |
| seq                   | 序列         | int  |      |        |      | 否   |

## 约束

* 主键：`PK_PAGE_BEHAVIOR_FUNC_BODY`
* 外键：(*未设置*)`FK_PAGE_FUNC_BODY_ON_FUNC_ID`，`page_behavior_func_id` 对应 `PAGE_BEHAVIOR_FUNC` 表的 `dbid`
* 索引：`UK_PAGE_FUNC_BODY_ON_FUNC_ID_CALLED_TYPE_ID_PARENT`(唯一索引)，对应字段 `page_behavior_func_id`、`called_func_type`、`called_func_id`、`parent_id`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `page_behavior_func_id` 是定义的函数标识，在定义函数中包含一系列被调函数
3. `called_func_type` 的值为：`01` 表示引用 API 组件库中定义的函数，`02` 表示本页面定义的函数
4. `called_func_id` 的值为，如果 `called_func_type` 的值为 `01` 则取 `API_COMPONENT` 表中的 `dbid`，如果 `called_func_type` 的值为 `02`，则取 `PAGE_BEHAVIOR_FUNC` 表中的 `dbid`
5. `parent_id` 对应本表中的 `id` 的值，而根节点的值为 `-1`
6. `seq` 不是全表排序，而是函数体内每一嵌套层内的排序，都是从 1 开始
