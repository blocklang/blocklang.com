# 获取 JDK 清单

获取项目发布列表，按发布时间倒排序。约定在 `APP` 表中存的 JDK 的名称为 **jdk**。

```text
GET /apps/jdk/releases
```

注意：此处的 jdk 是 app name。

## Parameters

无

## Response

获取成功

```text
Status: 200 OK
```

返回一个 JSON 数组，其中的 JSON 对象字段为：

| Name            | Type     | Description           |
| --------------- | -------- | --------------------- |
| `id`            | `int`    | 发行版标识            |
| `appId`         | `int`    | APP 标识              |
| `version`       | `string` | 语义化版本，如 v0.1.0 |
| `title`         | `string` | 发行版标题            |
| `description`   | `string` | 发行版描述            |
| `releaseTime`   | `string` | 发布时间              |
| `releaseResult` | `string` | 发布结果              |
