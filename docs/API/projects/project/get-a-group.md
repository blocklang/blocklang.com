# 查看项目的目录结构

逐层获取项目的结构。当前层级是分组，用于获取分组下的直属内容列表。

```text
GET /projects/{owner}/{projectName}/groups/{path}
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |
| `path`        | `string` | 分组的路径               |

## Response

```text
Status: 200 OK
```

返回一个 json 对象

```json
{
    "parentId": -1,
    "parentGroups": [],
    "resources": []
}
```

parentGroups 对象

| 属性名 | 类型     | 描述       |
| ------ | -------- | ---------- |
| `key`  | `string` | 资源 key   |
| `path` | `string` | 资源的路径 |

ProjectResource 对象

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `key`              | `string`   | 资源标识       |
| `name`             | `string`   | 资源名称       |
| `description`      | `string`   | 资源描述       |
| `resourceType`     | `string`   | 资源类型       |
| `parentId`         | `int`      | 父标识         |
| `seq`              | `int`      | 排序           |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |

如果没有找到此目录结构或者用户没有访问权限，则

```text
Status: 404 Not Found
```