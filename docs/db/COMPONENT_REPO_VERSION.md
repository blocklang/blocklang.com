# `COMPONENT_REPO_VERSION` - 组件仓库的版本信息

组件仓库的版本信息。

## 字段

| 字段名              | 注释             | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ---------------- | -------- | ---- | ------ | ---- | ---- |
| dbid                | 主键             | int      |      |        | 是   | 否   |
| component_repo_id   | 组件仓库标识     | int      |      |        |      | 否   |
| version             | 组件库的版本号   | varchar  | 32   |        |      | 否   |
| git_tag_name        | git tag 名称     | varchar  | 32   |        |      | 否   |
| api_repo_version_id | API 库的版本标识 | int      |      |        |      | 否   |
| create_user_id      | 创建人标识       | int      |      |        |      | 否   |
| create_time         | 创建时间         | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_COMPONENT_REPO_VERSION`
* 外键：外键：(*未设置*)`FK_COMPONENT_REPO_VERSION_API_REPO_VERSION`，`api_repo_version_id` 对应 `API_REPO_VERSION` 的 `dbid`
* 索引：`UK_COMPONENT_REPO_VERSION_ON_API_REPO_VERSION`，对应字段 `component_repo_id`、`version`

## 说明

1. 不需要 `last_update_user_id` 和 `last_update_time` 字段
2. 只存储在 blocklang 组件市场发布的版本，并不是仓库中的所有 version/tag 都要存
3. `version` 记录的是 `api.json` 文件中对应的 version 属性值；`git_tag_name` 记录的是 git 仓库的 tag 名称
4. `git_tag_name` 中不包含 `refs/tags/`
