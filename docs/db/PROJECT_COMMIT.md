# `PROJECT_COMMIT` - 项目提交信息

在 Block Lang 中借助 git 中的 commit 和 tag 概念来做版本控制。

将 Block Lang 中的用户信息与 git commit 等操作的用户信息关联起来，有如下方法：

1. 使用用户的邮箱关联，即用户的邮箱与 git 仓库中 commit 时添加的邮箱要相同，但如果用户修改了邮箱，就关联不上了；
2. 创建一张数据库表，存储 git commit 和用户之间的关系。

Block Lang 采用第二种方式。

## 字段

| 字段名         | 注释             | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ---------------- | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键             | int      |      |        | 是   | 否   |
| project_id     | 项目标识         | int      |      |        |      | 否   |
| branch         | 仓库分支名       | varchar  | 32   | master |      | 否   |
| commit_id      | git commit 标识  | varchar  | 128  |        |      | 否   |
| commit_user_id | commit 用户标识  | int      |      |        |      | 否   |
| commit_time    | commit 时间      | datetime |      |        |      | 否   |
| short_message  | 提交内容概要说明 | varchar  | 128  |        |      | 否   |
| full_message   | 提交内容详细说明 | text     |      |        |      | 是   |
| create_user_id | 创建人标识       | int      |      |        |      | 否   |
| create_time    | 创建时间         | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_PROJECT_COMMIT`
* 外键：(*未设置*)`FK_PROJECT_COMMIT`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_PROJECT_COMMIT_ON_PROJECT_ID_BRANCH_COMMIT_ID`，对应字段 `project_id`、`branch`、`commit_id`；`IDX_PROJECT_COMMIT_ON_COMMIT_TIME`，对应字段 `commit_time`

## 说明

1. 注意，本表中不包含 4 个辅助字段