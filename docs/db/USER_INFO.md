# `USER_INFO` - 用户信息

## 字段

| 字段名            | 注释           | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ----------------- | -------------- | -------- | ---- | ------ | ---- | ---- |
| dbid              | 主键           | int      |      |        | 是   | 否   |
| login_name        | 登录名         | varchar  | 32   |        |      | 否   |
| nickname          | 昵称           | varchar  | 64   |        |      | 是   |
| enabled           | 是否启用       | boolean  |      | false  |      | 否   |
| is_system_admin   | 是否系统管理员 | boolean  |      | false  |      | 否   |
| avatar_url        | 用户头像链接   | varchar  | 256  |        |      | 否   |
| email             | 邮箱           | varchar  | 64   |        |      | 是   |
| mobile            | 手机号码       | varchar  | 11   |        |      | 是   |
| location          | 所在地区       | varchar  | 256  |        |      | 是   |
| website_url       | 个人主页       | varchar  | 128  |        |      | 是   |
| company           | 公司名称       | varchar  | 128  |        |      | 是   |
| last_sign_in_time | 最近登录时间   | datetime |      |        |      | 是   |
| create_time       | 创建时间       | datetime |      |        |      | 否   |
| last_update_time  | 最近修改时间   | datetime |      |        |      | 是   |

## 约束

* 主键：`PK_USER_INFO`
* 外键：无
* 索引：`UK_LOGIN_NAME`，对应字段 `login_name`；`UK_EMAIL`，对应字段 `email`；`UK_MOBILE`，对应字段 `mobile`

## 说明

1. 注意，此表中不需要再添加创建人和修改人标识
2. `avatar_url` 默认取用户头像中尺寸类型为 `small` 的头像链接
