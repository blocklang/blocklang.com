# `PAGE_FUNC_CONNECTION` - 函数节点连接线

有两种连接线：

1. 用于连接序列端口的连接线，用于标识函数的调用顺序；
2. 用于连接数据端口的连接线，用于标识数据传输关系，常用于连接变量与输入参数，函数的返回值与输入参数的传递关系等。

## 字段

| 字段名              | 注释                       | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | -------------------------- | ------- | ---- | ------ | ---- | ---- |
| dbid                | 主键                       | varchar | 32   |        | 是   | 否   |
| from_output_port_id | 起始节点中的输出型端口标识 | varchar | 32   |        |      | 否   |
| to_input_port_id    | 终止节点中的输入型端口标识 | varchar | 32   |        |      | 否   |

## 约束

* 主键：`PK_PAGE_FUNC_CONNECTION`
* 外键：(*未设置*)`FK_PAGE_FUNC_CONN_ON_FROM_ID`，`from_output_port_id` 对应 `PK_PAGE_FUNC_NODE_PORT` 表的 `dbid`；`FK_PAGE_FUNC_CONN_ON_TO_ID`，`to_input_port_id` 对应 `PK_PAGE_FUNC_NODE_PORT` 表的 `dbid`
* 索引：`UK_PAGE_FUNC_CONN_ON_FROM_TO`(唯一索引)，对应字段 `from_output_port_id`、`to_input_port_id`

## 说明

1. 注意，本表中不包含 4 个辅助字段
