# `PROJECT_RESOURCE` - 项目资源

一个项目是由多个资源组成的，资源又分多种，如页面、分组、数据服务、文档等。

## 字段

| 字段名        | 注释     | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------- | -------- | ------- | ---- | ------ | ---- | ---- |
| dbid          | 主键     | int     |      |        | 是   | 否   |
| project_id    | 项目标识 | int     |      |        |      | 否   |
| resource_key  | 资源标识 | varchar | 32   |        |      | 否   |
| resource_name | 资源名称 | varchar | 32   |        |      | 否   |
| resource_desc | 资源描述 | varchar | 64   |        |      | 是   |
| resource_type | 资源类型 | char    | 2    |        |      | 否   |
| app_type      | 程序类型 | char    | 2    |        |      | 否   |
| parent_id     | 父标识   | int     |      | -1     |      | 否   |
| seq           | 排序     | int     |      |        |      | 否   |

## 约束

* 主键：`PK_PROJECT_RESOURCE`
* 外键：(*未设置*)`FK_RESOURCE_PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_PROJECT_RESOURCE_ON_PROJECT_PARENT_RESOURCE_APP_KEY`，对应字段 `project_id`、`parent_id`、`resource_type`、`app_type`、`resource_key`；`UK_PROJECT_RESOURCE_ON_PROJECT_PARENT_RESOURCE_APP_NAME`，对应字段 `project_id`、`parent_id`、`resource_type`、`app_type`、`resource_name`

## 说明

1. `resource_key` 由英文字母或拼音等组成，在 url 中使用，同一个功能模块下不能有同名的资源标识
2. `resource_name` 在界面中显示，同一个功能模块下不能有同名的模块名称
3. `resource_type` 的值为：`01` 表示 `分组`，`02` 表示 `页面`，`03` 表示 `面板`，`04` 表示 `页面模板`，`05` 表示 `文件`，`06` 表示 `服务`
4. `resource_type` 的值为 `面板` 时，表示可供多个 `程序模块` 重用的内容
5. `app_type` 仅适用于当 `resource_type` 的值为 `02` 时，其他情况，`app_type` 的值都为 `99` 即 `unknown`
6. `app_type` 的值为：`01` 表示 `web`，`02` 表示 `android`，`03` 表示 `ios`，`04` 表示 `微信小程序(wechatMiniApp)`，`05` 表示 `支付宝小程序(alipayMiniApp)`，`06` 表示 `快应用(quickApp)`，`99` 表示 `unknown(不属于任何 APP，如 README.md 文件)`
7. `parent_id` 其中的值为 `-1` 时，表示顶级节点
8. `seq` 是在同一层级排序，不是全表范围内排序，从 1 开始排
9. 唯一索引 `UK_PROJECT_ID_RES_KEY_TYPE_APP_PARENT` 中包含 `app_type`，是为了当支持多平台时，可以使用相同的名称
