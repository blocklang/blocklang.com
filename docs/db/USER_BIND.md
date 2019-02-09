# `USER_BIND` - 用户与社交帐号绑定信息

## 字段

| 字段名           | 注释               | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ---------------- | ------------------ | -------- | ---- | ------ | ---- | ---- |
| dbid             | 主键               | int      |      |        | 是   | 否   |
| user_id          | 用户标识           | int      |      |        |      | 否   |
| site             | 第三方网站标识     | char     | 2    |        |      | 否   |
| open_id          | 第三方网站用户标识 | varchar  | 64   |        |      | 否   |
| create_time      | 创建时间           | datetime |      |        |      | 否   |
| last_update_time | 最近修改时间       | datetime |      |        |      | 是   |

## 约束

* 主键：`PK_USER_BIND`
* 外键：(*未设置*)`FK_USER_BIND`，`user_id` 对应 `USER_INFO` 表的 `dbid`
* 索引：`UK_USER_ID_SITE`，对应字段 `user_id`、`site`

## 说明

1. 注意，此表不包含创建人和修改人标识，因为只允许用户修改自己的信息
2. `expires_in` 的计量单位是 `秒`
3. `site` 的值为：`10` 表示 `github`，`11` 表示 `微信`
