# `REPOSITORY_TAG` - GIT 仓库附注标签信息

为仓库创建 GIT 附注标签

## 字段

| 字段名        | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid          | 主键         | int     |      |        | 是   | 否   |
| repository_id | 仓库标识     | int     |      |        |      | 否   |
| version       | 版本号       | varchar | 32   | 0.1.0  |      | 否   |
| git_tag_id    | git 标签标识 | varchar | 50   |        |      | 否   |

## 约束

* 主键：`PK_REPOSITORY_TAG`
* 外键：(*未设置*)`FK_TAG_REPOSITORY`，`REPOSITORY_ID` 对应 `REPOSITORY` 表的 `dbid`
* 索引：`UK_TAG_REPO_ID_VERSION`，对应字段 `repository_id`、`version`

## 说明

1. `version` 的取值遵循语义化版本号规范
2. git 标签名称是在 `version` 的值前加上字母 `v`，如 `v0.1.0`
3. `git_tag_id` 存 git 附注标签的标识
