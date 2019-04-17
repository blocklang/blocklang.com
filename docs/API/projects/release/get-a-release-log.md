# 获取软件的一个发行版的发布日志

用户看到的日志分为历史日志和实时日志，此 API 只用来获取历史日志，实时日志是通过 websocket 获取的。

```text
GET /projects/{owner}/{projectName}/releases/{version}/log
```

## Parameters

| Name          | Type     | Description                                           |
| ------------- | -------- | ----------------------------------------------------- |
| `owner`       | `string` | **Required**. 用户登录名                              |
| `projectName` | `string` | **Required**. 项目名称                                |
| `version`     | `string` | 语义化版本，如 0.1.0                                  |

## Response

如果发布日志存在，则返回

```text
Status: 200 OK
```

结果是一个数组，如：

```json
[
    'line content 1',
    'line content 2'
]
```

如果发布日志不存在，则返回

```text
Status: 404 Not Found
```