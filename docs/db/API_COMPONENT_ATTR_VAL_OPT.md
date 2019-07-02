# `API_COMPONENT_ATTR_VAL_OPT` - 组件属性的可选值列表

## 字段

| 字段名                | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid                  | 主键               | int     |      |        | 是   | 否   |
| api_component_attr_id | API 组件的属性标识 | int     |      |        |      | 否   |
| code                  | 选项值编码         | char    | 4    |        |      | 否   |
| value                 | 选项值             | varchar | 32   |        |      | 否   |
| label                 | 选项值显示名       | varchar | 32   |        |      | 是   |
| description           | 选项值描述         | varchar | 512  |        |      | 是   |

## 约束

* 主键：`PK_API_COMPONENT_ATTR_VAL_OPT`
* 外键：(*未设置*)`FK_API_COMPONENT_ATTR_VAL_OPT`，`API_COMPONENT_ATTR_ID` 对应 `API_COMPONENT_ATTR` 表的 `dbid`
* 索引：`UK_API_COMP_ATTR_VAL_OPT_ON_ATTR_CODE`，对应字段 `api_component_attr_id`、`code`

## 说明

1. 不需要四个常规字段，取 `API_COMPONENT` 中的值
2. 不同版本的同一个部件的同一个属性的同一个可选值，则 `code` 的值必须相同
3. `CODE` 的值从 `0001` 开始，到 `9999` 结束
