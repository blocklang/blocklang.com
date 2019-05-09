# 提交更改

```text
POST /projects/{owner}/{projectName}/commits
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |

## Response

提交成功，则返回

```text
Status: 200 OK
```

如果没有找到此项目，则

```text
Status: 404 Not Found
```

如果用户没有访问权限，则

```text
Status: 403 Forbidden
```

如果没有变更的文件，则

```text
Status: 422 Unprocessable Entity
```

并返回提示信息

```json
{
    "errors": {
        "globalErrors": ["没有发现变更的文件"]
    }
}
```