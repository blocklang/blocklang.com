# 创建页面

在创建页面之前，要对页面的 key 和 name 做校验。

只有 key 和 name 都校验之后才返回错误信息，而不是发现错误就返回。

```text
POST /projects/{owner}/{projectName}/pages/
```

## Parameters

| Name          | Type     | Description  |
| ------------- | -------- | ------------ |
| `key`         | `string` | 名称         |
| `name`        | `string` | 备注         |
| `type`        | `string` | 类型         |
| `description` | `string` | 说明         |
| `parentId`    | `number` | 所属分组标识 |

## Response

没有创建页面的权限

```text
Status: 403 Forbidden
```

校验未通过

```text
Status: 422 Unprocessable Entity
```

返回的数据

```json
{
    "errors": {
        "key": ["${filedErrorMessage}"],
        "name": ["${filedErrorMessage}"]
    }
}
```

key 的 `filedErrorMessage` 的值为：

1. 当 key 为空时返回 `名称不能为空`
2. 当 key 中包含非法字符时返回 `只允许字母、数字、中划线(-)、下划线(_)`
3. 当 key 已被占用时返回 `{分组名称}下已存在名称<strong>{key}</strong>`

name 的 `filedErrorMessage` 的值为：

1. 当 name 已被占用时返回 `{分组名称}下已存在备注<strong>{name}</strong>`

校验通过，且保存成功后

```text
Status: 201 CREATED
```

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `projectId`        | `number`   | 项目标识       |
| `key`              | `string`   | 页面名称       |
| `name`             | `string`   | 页面备注       |
| `description`      | `string`   | 页面说明       |
| `resourceType`     | `string`   | 资源类型       |
| `appType`          | `string`   | app 类型       |
| `parentId`         | `number`   | 分组标识       |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |