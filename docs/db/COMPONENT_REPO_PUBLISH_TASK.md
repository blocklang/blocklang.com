# `COMPONENT_REPO_PUBLISH_TASK` - 组件发布任务登记表

因为组件发布是一个包含多个节点的流程，耗时也较长，在本表登记发布任务，此表属于任务登记表。

## 字段

| 字段名         | 注释         | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------ | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键         | int      |      |        | 是   | 否   |
| git_url        | git 仓库地址 | varchar  | 128  |        |      | 否   |
| start_time     | 开始时间     | datetime |      |        |      | 否   |
| end_time       | 结束时间     | datetime |      |        |      | 是   |
| publish_result | 发布结果     | char     | 2    | 01     |      | 否   |
| log_file_name  | 日志文件名   | varchar  | 255  |        |      | 是   |

## 约束

* 主键：`PK_COMPONENT_REPO_PUBLISH_TASK`
* 索引：`UK_COMP_REPO_PUBLISH_TASK_ON_GIT_URL_USER_ID`，对应字段 `git_url`、`create_user_id`

## 说明

1. `publish_result` 的值为：`01` 表示 `未发布(inited)`，`02` 表示 `正在发布(started)`，`03` 表示 `发布失败(failed)`，`04` 表示 `发布成功(passed)`，`05` 表示 `取消发布(canceled)`
2. `log_file_name` 中只存储日志文件名，不包含文件路径
3. 为了避免有人在市场中抢注仓库名，使用 `@{publisher}/{repo}` 的形式唯一定位一个组件库
