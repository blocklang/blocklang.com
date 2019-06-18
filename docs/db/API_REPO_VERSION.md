# `API_REPO_VERSION` - API 仓库的版本信息

API 仓库的版本信息。

## 字段

| 字段名         | 注释               | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------------ | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键               | int      |      |        | 是   | 否   |
| api_repo_id    | API 仓库标识       | int      |      |        |      | 否   |
| version        | API 组件库的版本号 | varchar  | 32   |        |      | 否   |
| create_user_id | 创建人标识         | int      |      |        |      | 否   |
| create_time    | 创建时间           | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_API_REPO_VERSION`
* 外键：无
* 索引：`UK_API_REPO_VERSION_ON_API_REPO_VERSION`，对应字段 `api_repo_id`、`version`

## 说明

1. 不需要 `last_update_user_id` 和 `last_update_time` 字段
2. 只存储在 blocklang 组件市场发布的版本，并不是仓库中的所有 tag 都要存
