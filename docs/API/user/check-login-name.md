# 校验用户登录名

校验规则：

1. 登录名不能为空
2. 只能以字母或数字开头
3. 只能以字母或数字结尾
4. 只能包含字母、数字、下划线(_)或中划线(-)
5. 登录名已被其他用户占用
6. 登录名不能使用平台关键字

```text
POST /user/check-login-name
```

## Parameters

Form data

| Name        | Type     | Description              |
| ----------- | -------- | ------------------------ |
| `loginName` | `string` | **Required**. 用户登录名 |

## Response

校验未通过

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

校验通过

```text
Status: 200 OK
```

不返回任何内容。