# `GIT_REPO_PUBLISH_TASK` - Git 仓库发布任务登记表

因为组件发布是一个包含多个节点的流程，耗时也较长，在本表登记发布任务，此表属于任务登记表。

## 字段

| 字段名         | 注释           | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | -------------- | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键           | int      |      |        | 是   | 否   |
| git_url        | git 仓库地址   | varchar  | 128  |        |      | 否   |
| seq            | 仓库的发布编号 | int      |      |        |      | 否   |
| start_time     | 开始时间       | datetime |      |        |      | 否   |
| end_time       | 结束时间       | datetime |      |        |      | 是   |
| publish_type   | 发布类型       | char     | 2    | 01     |      | 否   |
| publish_result | 发布结果       | char     | 2    | 01     |      | 否   |
| log_file_name  | 日志文件名     | varchar  | 255  |        |      | 是   |
| from_version   | 升级前版本号   | varchar  | 32   |        |      | 是   |
| to_version     | 升级后版本号   | varchar  | 32   |        |      | 是   |

## 约束

* 主键：`PK_GIT_REPO_PUBLISH_TASK`
* 索引：`UK_GIT_REPO_PUBLISH_TASK_ON_URL_USER_SEQ`(唯一索引)，对应字段 `git_url`、`seq`、`create_user_id`

## 说明

1. `publish_type` 的值为：`01` 表示 `首次发布(new)`，`02` 表示 `升级(upgrade)`
2. `publish_result` 的值为：`01` 表示 `未发布(inited)`，`02` 表示 `正在发布(started)`，`03` 表示 `发布失败(failed)`，`04` 表示 `发布成功(passed)`，`05` 表示 `取消发布(canceled)`
3. `log_file_name` 中只存储日志文件名，不包含文件路径
4. `from_version` 和 `to_version` 仅用于 `publish_type` 的值为 `02` 时
5. 为了避免有人在市场中抢注仓库名，使用 `@{publisher}/{repo}` 的形式唯一定位一个组件库
6. 用户可以重复为一个组件库添加多次发布，但后续会执行严格校验，以决定是升级还是无需重复发布
7. `seq` 是从 1 开始计数的，因为一个项目可由多人发布，所以需要使用 `git_url`、`seq` 和 `create_user_id` 三个字段联合唯一
8. 在第一次发布时，`from_version` 的值为 `null`，`to_version` 的值为当前版本
