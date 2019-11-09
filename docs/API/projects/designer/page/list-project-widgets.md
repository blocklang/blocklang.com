# 获取项目可用的 Widget 列表

查找出项目依赖的 Widget 组件仓库中的 Widget 列表。

```text
GET /designer/projects/{projectId}/dependences/widgets
```

## Parameters

| Name        | Type  | Description            |
| ----------- | ----- | ---------------------- |
| `projectId` | `int` | **Required**. 项目标识 |

## Response

项目不存在

```text
Status: 404 Not Found
```

对项目没有读权限

```text
Status: 403 Forbidden
```

否则返回

```text
Status: 200 OK
```

返回的数据是一个 json 数组

```json
[
    {
        "apiRepoId": "",
        "apiRepoName": "",
        "widgetCategories": []
    }
]
```

字段为

| 属性名             | 类型     | 描述           |
| ------------------ | -------- | -------------- |
| `apiRepoId`        | `int`    | API 仓库标识   |
| `apiRepoName`      | `number` | API 仓库名称   |
| `widgetCategories` | `Array`  | 部件按类型分组 |

`widgetCategories` 是一个 Json Array，其中的字段为

| 属性名    | 类型     | 描述     |
| --------- | -------- | -------- |
| `name`    | `string` | 分类名   |
| `widgets` | `Array`  | 部件列表 |

`widgets` 是部件列表，是一个 Json Array，其中的字段为

| 属性名           | 类型      | 描述                  |
| ---------------- | --------- | --------------------- |
| `widgetId`       | `int`     | 部件标识              |
| `widgetCode`     | `string`  | 部件编码              |
| `widgetName`     | `string`  | 部件名称              |
| `iconClass`      | `string`  | 部件图标样式类        |
| `canHasChildren` | `boolean` | 是否可以包含子部件    |
| `apiRepoId`      | `number`  | 部件所属的 API 库标识 |
| `properties`     | `Array`   | 部件的属性列表        |

`properties` 是部件的属性列表，是一个 Json Array，其中的字段为

| 属性名         | 类型     | 描述           |
| -------------- | -------- | -------------- |
| `code`         | `int`    | 属性编码       |
| `name`         | `string` | 属性名         |
| `defaultValue` | `string` | 属性默认值     |
| `valueType`    | `string` | 属性值数据类型 |
