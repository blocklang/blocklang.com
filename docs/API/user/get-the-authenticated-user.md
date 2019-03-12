# 获取登录用户信息

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
