# 查看项目的目录结构

逐层获取项目的结构。当前层级是分组时：

1. 获取当前分组的 id
2. 获取当前分组下的直属内容列表；
3. 获取当前分组的父分组信息（叶节点是当前分组）

```text
GET /projects/{owner}/{projectName}/groups/{groupPath}
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |
| `groupPath`   | `string` | 分组的路径               |

## Response

```text
Status: 200 OK
```

返回一个 json 对象

```json
{
    "id": -1,
    "parentGroups": [],
    "childResources": []
}
```

parentGroups 对象（最后一个元素是当前分组），如果是根节点则返回空数组。

| 属性名 | 类型     | 描述       |
| ------ | -------- | ---------- |
| `name` | `string` | 资源名     |
| `path` | `string` | 资源的路径 |

childResources 是数组，数据项为 ProjectResource 对象

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
