# 暂存更改

```text
POST /projects/{owner}/{projectName}/stage-changes
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |

```json
[
    "{fullKeyPath}"
]
```

支持暂存多个文件。

## Response

暂存成功，则返回

```text
Status: 200 OK
```

如果没有找到此项目或者用户没有访问权限，则

```text
Status: 404 Not Found
```