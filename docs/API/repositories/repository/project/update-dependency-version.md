# 更新项目依赖的版本信息

```text
PUT /repos/{owner}/{repoName}/{projectName}/dependencies/{dependencyId}
```

## Parameters

| Name                     | Type     | Description                      |
| ------------------------ | -------- | -------------------------------- |
| `owner`                  | `string` | **Required**. 用户登录名         |
| `repoName`               | `string` | **Required**. 仓库名称           |
| `projectName`            | `string` | **Required**. 项目名称           |
| `dependencyId`           | `int`    | **Required**. 项目依赖标识       |
| `componentRepoVersionId` | `int`    | **Required**. 组件仓库的版本标识 |

## Response

如果仓库或项目不存在，则返回

```text
Status: 404 Not Found
```

如果登录用户对仓库没有写权限，则返回

```text
Status: 403 Forbidden
```

升级成功，则返回组件仓库的版本信息

```text
Status: 201 CREATED
```

返回修改后的依赖，一个 JSON 对象，其字段为：

| Name               | Type     | Description      |
| ------------------ | -------- | ---------------- |
| `id`               | `int`    | 发行版标识       |
| `componentRepoId`  | `int`    | 组件仓库标识     |
| `version`          | `string` | 组件库的版本号   |
| `apiRepoVersionId` | `int`    | API 库的版本标识 |
