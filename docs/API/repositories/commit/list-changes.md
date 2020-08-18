# 获取有变动的资源

获取仓库中所有变化的资源，包括新增的、修改和删除的。

```text
GET /repos/{owner}/{repoName}/changes
```

## Parameters

| Name       | Type     | Description              |
| ---------- | -------- | ------------------------ |
| `owner`    | `string` | **Required**. 用户登录名 |
| `repoName` | `string` | **Required**. 仓库名称   |

## Response

```text
Status: 200 OK
```

返回一个 json 数组

对象

| 属性名           | 类型     | 描述                            |
| ---------------- | -------- | ------------------------------- |
| `fullKeyPath`    | `string` | 完整路径名，用于唯一定位资源    |
| `icon`           | `string` | 文件图标                        |
| `gitStatus`      | `string` | 资源状态                        |
| `resourceName`   | `string` | 资源名称                        |
| `parentNamePath` | `string` | 完整路径的显示名，使用 `/` 分割 |

如果没有找到此仓库，则

```text
Status: 404 Not Found
```

如果用户没有访问权限，则

```text
Status: 403 Forbidden
```