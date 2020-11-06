# `PROJECT_RELEASE_TASK` - 项目发布任务登记表

因为项目发布是一个包含多个节点的流程，耗时也较长，在本表登记发布任务，此表属于任务登记表。

## 字段

| 字段名         | 注释           | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | -------------- | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键           | int      |      |        | 是   | 否   |
| repository_id  | 仓库标识       | int      |      |        |      | 否   |
| project_id     | 项目标识       | int      |      |        |      | 否   |
| git_commit_id  | git commit id  | varchar  | 64   |        |      | 否   |
| version        | 版本号         | varchar  | 32   |        |      | 否   |
| title          | 发行版标题     | varchar  | 64   |        |      | 否   |
| build_target   | 构建目标平台   | varchar  | 32   |        |      | 是   |
| description    | 发行版说明     | clob     |      |        |      | 是   |
| jdk_release_id | jdk 发行版标识 | int      |      |        |      | 是   |
| start_time     | 开始时间       | datetime |      |        |      | 否   |
| end_time       | 结束时间       | datetime |      |        |      | 是   |
| release_result | 发布结果       | char     | 2    | 01     |      | 否   |
| log_file_name  | 日志文件名     | varchar  | 255  |        |      | 是   |

## 约束

* 主键：`PK_PROJECT_RELEASE_TASK`
* 外键：(*未设置*)`FK_RELEASE_TASK_REPO_ID`，`REPOSITORY_ID` 对应 `REPOSITORT` 表的 `dbid`
* 索引：无

## 说明

1. `jdk_release_id` 是指 jdk 的发行版标识
2. `release_result` 的值为：`01` 表示 `未发布(inited)`，`02` 表示 `正在发布(started)`，`03` 表示 `发布失败(failed)`，`04` 表示 `发布成功(passed)`，`05` 表示 `取消发布(canceled)`
3. `log_file_name` 中只存储日志文件名，不包含文件路径
4. `version`，如果发布的是 master 分支最新内容，则值为 `master`；一个项目的某一个版本可以多次发布
5. `build_target` 指发布的目标平台，如 `weapp` 表示 `微信小程序`，`swan` 表示 `百度小程序`，`alipay` 表示 `支付宝小程序`，`tt` 表示 `字节跳动小程序`，`qq` 表示 `QQ小程序`，`jd` 表示 `京东小程序`，`quickapp` 表示 `快应用`
6. `git_commit_id` 是模型仓库中一个项目目录上最新的 commit id
