# 为项目添加一个依赖

需校验依赖是否已添加。需要找到组件仓库的最新版本号，然后保存项目标识和组件仓库最新版本标识。

```text
POST /projects/{owner}/{projectName}/dependences
```

## Parameters

| Name              | Type     | Description              |
| ----------------- | -------- | ------------------------ |
| `owner`           | `string` | **Required**. 用户登录名 |
| `projectName`     | `string` | **Required**. 项目名称   |
| `componentRepoId` | `int`    | 组件仓库标识             |

## Response

如果项目不存在，则返回

```text
Status: 404 Not Found
```

如果登录用户对项目没有写权限，则返回

```text
Status: 403 Forbidden
```

如果组件库已添加，则返回

```text
Status: 422 Unprocessable Entity
```

返回的数据

```json
{
    "errors": {
        "componentRepoId": ["项目已依赖该组件仓库"]
    }
}
```

校验通过，且保存成功后

```text
Status: 201 CREATED
```

返回的结果

```json
{
    "dependence": {},
    "componentRepo": {},
    "componentRepoVersion": {},
    "apiRepo": {},
    "apiRepoVersion": {}
}
```

`dependence` 中的字段为：

| Name                     | Type     | Description             |
| ------------------------ | -------- | ----------------------- |
| `id`                     | `int`    | 发行版标识              |
| `projectId`              | `string` | 项目标识                |
| `componentRepoVersionId` | `string` | 组件仓库的版本标识      |
| `profileId`              | `string` | 项目构建的 Profile 标识 |

`componentRepo` 中的字段为：

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

`componentRepoVersion` 中的字段为：

| Name               | Type     | Description      |
| ------------------ | -------- | ---------------- |
| `id`               | `int`    | 发行版标识       |
| `componentRepoId`  | `int`    | 组件仓库标识     |
| `version`          | `string` | 组件库的版本号   |
| `apiRepoVersionId` | `int`    | API 库的版本标识 |

`apiRepo` 中的字段为：

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

`apiRepoVersion` 中的字段为：

| Name        | Type     | Description        |
| ----------- | -------- | ------------------ |
| `id`        | `int`    | 发行版标识         |
| `apiRepoId` | `int`    | API 组件库标识     |
| `version`   | `string` | API 组件库的版本号 |
