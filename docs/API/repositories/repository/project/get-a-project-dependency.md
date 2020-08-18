# 查看项目依赖资源的基本信息

FIXME:

1. 是不是应该返回 `projectResource` 和 `parentGroups` 就可以了？
2. `dependence` 和另一个 API 中的 `dependences` 没有办法明显区分开来，改为 `resource`?

获取项目依赖

```text
GET /repos/{owner}/{repoName}/{projectName}/dependency
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `repoName`    | `string` | **Required**. 仓库名称   |
| `projectName` | `string` | **Required**. 项目名称   |

## Response

如果没有找到仓库、项目或者项目依赖，则

```text
Status: 404 Not Found
```

如果用户没有访问权限，则

```text
Status: 403 Forbidden
```

否则返回

```text
Status: 200 OK
```

返回一个 json 对象

```json
{
    "resourceId": 1,
    "pathes": []
}
```

因为项目依赖也是项目资源的一种，所以 `resourceId` 就是项目依赖对应的资源标识。

`pathes` 是一个数组，因为项目依赖通常放在根目录下，所以其中只包含项目依赖一个元素

| 属性名 | 类型     | 描述       |
| ------ | -------- | ---------- |
| `name` | `string` | 资源名     |
| `path` | `string` | 资源的路径 |
