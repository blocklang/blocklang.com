# `PROJECT_BUILD_PROFILE` - 项目构建的 Profile

项目依赖可规范三类：API、IDE、PROD。Profile 只用于 PROD。通过 profile，实现了在构建时根据不同的 profile 来构建项目。

## 字段

| 字段名        | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid          | 主键         | int     |      |        | 是   | 否   |
| repository_id | 仓库标识     | int     |      |        |      | 否   |
| project_id    | 项目标识     | int     |      |        |      | 否   |
| app_type      | 程序类型     | char    | 2    |        |      | 否   |
| profile_name  | Profile 名称 | varchar | 64   |        |      | 否   |

## 约束

* 主键：`PK_PROJECT_BUILD_PROFILE`
* 外键：(*未设置*)`FK_PROJECT_BUILD_PROFILE_ON_PROJECT_ID`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`IDX_PROJECT_BUILD_PROFILE_ON_REPO_ID`(普通索引)，对应字段 `repository_id`;`UK_PROJECT_BUILD_PROFILE_ON_PROJECT_APP_NAME`(唯一索引)，对应字段 `project_id`、`app_type`、`profile_name`

## 说明

1. 一个项目的每个 `app_type` 有多个 Profile
2. `profile_name` 默认会为每个项目生成一个名为 `default` 的 profile，并且不能修改此名
3. `repository_id` 是冗余字段，方便查出一个仓库中所有项目信息
