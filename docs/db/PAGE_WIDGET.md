# `PAGE_WIDGET` - 页面部件

页面的外观是由多个 Widget 逐层嵌套组成的，页面只能有一个根 Widget，且取名为 `Page`。

## 字段

| 字段名              | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键         | varchar | 32   |        | 是   | 否   |
| parent_id           | 父部件标识   | varchar | 32   |        |      | 否   |
| seq                 | 序列         | int     |      |        |      | 否   |
| project_resource_id | 项目资源标识 | int     |      |        |      | 否   |
| api_repo_id         | API 仓库标识 | int     |      |        |      | 否   |
| widget_code         | 部件编码     | char    | 4    |        |      | 否   |

## 约束

* 主键：`PK_PAGE_WIDGET`
* 外键：(*未设置*)`FK_PAGE_WIDGET_ON_RESOURCE_ID`，`project_resource_id` 对应 `PROJECT_RESOURCE` 表的 `dbid`
* 索引：无

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `dbid` 中存的不是自增长的数字，而是 32 位的 uuid，因为 uuid 是全局唯一的，可以在设计器中生成，能减少转换处理，如果使用数字，因为依赖于数据库表，客户端的设计器中生成时需要实时查询，复杂度增加
3. `parent_id` 对应本表中的 `id` 的值，而根节点的值为 `-1`
4. `seq` 就是页面级别排序，每个页面都是从 1 开始。不是全表范围排序，也不是页面树状结构的同一层级排序
5. `widget_code` 对应 `API_COMPONENT` 表中的 `code` 字段，因为本表只会用到 Widget 类型的组件，所以此字段以 `widget_` 开头，而不是以 `component_` 开头
6. 不能给 `project_resource_id`、`api_repo_id` 和 `widget_code` 三个字段上添加联合唯一约束，因为一个页面会多次使用同一个组件
7. 定义页面与部件之间的关系时并没有使用 `API_COMPONENT` 表中的 `id`，而是使用 `API_REPO` 的 `id` 和 `API_COMPONENT` 的 `CODE` 两个字段，为的是当 API 仓库升级后，不同步修改本表的数据，也不影响之前配置好的关系


TODO: 增加 slotName 属性，支持添加多个子部件，slotName 的默认值为 default？
