# 获取登录用户详细信息

此 API 专门用于登录用户编辑个人资料，所以包含的信息较多。

```text
GET /user/profile
```

## Parameters

无

## Response

用户未登录

```text
Status: 403 Forbidden
```

用户已登录

```text
Status: 200 OK
```

| Name         | Type     | Description |
| ------------ | -------- | ----------- |
| `id`         | `number` | 用户标识    |
| `login_name` | `string` | 用户登录名  |
| `nickname`   | `string` | 用户昵称    |
| `email`      | `string` | 用户邮箱    |
| `websiteUrl` | `string` | 个人主页    |
| `company`    | `string` | 公司名称    |
| `location`   | `string` | 所在地区    |
| `bio`        | `string` | 自我介绍    |
