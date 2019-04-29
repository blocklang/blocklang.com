# 校验分组的 key

校验规则：

1. key 不能为空
2. 是否由有效字符组成
3. key 是否被占用

```text
POST /projects/{owner}/{projectName}/groups/check-key
```

## Parameters

Form Data

| Name      | Type     | Description        |
| --------- | -------- | ------------------ |
| `key`     | `string` | **Required**. 名称 |
| `parentId` | `number` | 所属分组标识       |

## Response

没有创建分组的权限

```text
Status: 403 Forbidden
```

校验未通过

```text
Status: 422 Unprocessable Entity
```

```json
{
    "errors": {
        "key": ["${filedErrorMessage}"]
    }
}
```

`filedErrorMessage` 的值为：

1. 当 key 为空时返回 `名称不能为空`
2. 当 key 中包含非法字符时返回 `只允许字母、数字、中划线(-)、下划线(_)`
3. 当 key 已被占用时返回 `{分组名称}下已存在名称<strong>{key}</strong>`

校验通过

```text
Status: 200 OK
```

不返回任何内容。