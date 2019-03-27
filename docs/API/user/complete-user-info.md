# 完善用户信息

```text
PUT /user/complete-user-info
```

## Parameters

Form data

| Name        | Type     | Description              |
| ----------- | -------- | ------------------------ |
| `loginName` | `string` | **Required**. 用户登录名 |

## Response

未获取到第三方用户信息

```text
Status: 403 Forbidden
```

服务器端校验未通过

```text
Status: 422 Unprocessable Entity
```

```json
{
    "errors": {
        "loginName": ["${filedErrorMessage}"]
    }
}
```

`filedErrorMessage` 的值为：

1. 登录名能为空时返回 `登录名不能为空`
2. 不是以字母或数字开头时返回 `只能以字母或数字开头`
3. 不是以字母或数字结尾时返回 `只能以字母或数字结尾`
4. 不是字母、数字、下划线(_)或中划线(-)时返回 `只能包含字母、数字、下划线(_)或中划线(-)`
5. 登录名已被其他用户占用时返回 `登录名已被占用`
6. 登录名已被平台关键字占用时返回 `登录名已被占用`

校验通过且保存成功

```text
Status: 200 OK
```

| Name        | Type     | Description                  |
| ----------- | -------- | ---------------------------- |
| `loginName` | `string` | 用户登录名                   |
| `avatarUrl` | `string` | 用户头像链接（尺寸为 small） |