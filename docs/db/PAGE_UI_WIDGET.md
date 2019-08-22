# `PAGE_UI_WIDGET` - 页面部件

页面的外观是由多个 Widget 逐层嵌套组成的，页面只能有一个根 Widget，且取名为 `Page`。

## 字段

| 字段名              | 注释         | 类型 | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------ | ---- | ---- | ------ | ---- | ---- |
| dbid                | 主键         | int  |      |        | 是   | 否   |
| parent_id           | 父部件标识   | int  |      |        |      | 否   |
| seq                 | 序列         | int  |      |        |      | 否   |
| project_resource_id | 项目资源标识 | int  |      |        |      | 否   |
| api_repo_id         | API 仓库标识 | int  |      |        |      | 否   |
| widget_code         | 部件编码     | char | 4    |        |      | 否   |

## 约束

* 主键：`PK_PAGE_UI_WIDGET`
* 外键：(*未设置*)`FK_PAGE_UI_WIDGET_ON_RESOURCE_ID`，`project_resource_id` 对应 `PROJECT_RESOURCE` 表的 `dbid`
* 索引：无

## 说明

1. `seq` 不是页面级别排序，而是同一层级内排序，每层都是从 1 开始
2. `widget_code` 对应 `API_COMPONENT` 表中的 `code` 字段，因为本表只会用到 Widget 类型的组件，所以此字段以 `widget_` 开头，而不是以 `component_` 开头
3. 不能给 `project_resource_id`、`api_repo_id` 和 `widget_code` 三个字段上添加联合唯一约束，因为一个页面会多次使用同一个组件
4. 定义页面与部件之间的关系时并没有使用 `API_COMPONENT` 表中的 `id`，而是使用 `API_REPO` 的 `id` 和 `API_COMPONENT` 的 `CODE` 两个字段，为的是当 API 仓库升级后，不同步修改本表的数据，也不影响之前配置好的关系
