# `PAGE_FUNC_NODE` - 可视化函数设计器中的节点

在可视化的函数设计器中，通过节点、端口和连接线来描述函数定义、函数调用顺序以及数据传输关系。

1. 函数节点表示函数定义和函数调用
2. 函数节点中的序列端口描述函数的调用顺序
3. 函数节点中的数据端口描述函数的输入参数和返回值的传递关系

页面中显示的引用自组件或部件中的文本，要支持组件版本升级。

## 字段

| 字段名        | 注释                        | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------- | --------------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid          | 主键                        | varchar | 32   |        | 是   | 否   |
| page_func_id  | 页面函数标识                | varchar | 32   |        |      | 否   |
| left          | 相对于设计器左上角的 x 坐标 | int     |      |        |      | 否   |
| top           | 相对于设计器左上角的 y 坐标 | int     |      |        |      | 否   |
| category      | 节点类型                    | varchar | 16   |        |      | 否   |
| function_type | 定义或调用的函数类型        | varchar | 16   |        |      | 否   |
| bind_source   | 节点绑定的数据源            | varchar | 16   |        |      | 是   |
| api_repo_id   | API 仓库标识                | int     |      |        |      | 是   |
| code          | 组件编码                    | varchar | 32   |        |      | 是   |

## 约束

* 主键：`PK_PAGE_FUNC_NODE`
* 外键：(*未设置*)`FK_PAGE_FUNC_NODE_ON_FUNC_ID`，`page_func_id` 对应 `PAGE_FUNC` 表的 `dbid`
* 索引：无

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `category` 字段与节点在可视化设计器中的布局有关：`flowControl` 表示使用流程控制的节点布局，`data` 表示使用数据的节点布局
3. `function_type` 的值为：`function` 表示函数定义，`functionCall` 表示函数调用，`variableSet` 表示为变量设置值，`variableGet` 表示获取变量的值
4. 如果 `function_type` 的值为 `function`，则 `bind_source`、`api_api_id` 和 `code` 三个字段的值为空
5. `bind_source` 的值为：`data` 表示取自页面数据，`service` 表示取自 RESTful API
6. 如果 `bind_source` 的值为 `data`，则 `api_repo_id` 的值为空，`code` 的值为 `page_data` 中的 `dbid`；如果 `bind_source` 的值为 `service`，则 `api_repo_id` 的值为 `API_REPO` 表中的 `dbid`，`code` 的值为 `API_COMPONENT` 表中的 `code`


另一种设计：

* 如果 `function_type` 的值为 `function`，则 `bind_source` 的值为 `WidgetEvent`(引用部件事件)、`api_api_id` 的值为 `API_REPO` 表中的 `dbid`，`code` 的值为 `API_COMPONENT` 表中的 `code`。但是这些值可以通过 `PAGE_FUNC` 中的 `dbid` 关联出来，所以暂时不用这个设计
