# `APP_RELEASE_RELATION` - 应用程序发行版依赖关系

依赖关系是指 `APP_RELEASE` 表中记录间的依赖关系，如 Spring Boot Jar 要运行在 JDK 上，则称 Spring Boot Jar 依赖。

## 字段

| 字段名                | 注释                     | 类型 | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ------------------------ | ---- | ---- | ------ | ---- | ---- |
| dbid                  | 主键                     | int  |      |        | 是   | 否   |
| app_release_id        | 应用程序发行版标识       | int  |      |        |      | 否   |
| depend_app_release_id | 依赖的应用程序发行版标识 | int  |      |        |      | 否   |

## 约束

* 主键：`PK_APP_RELEASE_RELATION`
* 外键：无
* 索引：`UK_APP_RELEASE_DEPEND`，对应字段 `app_release_id`、`depend_app_release_id`

## 说明

1. 此表不需要加4个辅助字段，因为这些值与 `APP_RELEASE` 表中的值相同
