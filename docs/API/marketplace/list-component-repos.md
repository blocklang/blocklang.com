# 获取组件仓库列表

获取组件仓库列表，按组件库的名称正序排列。一个组件仓库会登记一次，但会随着新版本的出现而发布多次。

如果仓库注册了，但是没有发布，则不返回此仓库。

注意：要排除标准库。

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
    "componentRepoVersion": {},
    "apiRepo": {},
    "apiRepoVersion": {}
}
```

componentRepo 中的字段为：

| Name                  | Type      | Description        |
| --------------------- | --------- | ------------------ |
| `id`                  | `int`     | 组件库标识         |
| `gitRepoUrl`          | `string`  | git 仓库地址       |
| `gitRepoWebsite`      | `string`  | git 仓库网站       |
| `gitRepoOwner`        | `string`  | git 仓库拥有者     |
| `gitRepoName`         | `string`  | git 仓库名称       |
| `category`            | `string`  | 组件库类型         |
| `repoType`            | `string`  | 仓库类型           |
| `lastPublishTime`     | `string`  | 最近发布时间       |
| `std`                 | `boolean` | 是否标准库         |
| `createUserName`      | `string`  | 创建用户的名称     |
| `createUserAvatarUrl` | `string`  | 创建用户的头像链接 |

componentRepoVersion 中的字段为：

| Name               | Type     | Description                    |
| ------------------ | -------- | ------------------------------ |
| `id`               | `int`    | 组件库版本标识                 |
| `componentRepoId`  | `int`    | 组件库标识                     |
| `version`          | `string` | 组件库的版本号                 |
| `gitTagName`       | `string` | tag 全称                       |
| `apiRepoVersionId` | `string` | API 库的版本标识               |
| `name`             | `string` | 组件库的名称                   |
| `displayName`      | `string` | 组件库的显示名                 |
| `description`      | `string` | 组件库的详细说明               |
| `logoPath`         | `string` | 项目 Logo 访问路径             |
| `language`         | `string` | 组件库使用的编程语言           |
| `build`            | `string` | 组件库的构建工具               |
| `appType`          | `string` | 组件库适用的 app 类型          |
| `icon`             | `string` | 组件库的图标                   |
| `title`            | `string` | 鼠标悬停在 icon 上时显示的内容 |

apiRepo 中的字段为：

repoType 的值只能是 `API`

| Name              | Type      | Description    |
| ----------------- | --------- | -------------- |
| `id`              | `int`     | API 库标识     |
| `gitRepoUrl`      | `string`  | git 仓库地址   |
| `gitRepoWebsite`  | `string`  | git 仓库网站   |
| `gitRepoOwner`    | `string`  | git 仓库拥有者 |
| `gitRepoName`     | `string`  | git 仓库名称   |
| `category`        | `string`  | 组件库分类     |
| `lastPublishTime` | `string`  | 最近发布时间   |
| `std`             | `boolean` | 是否标准库     |

apiRepoVersion 中的字段为：

| Name          | Type     | Description      |
| ------------- | -------- | ---------------- |
| `id`          | `int`    | API 库版本标识   |
| `apiRepoId`   | `int`    | API 库标识       |
| `version`     | `string` | API 库的版本号   |
| `gitTagName`  | `string` | tag 全称         |
| `name`        | `string` | API 库的名称     |
| `displayName` | `string` | API 库的显示名   |
| `description` | `string` | API 库的详细说明 |