# `APP` - 应用程序基本信息

项目(Project)发布后形成应用程序(APP)。这里的 APP 是指能部署和运行的文件。

## 字段

| 字段名             | 注释       | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------ | ---------- | ------- | ---- | ------ | ---- | ---- |
| dbid               | 主键       | int     |      |        | 是   | 否   |
| project_id         | 项目标识   | int     |      |        |      | 是   |
| app_name           | app 名称   | varchar | 32   |        |      | 否   |

## 约束

* 主键：`PK_APP`
* 外键：(*未设置*)`FK_PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_APP_NAME`，对应字段 `app_name`；`UK_APP_ON_PROJECT_ID`，对应字段 `project_id`

## 说明

1. `project_id` 字段的值可空，因为有些 APP（如 JDK）是直接上传的，不是根据项目编译的