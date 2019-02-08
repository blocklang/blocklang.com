# `USER_AVATAR` - 用户头像信息

## 字段

| 字段名           | 注释         | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ---------------- | ------------ | -------- | ---- | ------ | ---- | ---- |
| dbid             | 主键         | int      |      |        | 是   | 否   |
| user_id          | 用户标识     | int      |      |        |      | 否   |
| avatar_url       | 头像链接     | varchar  | 256  |        |      | 否   |
| size_type        | 尺寸类型     | char     | 2    |        |      | 否   |
| create_time      | 创建时间     | datetime |      |        |      | 否   |
| last_update_time | 最近修改时间 | datetime |      |        |      | 是   |

## 约束

* 主键：`PK_USER_AVATAR`
* 外键：(*未设置*)`FK_USER_AVATAR`，`user_id` 对应 `USER_INFO` 表的 `dbid`
* 索引：`UK_USER_ID_SIZE_TYPE`，对应字段 `user_id`、`size_type`

## 说明

1. 注意，此表不包含创建人和修改人标识，因为只允许用户修改自己的信息
2. `size_type` 的值为：`01` 表示 `small`，`02` 表示 `large`，`03` 表示 `larger`
3. 注意，大部分网站约定 url 的最后一个数值代表头像大小，这里不做拆分处理，都存在 `avatar_url` 中
