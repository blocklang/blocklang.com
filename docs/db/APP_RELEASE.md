# `APP_RELEASE` - 应用程序发行版基本信息

## 字段

| 字段名         | 注释         | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------ | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键         | int      |      |        | 是   | 否   |
| app_id         | 应用程序标识 | int      |      |        |      | 否   |
| version        | 版本         | varchar  | 32   | 0.1.0  |      | 否   |
| title          | 发行版标题   | varchar  | 64   |        |      | 否   |
| description    | 发行版说明   | clob     |      |        |      | 是   |
| release_time   | 发布时间     | datetime |      |        |      | 否   |
| release_method | 发布方法     | char     | 2    |        |      | 否   |

## 约束

* 主键：`PK_APP_RELEASE`
* 外键：(*未设置*)`FK_APP`，`app_id` 对应 `APP` 表的 `dbid`
* 索引：`UK_APP_ID_VERSION`，对应字段 `app_id`、`version`

## 说明

1. `version` 采用语义化版本，自动发布的 APP，必须严格按照时间增加，在保存记录时要添加版本号的校验逻辑，确保新的版本比上一个发布的版本号大
2. `release_method` 的值为：`01` 表示自动发布，`02` 表示人工上传
3. 一个 APP 必须至少发布一个版本后，才能注册 installer