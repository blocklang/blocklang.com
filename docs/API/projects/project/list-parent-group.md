# 获取资源的父分组列表

获取当前资源的所有父资源列表

```text
GET /projects/{owner}/{projectName}/group-path/{path}
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
    "id": -1,
    "parentGroups": []
}
```

parentGroups 对象（最后一个元素是当前分组）

| 属性名 | 类型     | 描述       |
| ------ | -------- | ---------- |
| `name` | `string` | 资源名     |
| `path` | `string` | 资源的路径 |

如果没有找到此目录结构或者用户没有访问权限，则

```text
Status: 404 Not Found
```
