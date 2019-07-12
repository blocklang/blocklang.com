# 获取一个组件库发布任务

获取登录用户发布的组件库，从登记的发布任务中获取，如果任务发布失败，则允许用户重新发布。

```text
GET /marketplace/publish/{taskId}
```

## Parameters

| Name     | Type  | Description |
| -------- | ----- | ----------- |
| `taskId` | `int` | 任务标识    |

## Response

获取成功

```text
Status: 200 OK
```

返回一个 JSON 数组

| Name             | Type     | Description                    |
| ---------------- | -------- | ------------------------------ |
| `id`             | `int`    | 发行版标识                     |
| `gitUrl`         | `string` | git 仓库地址                   |
| `seq`            | `int`    | 一个用户对一个组件库的发布序列 |
| `website`        | `string` | git 仓库网站                   |
| `owner`          | `string` | git 仓库拥有者                 |
| `repoName`       | `string` | git 仓库名称                   |
| `startTime`      | `string` | 开始时间                       |
| `publishType`    | `string` | 发布类型                       |
| `publishResult`  | `string` | 发布结果                       |
| `fromVersion`    | `string` | 升级前版本号                   |
| `toVersion`      | `string` | 升级后版本号                   |
| `createUserId`   | `string` | 创建者的标识                   |
| `createUserName` | `string` | 创建者的用户名                 |
