# 查看项目依赖

获取项目依赖

```text
GET /projects/{owner}/{projectName}/dependence
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |

## Response

```text
Status: 200 OK
```

返回一个 json 对象

```json
{
    "resourceId": 1,
    "pathes": [],
    "dependences": []
}
```

parentGroups 对象

| 属性名 | 类型     | 描述       |
| ------ | -------- | ---------- |
| `name` | `string` | 资源名     |
| `path` | `string` | 资源的路径 |

ProjectDependence 对象

| 属性名 | 类型  | 描述     |
| ------ | ----- | -------- |
| `id`   | `int` | 记录标识 |

TODO: 添加项目依赖定义

如果没有找到项目，或者没有找到项目依赖，则

```text
Status: 404 Not Found
```

如果用户没有访问权限，则

```text
Status: 403 Forbidden
```
