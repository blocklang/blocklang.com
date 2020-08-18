# 撤销暂存的更改

```text
POST /repos/{owner}/{repoName}/unstage-changes
```

## Parameters

| Name       | Type     | Description              |
| ---------- | -------- | ------------------------ |
| `owner`    | `string` | **Required**. 用户登录名 |
| `repoName` | `string` | **Required**. 仓库名称   |

```json
[
    "{fullKeyPath}"
]
```

支持撤销多个文件。

## Response

暂存成功，则返回

```text
Status: 200 OK
```

如果没有找到此仓库，则

```text
Status: 404 Not Found
```

如果用户没有访问权限，则

```text
Status: 403 Forbidden
```