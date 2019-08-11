# 删除项目的一个依赖

```text
DELETE /projects/{owner}/{projectName}/dependences/{dependenceId}
```

## Parameters

| Name           | Type     | Description              |
| -------------- | -------- | ------------------------ |
| `owner`        | `string` | **Required**. 用户登录名 |
| `projectName`  | `string` | **Required**. 项目名称   |
| `dependenceId` | `int`    | 项目依赖标识             |

## Response

如果项目不存在，则返回

```text
Status: 404 Not Found
```

如果登录用户对项目没有写权限，则返回

```text
Status: 403 Forbidden
```

删除成功

```text
Status: 204 No Content
```
