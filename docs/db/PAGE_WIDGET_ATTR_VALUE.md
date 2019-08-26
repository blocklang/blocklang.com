# `PAGE_WIDGET_ATTR_VALUE` - 页面部件的属性值

## 字段

| 字段名           | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ---------------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid             | 主键         | int     |      |        | 是   | 否   |
| page_widget_id   | 页面部件标识 | int     |      |        |      | 否   |
| widget_attr_code | 部件属性编码 | char    | 4    |        |      | 否   |
| attr_value       | 属性值       | text    |      |        |      | 否   |
| is_expr          | 是否表达式   | boolean |      | false  |      | 否   |

## 约束

* 主键：`PK_PAGE_WIDGET_ATTR_VALUE`
* 外键：(*未设置*)`FK_WIDGET_ATTR_VALUE_ON_UI_WIDGET_ID`，`page_widget_id` 对应 `PAGE_WIDGET` 表的 `dbid`
* 索引：`UK_WIDGET_ATTR_VALUE_ON_WIDGET_ID_ATTR_CODE`(唯一索引)，对应字段 `page_widget_id`、`widget_attr_code`

## 说明

1. 注意，本表中不包含 4 个辅助字段
2. `widget_attr_code` 对应 `API_COMPONENT_ATTR` 表中的 `code` 字段，因为本表只会设置 Widget 类型的组件属性，所以此字段以 `widget_` 开头，而不是以 `component_` 开头
3. 定义部件与部件属性之间的关系时并没有使用 `API_COMPONENT_ATTR` 表中的 `id`，而是使用 `code`，因为 `id` 是随着版本升级变的，`code` 是不随版本升级变化的
4. `attr_value` 目前支持两种值：一是纯文本；二是包含表达式的文本
5. `is_expr` 表示属性值是否表达式或者是否包含表达式，如果值为 `true`，则为部件设置该属性值之前需要先解析表达式
6. 如果 `attr_value` 的值为 null，则不用存储此记录，所以将 `attr_value` 设置为不可空
