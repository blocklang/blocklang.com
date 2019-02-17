# 项目基本信息

- [项目基本信息](#%E9%A1%B9%E7%9B%AE%E5%9F%BA%E6%9C%AC%E4%BF%A1%E6%81%AF)
  - [校验项目名称](#%E6%A0%A1%E9%AA%8C%E9%A1%B9%E7%9B%AE%E5%90%8D%E7%A7%B0)
    - [Parameters](#parameters)
    - [Response](#response)
  - [创建项目](#%E5%88%9B%E5%BB%BA%E9%A1%B9%E7%9B%AE)
    - [Parameters](#parameters-1)
    - [Response](#response-1)

## 校验项目名称

校验规则：

1. 项目名称不能为空
2. 项目名称只支持英文字母、数字、中划线(-)、下划线(_)、点(.)等字符
3. 登录用户下是否已存在该项目名

```text
POST /projects/check-name
```

### Parameters

| Name    | Type     | Description              |
| ------- | -------- | ------------------------ |
| `owner` | `string` | **Required**. 用户登录名 |
| `value` | `string` | **Required**. 项目名称   |

### Response

校验未通过

```text
Status: 422 Unprocessable Entity
```

```json
{
    "errors": {
        "value": ["${filedErrorMessage}"]
    }
}
```

`filedErrorMessage` 的值为：

1. 当项目名称为空时返回 `项目名称不能为空`
2. 当项目名称中存在非法字符时返回 `只允许字母、数字、中划线(-)、下划线(_)、点(.)`
3. 登录用户下是否已存在该项目名时返回 `{owner}下已存在<strong>{name}</strong>项目`

校验通过

```text
Status: 200 OK
```

不返回任何内容。

## 创建项目

在创建项目方法中，也要在保存数据之前，做项目名称的校验。

```text
POST /projects
```

### Parameters

| Name          | Type      | Description              |
| ------------- | --------- | ------------------------ |
| `owner`       | `string`  | **Required**. 用户登录名 |
| `name`        | `string`  | **Required**. 项目名称   |
| `description` | `string`  | 项目描述                 |
| `public`      | `boolean` | 是否公开                 |

### Response

校验未通过

```text
Status: 422 Unprocessable Entity
```

```json
{
    "errors": {
        "value": ["${filedErrorMessage}"]
    }
}
```

`filedErrorMessage` 的值为：

1. 当项目名称为空时返回 `项目名称不能为空`
2. 当项目名称中存在非法字符时返回 `只允许字母、数字、中划线(-)、下划线(_)、点(.)`
3. 登录用户下是否已存在该项目名时返回 `{owner}下已存在<strong>{name}</strong>项目`

校验通过，且保存成功后

```text
Status: 201 CREATED
```

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `name`             | `string`   | 项目名称       |
| `description`      | `string`   | 项目描述       |
| `isPublic`         | `boolean`  | 是否公开       |
| `lastActiveTime`   | `datetime` | 最近活动时间   |
| `createUserName`   | `string`   | 创建用户名     |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |
