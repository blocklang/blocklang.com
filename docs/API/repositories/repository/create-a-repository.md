# 创建仓库

在创建仓库方法中，也要在保存数据之前，对仓库名称进行校验。

```text
POST /repos
```

## Parameters

| Name          | Type      | Description              |
| ------------- | --------- | ------------------------ |
| `owner`       | `string`  | **Required**. 用户登录名 |
| `name`        | `string`  | **Required**. 仓库名称   |
| `description` | `string`  | 项目描述                 |
| `isPublic`    | `boolean` | 是否公开                 |

## Response

校验未通过

```text
Status: 422 Unprocessable Entity
```

返回的数据

```json
{
    "errors": {
        "value": ["${filedErrorMessage}"]
    }
}
```

`filedErrorMessage` 的值为：

1. 当仓库名称为空时返回 `仓库名称不能为空`
2. 当仓库名称中存在非法字符时返回 `只允许字母、数字、中划线(-)、下划线(_)、点(.)`
3. 登录用户下是否已存在该仓库名时返回 `{owner}下已存在<strong>{name}</strong>仓库`

校验通过，且保存成功后

```text
Status: 201 CREATED
```

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `name`             | `string`   | 仓库名称       |
| `description`      | `string`   | 仓库描述       |
| `isPublic`         | `boolean`  | 是否公开       |
| `lastActiveTime`   | `datetime` | 最近活动时间   |
| `createUserName`   | `string`   | 创建用户名     |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |