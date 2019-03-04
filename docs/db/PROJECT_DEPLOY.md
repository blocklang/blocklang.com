# `PROJECT_DEPLOY` - 项目部署信息

## 字段

| 字段名             | 注释       | 类型 | 长度 | 默认值 | 主键 | 可空 |
| ------------------ | ---------- | ---- | ---- | ------ | ---- | ---- |
| dbid               | 主键       | int  |      |        | 是   | 否   |
| project_id         | 项目标识   | int  |      |        |      | 否   |
| user_id            | 用户标识   | int  |      |        |      | 否   |
| registration_token | 注册 token | char | 22   |        |      | 否   |

## 约束

* 主键：`PK_PROJECT_DEPLOY`
* 外键：(*未设置*)`FK_PROJECT_DEPLOY`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_PROJECT_DEPLOY_ON_PROJECT_ID_USER_ID_REG_TOKEN`，对应字段 `project_id`、`user_id`、`registration_token`；`UK_PROJECT_DEPLOY_ON_REG_TOKEN`，对应字段 `registration_token`

## 说明

1. `registration_token` 是22位的 UUID