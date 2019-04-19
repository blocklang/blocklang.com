# `PERSISTENT_LOGINS` - 记住用户登录信息

本表用于存储自动化登录功能相关的信息。这里的表名与 Spring Security 中的名字相同。

## 字段

| 字段名         | 注释         | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------ | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键         | int      |      |        | 是   | 否   |
| login_name     | 登录名       | varchar  | 32   |        |      | 否   |
| token          | 登录标识     | varchar  | 64   |        |      | 否   |
| last_used_time | 最后使用时间 | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_PERSISTENT_LOGINS`
* 外键：无
* 索引：`UK_PERSISTENT_LOGINS_ON_LOGIN_NAME`，对应字段 `login_name`；`UK_PERSISTENT_LOGINS_ON_TOKEN`，对应字段 `token`

## 说明

1. 注意，此表中不需要再添加创建人和修改人标识
