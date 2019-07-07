# 获取登录用户发布的组件库

获取登录用户发布的组件库，从登记的发布任务中获取，如果任务发布失败，则允许用户重新发布。

```text
GET /user/component-repos
```

## Parameters

无

## Response

获取成功

```text
Status: 200 OK
```

返回一个 JSON 数组

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
