# 获取组件仓库列表

获取组件仓库列表，按组件库的名称正序排列。一个组件仓库会登记一次，但会随着新版本的出现而发布多次。

如果仓库注册了，但是没有发布，则不返回此仓库。

```text
GET /component-repos?q={query}&page={page}
```

## Parameters

| Name    | Type     | Description                |
| ------- | -------- | -------------------------- |
| `query` | `string` | 与组件库的名称或显示名匹配 |
| `page`  | `int`    | 当前页码                   |

## Response

获取成功

```text
Status: 200 OK
```

返回一个 JSON 数组，其中包含分页信息

```json
{
    "totalPages": 0,
    "number": 0,
    "size": 10,
    "first": true,
    "last": true,
    "content": []
}
```

`content` 数组中的 JSON 对象字段为：

```json
{
    "componentRepo": {},
    "apiRepo": {}
}
```

componentRepo 中的字段为：

| Name              | Type     | Description        |
| ----------------- | -------- | ------------------ |
| `id`              | `int`    | 发行版标识         |
| `gitRepoUrl`      | `string` | git 仓库地址       |
| `gitRepoWebsite`  | `string` | git 仓库网站       |
| `gitRepoOwner`    | `string` | git 仓库拥有者     |
| `gitRepoName`     | `string` | git 仓库名称       |
| `name`            | `string` | 组件库的名称       |
| `version`         | `string` | 组件库的版本号     |
| `label`           | `string` | 组件库的显示名     |
| `description`     | `string` | 组件库的详细说明   |
| `logoPath`        | `string` | 项目 Logo 访问路径 |
| `category`        | `string` | 组件库分类         |
| `lastPublishTime` | `string` | 最近发布时间       |

apiRepo 中的字段为：

| Name              | Type     | Description        |
| ----------------- | -------- | ------------------ |
| `id`              | `int`    | 发行版标识         |
| `gitRepoUrl`      | `string` | git 仓库地址       |
| `gitRepoWebsite`  | `string` | git 仓库网站       |
| `gitRepoOwner`    | `string` | git 仓库拥有者     |
| `gitRepoName`     | `string` | git 仓库名称       |
| `name`            | `string` | 组件库的名称       |
| `version`         | `string` | 组件库的版本号     |
| `label`           | `string` | 组件库的显示名     |
| `description`     | `string` | 组件库的详细说明   |
| `category`        | `string` | 组件库分类         |
| `lastPublishTime` | `string` | 最近发布时间       |
