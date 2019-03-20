# 获取登录用户简要信息

此 API 专门用于校验用户是否登录，如果用户已登录，则返回用户登录名和头像；否则返回 null。
此 API 只返回可公开的、页面常用的登录用户信息。

```text
GET /user
```

## Parameters

无

## Response

用户未登录

```text
Status: 200 OK
```

返回 `{}`

用户已登录

```text
Status: 200 OK
```

| Name        | Type     | Description                  |
| ----------- | -------- | ---------------------------- |
| `loginName` | `string` | 用户登录名                   |
| `avatarUrl` | `string` | 用户头像链接（尺寸为 small） |
