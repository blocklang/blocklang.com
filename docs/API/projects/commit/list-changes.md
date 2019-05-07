# 获取有变动的资源

获取项目中所有变化的资源，包括新增的、修改和删除的。

```text
GET /projects/{owner}/{projectName}/changes
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

返回一个 json 数组

对象

| 属性名             | 类型     | 描述                         |
| ------------------ | -------- | ---------------------------- |
| `fullKeyPath`      | `string` | 完整路径名，用于唯一定位资源 |
| `resourceType`     | `string` | 资源类型                     |
| `resourceTypeIcon` | `string` | 资源类型的图标               |
| `gitStatus`        | `string` | 资源状态                     |
| `resourceId`       | `number` | 资源标识                     |
| `resourceName`     | `string` | 资源名称                     |
| `parentNamePath`   | `string` | 完整路径的显示名             |

如果没有找到此项目或者用户没有访问权限，则

```text
Status: 404 Not Found
```