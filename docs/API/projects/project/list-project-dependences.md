# 获取项目的依赖列表

```text
GET /projects/{owner}/{projectName}/dependences
```

## Parameters

| Name              | Type     | Description              |
| ----------------- | -------- | ------------------------ |
| `owner`           | `string` | **Required**. 用户登录名 |
| `projectName`     | `string` | **Required**. 项目名称   |

## Response

如果项目不存在，则返回

```text
Status: 404 Not Found
```

如果登录用户对项目没有读权限，则返回

```text
Status: 403 Forbidden
```

获取成功，则返回

```text
Status: 200 OK
```

返回的结果

```json
[{
    "componentRepo": {},
    "componentRepoVersion": {},
    "apiRepo": {},
    "apiRepoVersion": {}
}]
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

componentRepoVersion 中的字段为：

| Name               | Type     | Description      |
| ------------------ | -------- | ---------------- |
| `id`               | `int`    | 发行版标识       |
| `componentRepoId`  | `int`    | 组件仓库标识     |
| `version`          | `string` | 组件库的版本号   |
| `apiRepoVersionId` | `int`    | API 库的版本标识 |

apiRepo 中的字段为：

| Name              | Type     | Description      |
| ----------------- | -------- | ---------------- |
| `id`              | `int`    | 发行版标识       |
| `gitRepoUrl`      | `string` | git 仓库地址     |
| `gitRepoWebsite`  | `string` | git 仓库网站     |
| `gitRepoOwner`    | `string` | git 仓库拥有者   |
| `gitRepoName`     | `string` | git 仓库名称     |
| `name`            | `string` | 组件库的名称     |
| `version`         | `string` | 组件库的版本号   |
| `label`           | `string` | 组件库的显示名   |
| `description`     | `string` | 组件库的详细说明 |
| `category`        | `string` | 组件库分类       |
| `lastPublishTime` | `string` | 最近发布时间     |

apiRepoVersion 中的字段为：

| Name        | Type     | Description        |
| ----------- | -------- | ------------------ |
| `id`        | `int`    | 发行版标识         |
| `apiRepoId` | `int`    | API 组件库标识     |
| `version`   | `string` | API 组件库的版本号 |
