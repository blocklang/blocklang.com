# 查看一个项目

查看指定的项目。

```text
GET /projects/{owner}/{projectName}
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |

## Response

如果项目存在，则返回

```text
Status: 200 OK
```

| 属性名             | 类型       | 描述                 |
| ------------------ | ---------- | -------------------- |
| `id`               | `int`      | 记录标识             |
| `name`             | `string`   | 项目名称             |
| `description`      | `string`   | 项目描述             |
| `isPublic`         | `boolean`  | 是否公开             |
| `lastActiveTime`   | `datetime` | 最近活动时间         |
| `createUserName`   | `string`   | 创建用户名           |
| `createTime`       | `datetime` | 创建时间             |
| `createUserId`     | `int`      | 创建用户标识         |
| `lastUpdateTime`   | `datetime` | 最近修改时间         |
| `lastUpdateUserId` | `int`      | 最近修改人标识       |
| `accessLevel`      | `string`   | 用户对项目的访问权限 |

如果项目不存在，则返回

```text
Status: 404 Not Found
```