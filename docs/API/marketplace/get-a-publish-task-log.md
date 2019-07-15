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

如果用户未登录，或者登录用户不是任务的发布者，则返回

```text
Status: 403 Forbidden
```

如果发布任务不存在；或者发布任务存在，但发布日志不存在，则返回

```text
Status: 404 Not Found
```
