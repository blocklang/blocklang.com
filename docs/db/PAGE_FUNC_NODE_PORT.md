# `PAGE_FUNC_NODE_PORT` - 节点中的端口

一个节点可能包含两种端口：

1. 序列端口
2. 数据端口

而每种端口又分为：

1. 输入型端口
2. 输出型端口

## 字段

| 字段名                    | 注释                     | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------------- | ------------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                      | 主键                     | varchar | 32   |        | 是   | 否   |
| page_func_node_id         | 页面函数节点标识         | varchar | 32   |        |      | 否   |
| port_type                 | 端口类型                 | varchar | 32   |        |      | 否   |
| flow_type                 | 数据流方向               | varchar | 32   |        |      | 否   |
| output_sequence_port_text | 输出型序列端口的显示文本 | varchar | 64   |        |      | 是   |
| data_port_name            | 数据端口上的显示文本     | varchar | 64   |        |      | 是   |
| data_port_value_type      | 数据端口上的值类型       | varchar | 64   |        |      | 是   |
| input_data_port_value     | 输入型数据端口上的默认值 | varchar | 64   |        |      | 是   |

| bind_source               | 节点绑定的数据源         | varchar | 16   |        |      | 否   |
| api_repo_id               | API 仓库标识             | int     |      |        |      | 是   |
| code                      | 组件编码                 | varchar | 32   |        |      | 否   |

## 约束

* 主键：`PK_PAGE_FUNC_NODE_PORT`
* 外键：(*未设置*)`FK_PAGE_FUNC_NODE_PORT_ON_NODE_ID`，`page_func_node_id` 对应 `PAGE_FUNC_NODE` 表的 `dbid`
* 索引：无

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `port_type` 的值为：`sequence` 表示序列端口，`data` 表示数据端口
3. `flow_type` 的值为：`output` 表示输出型端口，`input` 表示输入型端口
4. `data_port_name` 和 `data_port_value_type` 的值不在本表存储，而是通过 `bind_source`、`api_repo_id` 和 `code` 推导
