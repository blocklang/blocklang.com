# 获取一个组件仓库的版本列表

获取组件仓库的版本列表，按照版本号从大到小排序。

```text
GET /component-repos/{componentRepoId}/versions
```

## Parameters

| Name              | Type  | Description  |
| ----------------- | ----- | ------------ |
| `componentRepoId` | `int` | 组件仓库标识 |

## Response

当组件库不存在时，返回

```text
Status: 404 Not Found
```

获取成功，则返回

```text
Status: 200 OK
```

返回一个 JSON 数组，其中的 JSON 对象字段为：

| Name              | Type     | Description    |
| ----------------- | -------- | -------------- |
| `id`              | `int`    | 发行版标识     |
| `componentRepoId` | `int`    | 组件仓库标识   |
| `version`         | `string` | 组件库的版本号 |
