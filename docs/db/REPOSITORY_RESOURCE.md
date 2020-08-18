# `REPOSITORY_RESOURCE` - 仓库资源

一个仓库是由多个资源组成的，资源又分多种，如项目、分组、页面、数据服务、文档等。

注意：

1. 约定项目资源只能放在仓库的根目录下；
2. 同一个项目下资料的 app type 值必须相同

## 字段

| 字段名        | 注释     | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------- | -------- | ------- | ---- | ------ | ---- | ---- |
| dbid          | 主键     | int     |      |        | 是   | 否   |
| repository_id | 仓库标识 | int     |      |        |      | 否   |
| resource_key  | 资源标识 | varchar | 32   |        |      | 否   |
| resource_name | 资源名称 | varchar | 32   |        |      | 是   |
| resource_desc | 资源描述 | varchar | 64   |        |      | 是   |
| resource_type | 资源类型 | char    | 2    |        |      | 否   |
| app_type      | 程序类型 | char    | 2    |        |      | 否   |
| parent_id     | 父标识   | int     |      | -1     |      | 否   |
| seq           | 排序     | int     |      |        |      | 否   |

## 约束

* 主键：`PK_REPOSITORY_RESOURCE`
* 外键：(*未设置*)`FK_REPOSITORY_PROJECT`，`REPOSITORY_ID` 对应 `REPOSITORY` 表的 `dbid`
* 索引：`UK_REPO_RESOURCE_ON_PROJECT_PARENT_RESOURCE_APP_KEY`，对应字段 `respository_id`、`parent_id`、`resource_type`、`resource_key`；`UK_REPO_RESOURCE_ON_PROJECT_PARENT_RESOURCE_APP_NAME`，对应字段 `respository_id`、`parent_id`、`resource_type`、`resource_name`

## 说明

1. `resource_key` 由英文字母或拼音等组成，在 url 中使用，同一个功能模块下不能有同名的资源标识
2. `resource_name` 在界面中显示，同一个功能模块下不能有同名的模块名称
3. `resource_type` 的值为：`01` 表示项目，`02` 表示 `项目入口`，`03` 表示 `分组`，`04` 表示 `页面`，`05` 表示 `面板`，`06` 表示 `页面模板`，`07` 表示 `文件`，`08` 表示 `服务`，`09` 表示 `依赖`，`10` 表示 `Build 配置信息`
4. `resource_type` 的值为 `面板` 时，表示可供多个 `程序模块` 重用的内容
5. `app_type` 适用于所有 `resource_type`
6. `app_type` 的值为：`01` 表示 `web`，`02` 表示 `mobile`，`03` 表示 `miniProgram`，`99` 表示 `unknown(不属于任何 APP，如 README.md 文件)`
7. `parent_id` 其中的值为 `-1` 时，表示顶级节点
8. `seq` 是在同一层级排序，不是全表范围内排序，从 1 开始排
9. 唯一索引 `UK_REPO_RESOURCE_ON_PROJECT_PARENT_RESOURCE_APP_KEY` 中没有包含 `app_type`，因为规范仓库结构后，不允许同一个目录下 `app_type` 不同，但 `resource_key` 相同的情况出现。
