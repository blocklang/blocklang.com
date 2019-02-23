# `PROJECT_AUTHORIZATION` - 项目授权信息

## 字段

| 字段名         | 注释       | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ---------- | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键       | int      |      |        | 是   | 否   |
| user_id        | 用户标识   | int      |      |        |      | 否   |
| project_id     | 项目标识   | int      |      |        |      | 否   |
| access_level   | 访问级别   | char     | 2    |        |      | 否   |
| create_user_id | 创建人标识 | int      |      |        |      | 否   |
| create_time    | 创建时间   | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_PROJECT_AUTHORIZATION`
* 外键：无
* 索引：`UK_PROJECT_AUTHORIZATION_ON_USER_ID_PROJECT_ID_ACCESS_LEVEL`，对应字段 `user_id`、`project_id`、`access_level`

## 说明

1. `access_level` 的值为：`01` 表示 `read(只读)`，`02` 表示 `write(可读写)`，`03` 表示 `admin(管理)`
2. 注意：本表不需要最近修改人和最近修改时间