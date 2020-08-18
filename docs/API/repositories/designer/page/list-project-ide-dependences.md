# 获取项目的 IDE 依赖列表

一个仓库中可存放多个项目，每个项目都有各自的依赖配置文件。

在设计器中使用开发专用的 IDE 依赖，这里的依赖不能是 API 仓库，必须是组件仓库（实现）。

注意：要包含标准库。

```text
GET /designer/projects/{projectId}/dependences?repo=ide
```

## Parameters

| Name        | Type  | Description            |
| ----------- | ----- | ---------------------- |
| `projectId` | `int` | **Required**. 项目标识 |

`projectId` 不是仓库标识，而是仓库中的项目标识，是位于仓库根目录下，资源类型为 `项目` 的分组标识。

## Response

项目不存在

```text
Status: 404 Not Found
```

对项目没有读权限

```text
Status: 403 Forbidden
```

否则返回

```text
Status: 200 OK
```

返回的数据是一个 json 数组，其中的字段为：

```json
[{
    "id": 1,
    "gitRepoWebsite": "",
    "gitRepoOwner": "",
    "gitRepoName": "",
    "apiRepoId": 2,
    "name": "",
    "category": "",
    "version": "",
    "std": false
}]
```

| Name             | Type      | Description                   |
| ---------------- | --------- | ----------------------------- |
| `id`             | `number`  | git 仓库标识                  |
| `gitRepoWebsite` | `string`  | git 仓库网站                  |
| `gitRepoOwner`   | `string`  | git 仓库拥有者                |
| `gitRepoName`    | `string`  | git 仓库名称                  |
| `apiRepoId`      | `number`  | 该组件仓库实现的 API 仓库标识 |
| `name`           | `string`  | 组件库的名称                  |
| `category`       | `string`  | 组件库分类                    |
| `version`        | `string`  | 项目依赖的版本号              |
| `std`            | `boolean` | 是否标准库                    |
