# `PAGE_FUNC_NODE_PORT` - 节点中的端口

一个节点可能包含两种端口：

1. 序列端口
2. 数据端口

而每种端口又分为：

1. 输入型端口
2. 输出型端口

页面中显示的引用自组件或部件中的文本，要支持组件版本升级。

## 字段

| 字段名                    | 注释                     | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------------- | ------------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                      | 主键                     | varchar | 32   |        | 是   | 否   |
| project_resource_id       | 项目资源标识             | int     |      |        |      | 否   |
| page_func_node_id         | 页面函数节点标识         | varchar | 32   |        |      | 否   |
| port_type                 | 端口类型                 | varchar | 32   |        |      | 否   |
| flow_type                 | 数据流方向               | varchar | 32   |        |      | 否   |
| output_sequence_port_text | 输出型序列端口的显示文本 | varchar | 64   |        |      | 是   |
| input_data_port_value     | 输入型数据端口上的默认值 | varchar | 64   |        |      | 是   |

| bind_source               | 节点绑定的数据源         | varchar | 16   |        |      | 否   |
| api_repo_id               | API 仓库标识             | int     |      |        |      | 是   |
| code                      | 组件编码                 | varchar | 32   |        |      | 否   |

FIXME: __这里结合 page_func_node_id 和 code 来定义参数信息，所以可以删除 bind_source 和 api_repo_id?__

此处的 code 直接对应组件定义的输入参数和返回结果， code -> param_code? 如果是input,则为输入参数，如果为 output,则为输出参数

## 约束

* 主键：`PK_PAGE_FUNC_NODE_PORT`
* 外键：(*未设置*)`FK_PAGE_FUNC_NODE_PORT_ON_NODE_ID`，`page_func_node_id` 对应 `PAGE_FUNC_NODE` 表的 `dbid`
* 索引：`IDX_PAGE_FUNC_NODE_PORT_ON_PROJECT_RESOURCE_ID`(普通索引)，对应字段 `project_resource_id`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `project_resource_id` 是一个冗余字段，便于快速查找出一个页面中所有事件处理函数的节点中的端口
3. `port_type` 的值为：`sequence` 表示序列端口，`data` 表示数据端口
4. `flow_type` 的值为：`output` 表示输出型端口，`input` 表示输入型端口
5. `output_sequence_port_text` 只用于当 `port_type` 的值为 `sequence`，`flow_type` 的值为 `output` 时，所以在字段名上显式标识出来
6. `port_type` 的值为 `sequence` 时，`bind_source`、`api_repo_id` 和 `code` 的值为空；
7. `data_port_name`（数据端口上的显示文本）和 `data_port_value_type`（数据端口上的值类型）的值不在本表存储，而是通过 `bind_source`、`api_repo_id` 和 `code` 推导；如果是设置或获取变量，则根据变量的类型和名称推导
