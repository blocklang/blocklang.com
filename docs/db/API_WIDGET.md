# `API_WIDGET` -  Widget 基本信息

曾经尝试用一套模型来描述 Widget、Service、WebApi，但发现三者的属性差别太大，因为每一类组件都单独存储。

只存储 API 信息，不存储组件（对 API 的实现）信息。

页面或服务中引用的的是 `code`，而不是 `dbid`。目的是当部件升级时，不用修改页面或服务中的配置，而是直接修改项目依赖的版本号即可。

## 字段

| 字段名              | 注释             | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ---------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键             | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识 | int     |      |        |      | 否   |
| code                | Widget 的编码       | char    | 4    |        |      | 否   |
| name                | Widget 的名称       | varchar | 64   |        |      | 否   |
| label               | Widget 的显示名     | varchar | 64   |        |      | 是   |
| description         | Widget 的详细说明   | varchar | 512  |        |      | 是   |

## 约束

* 主键：`PK_API_WIDGET`
* 外键：(*未设置*)`FK_API_WIDGET_REPO_VERSION`，`API_REPO_VERSION_ID` 对应 `API_REPO_VERSION` 表的 `dbid`
* 索引：`UK_API_WIDGET_ON_API_REPO_VERSION_NAME`，对应字段 `api_repo_version_id`、`name`；`UK_API_WIDGET_ON_API_REPO_VERSION_CODE`，对应字段 `api_repo_version_id`、`code`

## 说明

1. `API_WIDGET` 只与 `API_REPO_VERSION` 表有关联，与 `COMPONENT_REPO_VERSION` 表无关
2. `CODE` 是组件的编码，一个组件的编码确定后，就不能再变更；在页面中引用 Widget 时，使用的不是 `dbid`，而是 `code`
3. 一个组件库对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
4. Widget 的每个版本，以及每个版本的属性和每个版本的属性可选值都要在相关表中存一份
5. 不同 Widget 的同一个部件，则 `code` 的值必须相同
