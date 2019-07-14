# 获取一个组件库发布任务的日志

用户看到的日志分为历史日志和实时日志，此 API 只用来获取历史日志，实时日志是通过 websocket 获取的。

```text
GET /marketplace/publish/{taskId}/log
```

## Parameters

| Name     | Type  | Description |
| -------- | ----- | ----------- |
| `taskId` | `int` | 任务标识    |

## Response

如果发布日志存在，则返回

```text
Status: 200 OK
```

结果是一个数组，如：

```json
[
    "line content 1",
    "line content 2"
]
```

如果发布日志不存在，则返回

```text
Status: 404 Not Found
```
