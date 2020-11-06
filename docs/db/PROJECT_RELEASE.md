# `PROJECT_RELEASE` - 项目发布

记录一个仓库中的一个项目的发布信息。注意：

1. 如果某一个版本发布了多次，则只记录最新信息
2. 发布成功才在此表中添加记录，发布失败不添加

## 字段

| 字段名        | 注释          | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------- | ------------- | ------- | ---- | ------ | ---- | ---- |
| dbid          | 主键          | int     |      |        | 是   | 否   |
| repository_id | 仓库标识      | int     |      |        |      | 否   |
| project_id    | 项目标识      | int     |      |        |      | 否   |
| git_commit_id | git commit id | varchar | 64   |        |      | 否   |
| version       | 版本号        | varchar | 32   |        |      | 否   |
| build_target  | 构建目标平台  | varchar | 32   |        |      | 否   |

## 约束

* 主键：`PK_PROJECT_RELEASE`
* 外键：(*未设置*)`FK_RELEASE_REPO_ID`，`REPOSITORY_ID` 对应 `REPOSITORT` 表的 `dbid`
* 索引：`UK_PROJECT_RELEASE_ON_REPO_PRO_VERSION_BUILD`, 对应字段 `repository_id`、`project_id`、`version` 和 `build_target`

## 说明

1. `git_commit_id` 是模型仓库中一个项目目录上最新的 commit id
2. `version` 表示语义化版本号，但如果发布的是 master 分支最新内容，则值为 `master`
3. 如果发布的是 `master` 分支，则在发布前将 git 仓库的最新 commit id 和 本表中的 `git_commit_id` 进行比较，如果相同，则不需要发布
