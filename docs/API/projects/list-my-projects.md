# 查看我的项目

查看我（即登录用户）有权访问的项目，包括我创建的项目，我参与的项目等。

```text
GET /user/projects
```

## Parameters

无

## Response

如果用户登录，则返回

```text
Status: 200 OK
```

一个数组

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `name`             | `string`   | 项目名称       |
| `description`      | `string`   | 项目描述       |
| `isPublic`         | `boolean`  | 是否公开       |
| `lastActiveTime`   | `datetime` | 最近活动时间   |
| `createUserName`   | `string`   | 创建用户名     |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |

如果用户未登录，则返回

```text
Status: 403 Forbidden
```