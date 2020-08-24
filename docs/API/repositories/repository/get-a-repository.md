# 查看一个仓库

查看指定仓库的基本信息。

```text
GET /repos/{owner}/{repoName}
```

## Parameters

| Name       | Type     | Description              |
| ---------- | -------- | ------------------------ |
| `owner`    | `string` | **Required**. 用户登录名 |
| `repoName` | `string` | **Required**. 仓库名称   |

## Response

如果仓库不存在，则返回

```text
Status: 404 Not Found
```

如果仓库存在，则返回

```text
Status: 200 OK
```

| 属性名             | 类型       | 描述                 |
| ------------------ | ---------- | -------------------- |
| `id`               | `int`      | 记录标识             |
| `name`             | `string`   | 仓库名称             |
| `description`      | `string`   | 仓库描述             |
| `isPublic`         | `boolean`  | 是否公开             |
| `avatarUrl`        | `string`   | 仓库logo             |
| `lastActiveTime`   | `datetime` | 最近活动时间         |
| `createUserName`   | `string`   | 创建用户名           |
| `accessLevel`      | `string`   | 访问权限信息         |
| `createTime`       | `datetime` | 创建时间             |
| `createUserId`     | `int`      | 创建用户标识         |
| `lastUpdateTime`   | `datetime` | 最近修改时间         |
| `lastUpdateUserId` | `int`      | 最近修改人标识       |
| `accessLevel`      | `string`   | 用户对仓库的访问权限 |
