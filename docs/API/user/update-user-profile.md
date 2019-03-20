# 修改用户信息

```text
PUT /user/profile
```

## Parameters

| Name         | Type     | Description            |
| ------------ | -------- | ---------------------- |
| `id`         | `number` | **Required**. 用户标识 |
| `nickname`   | `string` | 用户昵称               |
| `websiteUrl` | `string` | 个人主页               |
| `company`    | `string` | 公司名称               |
| `location`   | `string` | 所在地区               |
| `bio`        | `string` | 自我介绍               |

## Response

用户未登录

```text
Status: 403 Forbidden
```

用户已登录

```text
Status: 200 OK
```

界面上显示：“用户信息更新成功”。
