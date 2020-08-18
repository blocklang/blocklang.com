# 获取项目的发布数

目前统计的是发布任务数。

所有统计 API 的 URL 中必须使用 `stats`

```text
GET /projects/{owner}/{projectName}/stats/releases
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |

## Response

当没有找到项目时

```text
Status: 404 Not Found
```

获取成功

```text
Status: 200 OK
```

| Name    | Type  | Description |
| ------- | ----- | ----------- |
| `total` | `int` | 发行版个数  |
