# 校验发布版本号

校验规则：

1. 版本号不能为空
2. 是有效的语义化版本号
3. 版本号没有被占用
4. 要大于最近的版本号

为了帮助用户输入，提示信息中要包含最近的版本号。

```text
POST /projects/{owner}/{projectName}/releases/check-version?version={version}
```

## Parameters

Query

| Name    | Type     | Description              |
| ------- | -------- | ------------------------ |
| `version`  | `string` | **Required**. 版本号 |

## Response

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

1. 当版本号为空时返回 `版本号不能为空`
2. 当版本号不是有效的语义化版本时返回 `请使用[语义化版本]()`
3. 当版本号已被占用时返回 `版本号<strong>{name}</strong>已被使用，最新版本号为<strong>{name}</strong>`
4. 当版本号不大于最近的版本号时返回 `要大于最新的版本号<strong>{name}</strong>`

校验通过

```text
Status: 200 OK
```

不返回任何内容。