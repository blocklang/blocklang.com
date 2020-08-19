# 获取最近提交信息

```text
GET /repos/{owner}/{repoName}/latest-commit/{parentId}
```

## Parameters

| Name       | Type     | Description                   |
| ---------- | -------- | ----------------------------- |
| `owner`    | `string` | **Required**. 用户登录名      |
| `repoName` | `string` | **Required**. 仓库名称        |
| `parentId` | `string` | 当前目录的标识，-1 表示根结点 |

## Response

如果无权访问此仓库，则返回

```text
Status: 404 Not Found
```

如果有权访问此仓库，则返回

```text
Status: 200 OK
```

| 属性名         | 类型      | 描述         |
| -------------- | --------- | ------------ |
| `id`           | `int`     | 记录标识     |
| `commitTime`   | `string`  | 提交时间     |
| `shortMessage` | `string`  | 概要信息     |
| `fullMessage`  | `boolean` | 详情         |
| `userName`     | `string`  | 用户名       |
| `avatarUrl`    | `string`  | 用户头像链接 |
