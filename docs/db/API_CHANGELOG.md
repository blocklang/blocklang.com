# `API_CHANGELOG` - API 更新日志

一次版本发布，记录一次 API 更新日志。

## 字段

| 字段名              | 注释               | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------------ | -------- | ---- | ------ | ---- | ---- |
| dbid                | 主键               | int      |      |        | 是   | 否   |
| api_repo_id         | API 仓库版本标识   | int      |      |        |      | 否   |
| changelog_id        | 变更日志标识       | varchar  | 255  |        |      | 否   |
| changelog_author    | 变更日志的作者     | varchar  | 255  |        |      | 否   |
| changelog_file_name | 变更日志的存储路径 | varchar  | 255  |        |      | 否   |
| execute_time        | 执行时间           | datetime |      |        |      | 否   |
| execute_order       | 执行顺序           | int      |      |        |      | 否   |
| execute_result      | 执行结果           | char     | 2    |        |      | 否   |
| md5_sum             | 校验日志文件的 md5 | varchar  | 64   |        |      | 否   |
| deployment_id       | 部署标识           | int      |      |        |      | 否   |
| create_user_id      | 创建人标识         | int      |      |        |      | 否   |
| create_time         | 创建时间           | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_API_CHANGELOG`
* 外键：(*未设置*)`FK_API_REPO_DEPLOY`，`API_REPO_VERSION_ID` 对应 `API_REPO_VERSION` 表的 `dbid`
* 索引：`UK_API_CHANGELOG_ON_API_REPO_ID_AUTHOR_FILE`，对应字段 `api_repo_id`、`changelog_id`、`changelog_author`、`changelog_file_name`

## 说明

1. 不需要 `last_update_user_id` 和 `last_update_time` 字段
2. `execute_time` 记录的是执行开始时间
3. `execute_order` 先按照在 `api.json` 中 `components` 的登记顺序查找每个组件，然后每个组件按照版本的先后顺序执行
4. `execute_result` 的值为：`01` 表示 `SUCCESS(成功)`，`02` 表示 `FAILED(失败)`
5. `deployment_id` 中填写的值是 `API_REPO_VERSION` 表中的 `dbid`，因为是按版本发布的，如果没有发现新版本，就不执行发布操作
6. `deployment_id` 实际存的值是 `API_REPO_VERSION` 的 `dbid`，比 `API_VERSION` 的 `dbid` 更具体，但是需要在项目一级来确保日志记录的唯一性，所以依然加入 `api_repo_id` 字段
