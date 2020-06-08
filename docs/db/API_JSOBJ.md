# `API_JSOBJ` -  JavaScript 对象基本信息

JavaScript 对象分两层描述，第一层是 Object，第二层是函数。

页面或服务中引用的的是 `code`，而不是 `dbid`。目的是当部件升级时，不用修改页面或服务中的配置，而是直接修改项目依赖的版本号即可。

## 字段

| 字段名              | 注释                      | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键                      | int     |      |        | 是   | 否   |
| api_repo_version_id | API 仓库版本标识          | int     |      |        |      | 否   |
| code                | JavaScript 对象的编码     | char    | 4    |        |      | 否   |
| name                | JavaScript 对象的名称     | varchar | 64   |        |      | 否   |
| description         | JavaScript 对象的详细说明 | varchar | 512  |        |      | 是   |

## 约束

* 主键：`PK_API_JSOBJ`
* 外键：(*未设置*)`FK_API_JSOBJ_REPO_VERSION`，`API_REPO_VERSION_ID` 对应 `API_REPO_VERSION` 表的 `dbid`
* 索引：`UK_API_JSOBJ_ON_API_REPO_VERSION_NAME`，对应字段 `api_repo_version_id`、`name`；`UK_API_JSOBJ_ON_API_REPO_VERSION_CODE`，对应字段 `api_repo_version_id`、`code`

## 说明

1. `API_JSOBJ` 只与 `API_REPO_VERSION` 表有关联，与 `COMPONENT_REPO_VERSION` 表无关
2. `CODE` 是组件的编码，一个组件的编码确定后，就不能再变更；在页面中引用 JavaScript 对象时，使用的不是 `dbid`，而是 `code`
3. 一个组件库对应一套编码，`CODE` 的值从 `0001` 开始，到 `9999` 结束
4. JavaScript 对象的每个版本，以及每个版本的属性和每个版本的属性可选值都要在相关表中存一份
5. 不同版本的同一个 JavaScript 对象，则 `code` 的值必须相同
