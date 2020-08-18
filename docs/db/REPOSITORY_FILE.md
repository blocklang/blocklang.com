# `REPOSITORY_FILE` - 仓库文件

当资源类型为`文件`时，将文件的内容存在这里，本表只支持存储文本文件，当前没有存储二进制文件的需求。

## 字段

| 字段名                 | 注释         | 类型 | 长度 | 默认值 | 主键 | 可空 |
| ---------------------- | ------------ | ---- | ---- | ------ | ---- | ---- |
| dbid                   | 主键         | int  |      |        | 是   | 否   |
| repository_resource_id | 仓库资源标识 | int  |      |        |      | 否   |
| file_type              | 文件类型     | char | 2    |        |      | 否   |
| content                | 内容         | text |      |        |      | 是   |

## 约束

* 主键：`PK_REPOSITORY_FILE`
* 外键：(*未设置*)`FK_REPOSITORY_FILE_RESOURCE`，`REPOSITORY_RESOURCE_ID` 对应 `REPOSITORY_RESOURCE` 表的 `dbid`
* 索引：`IDX_REPO_FILE_RESOURCE_ID`，对应字段 `repository_resource_id`

## 说明

1. `file_type` 的值为：`01` 表示 `markdown`
2. 只支持一个项目资源标识对应一个文件
3. 注意，本表中不包含 4 个辅助字段
