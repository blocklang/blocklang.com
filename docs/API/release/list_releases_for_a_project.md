# 获取项目发布列表

获取项目发布列表，按发布时间倒排序。

```text
GET /projects/{owner}/{projectName}/releases
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

返回一个 JSON 数组，其中的 JSON 对象字段为：

| Name            | Type     | Description            |
| --------------- | -------- | ---------------------- |
| `id`            | `int`    | 发行版标识             |
| `projectId`     | `int`    | 项目标识               |
| `version`       | `string` | 语义化版本，如 v0.1.0  |
| `title`         | `string` | 发行版标题             |
| `description`   | `string` | 发行版描述             |
| `jdkName`       | `string` | JDK 名称，包括版本信息 |
| `startTime`     | `string` | 发布开始时间           |
| `endTime`       | `string` | 发布开始时间           |
| `releaseResult` | `string` | 发布结果               |
