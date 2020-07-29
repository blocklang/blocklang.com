# `API_REPO_VERSION` - API 仓库的版本信息

API 仓库的版本信息，其中存储 git 仓库的所有 tag 信息和 master 分支信息。

## 字段

| 字段名            | 注释               | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ----------------- | ------------------ | -------- | ---- | ------ | ---- | ---- |
| dbid              | 主键               | int      |      |        | 是   | 否   |
| api_repo_id       | API 组件库标识     | int      |      |        |      | 否   |
| version           | API 组件库的版本号 | varchar  | 32   |        |      | 否   |
| git_tag_name      | git tag 名称       | varchar  | 32   |        |      | 否   |
| name              | 组件库的名称       | varchar  | 64   |        |      | 否   |
| display_name      | 组件库的显示名     | varchar  | 64   |        |      | 是   |
| description       | 组件库的详细说明   | varchar  | 512  |        |      | 是   |
| create_user_id    | 创建人标识         | int      |      |        |      | 否   |
| create_time       | 创建时间           | datetime |      |        |      | 否   |
| last_publish_time | 最近发布时间       | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_API_REPO_VERSION`
* 外键：无
* 索引：`UK_API_REPO_VERSION_ON_API_REPO_VERSION`，对应字段 `api_repo_id`、`version`

## 说明

1. 不需要 `last_update_user_id` 和 `last_update_time` 字段
2. 存储内容包括仓库的所有 git tag 和 master 分支
3. 如果对应的是 git tag，则 `version` 的值是 git tag 名称中的语义化版本号；如果是 master 分支，则值为 `master`
4. `git_tag_name`，如果是 tag 分支，则要包含 `refs/tags/` 前缀，如果是 master 分支，则值固定为 `refs/heads/master`
5. 如果是 tag 分支，则 `create_time` 和 `last_publish_time` 的值相同，如果是 master 分支，因为 master 每次都重新发布，所以 `last_publish_time` 存的是最近发布时间
