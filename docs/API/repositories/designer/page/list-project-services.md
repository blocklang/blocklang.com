# 获取项目可用的 Service 列表

查找出项目依赖的 Service API 仓库中的 Service 列表。

这些 Service 对应 RESTful API，是已经部署完成，可直接通过 ajax 或 fetch 访问。

```text
GET /designer/projects/{projectId}/dependencies/services
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
        "serviceCategories": []
    }
]
```

字段为

| 属性名              | 类型     | 描述                       |
| ------------------- | -------- | -------------------------- |
| `apiRepoId`         | `int`    | API 仓库标识               |
| `apiRepoName`       | `number` | API 仓库名称               |
| `serviceCategories` | `Array`  | Service 按 Controller 分组 |

`serviceCategories` 是一个 Json Array，其中的字段为

| 属性名     | 类型     | 描述         |
| ---------- | -------- | ------------ |
| `name`     | `string` | 分类名       |
| `services` | `Array`  | Service 列表 |

`services` 是 Service 列表，是一个 Json Array，其中的字段为

| 属性名           | 类型      | 描述                  |
| ---------------- | --------- | --------------------- |
| `serviceId`      | `int`     | Service 标识          |
| `serviceCode`    | `string`  | Service 编码          |
| `serviceName`    | `string`  | Service 名称          |
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
