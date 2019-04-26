# 校验页面的备注

备注，也称为显示名。

校验规则：

1. 备注是否被占用

```text
POST /projects/{owner}/{projectName}/pages/check-name
```

## Parameters

Form Data

| Name      | Type     | Description        |
| --------- | -------- | ------------------ |
| `name`    | `string` | **Required**. 备注 |
| `groupId` | `number` | 所属分组标识       |

## Response

没有创建页面的权限

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
        "name": ["${filedErrorMessage}"]
    }
}
```

`filedErrorMessage` 的值为：

1. 当 name 已被占用时返回 `{分组名称}下已存在备注<strong>{name}</strong>`

校验通过

```text
Status: 200 OK
```

不返回任何内容。