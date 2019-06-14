# `PROJECT_RELEASE_TASK` - 项目发布任务登记表

因为项目发布是一个包含多个节点的流程，耗时也较长，在本表登记发布任务，此表属于任务登记表。

## 字段

| 字段名         | 注释           | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | -------------- | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键           | int      |      |        | 是   | 否   |
| project_id     | 项目标识       | int      |      |        |      | 否   |
| version        | 版本号         | varchar  | 32   | 0.1.0  |      | 否   |
| title          | 发行版标题     | varchar  | 64   |        |      | 否   |
| description    | 发行版说明     | clob     |      |        |      | 是   |
| jdk_release_id | jdk 发行版标识 | int      |      |        |      | 否   |
| start_time     | 开始时间       | datetime |      |        |      | 否   |
| end_time       | 结束时间       | datetime |      |        |      | 是   |
| release_result | 发布结果       | char     | 2    | 01     |      | 否   |
| log_file_name  | 日志文件名     | varchar  | 255  |        |      | 是   |

## 约束

* 主键：`PK_PROJECT_RELEASE_TASK`
* 外键：(*未设置*)`FK_RELEASE_TASK_PROJECT_ID`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_RELEASE_TASK_PROJECT_ID_VERSION`，对应字段 `project_id`、`version`

## 说明

1. `jdk_release_id` 是指 jdk 的发行版标识
2. `release_result` 的值为：`01` 表示 `未发布(inited)`，`02` 表示 `正在发布(started)`，`03` 表示 `发布失败(failed)`，`04` 表示 `发布成功(passed)`，`05` 表示 `取消发布(canceled)`
3. `log_file_name` 中只存储日志文件名，不包含文件路径
