# `PROJECT_BUILD` - 项目构建信息

项目基于 git tag 标签构建。

## 字段

| 字段名         | 注释         | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------ | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键         | int      |      |        | 是   | 否   |
| project_tag_id | 项目标签标识 | int      |      |        |      | 否   |
| start_time     | 开始时间     | datetime |      |        |      | 否   |
| end_time       | 结束时间     | datetime |      |        |      | 是   |
| build_result   | 构建结果     | char     | 2    | 01     |      | 否   |

## 约束

* 主键：`PK_PROJECT_BUILD`
* 外键：(*未设置*)`FK_BUILD_TAG_ID`，`PROJECT_TAG_ID` 对应 `PROJECT_TAG` 表的 `dbid`
* 索引：`UK_BUILD_PROJECT_TAG_ID`，对应字段 `project_tag_id`

## 说明

1. `build_result` 的值为：`01` 表示 `未构建(inited)`，`02` 表示 `正在构建(started)`，`03` 表示 `构建失败(failed)`，`04` 表示 `构建成功(passed)`，`05` 表示 `取消构建(canceled)`
2. 对于一个 tag，只保存最近一次构建信息，如果重新构建，则覆盖上一次的构建信息
3. 如果要保存一个 tag 的所有构建信息，则不要调整此表，因为此表只保存最近一次的构建信息，可创建一张构建历史表
