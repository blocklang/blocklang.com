# 文档模板

编写表结构文档的常用模板。

1. 表的主键，统一使用代理主键，并命名为 `DBID`
2. 根据业务唯一性，为表增加唯一约束
3. 在数据库表层面，即存储时，如果一个字段的值可以编码，则约定必须使用编码；但在数据服务中，即使用时，可直接使用编码描述字段

## 表结构

```text
# 表名 `XX_XX` - 表注释

## 字段

| 字段名 | 注释 | 类型 | 长度 | 默认值 | 主键 | 可空 |
| ------ | ---- | ---- | ---- | ------ | ---- | ---- |
|        |      |      |      |        | 是   | 否   |

## 约束

* 主键：`PK_`
* 外键：`FK-`
* 索引：`UK_`

## 说明

1. 说明特殊逻辑
2. 罗列编码字段的值
```

## 辅助字段

所有数据库表，都要增加以下4个辅助字段

模型

```text
| 字段名              | 注释           | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ------------------- | -------------- | -------- | ---- | ------ | ---- | ---- |
| create_user_id      | 创建人标识     | int      |      |        |      | 否   |
| create_time         | 创建时间       | datetime |      |        |      | 否   |
| last_update_user_id | 最近修改人标识 | int      |      |        |      | 是   |
| last_update_time    | 最近修改时间   | datetime |      |        |      | 是   |
```

Changelog

```xml
<column name="create_user_id" remarks="创建人标识" type="int">
    <constraints nullable="false" />
</column>
<column name="create_time" remarks="创建时间" type="${datetime}">
    <constraints nullable="false" />
</column>
<column name="last_update_user_id" remarks="最近修改人标识" type="int" />
<column name="last_update_time" remarks="最近修改时间" type="${datetime}" />
```