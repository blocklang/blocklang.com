# 项目基本信息

- [项目基本信息](#%E9%A1%B9%E7%9B%AE%E5%9F%BA%E6%9C%AC%E4%BF%A1%E6%81%AF)
  - [校验项目名称](#%E6%A0%A1%E9%AA%8C%E9%A1%B9%E7%9B%AE%E5%90%8D%E7%A7%B0)
    - [Parameters](#parameters)
    - [Response](#response)
  - [创建项目](#%E5%88%9B%E5%BB%BA%E9%A1%B9%E7%9B%AE)
    - [Parameters](#parameters-1)
    - [Response](#response-1)
  - [查看项目的目录结构](#%E6%9F%A5%E7%9C%8B%E9%A1%B9%E7%9B%AE%E7%9A%84%E7%9B%AE%E5%BD%95%E7%BB%93%E6%9E%84)
    - [Parameters](#parameters-2)
    - [Response](#response-2)
  - [查看项目中的一个对象](#%E6%9F%A5%E7%9C%8B%E9%A1%B9%E7%9B%AE%E4%B8%AD%E7%9A%84%E4%B8%80%E4%B8%AA%E5%AF%B9%E8%B1%A1)
  - [获取项目的 README 文件](#%E8%8E%B7%E5%8F%96%E9%A1%B9%E7%9B%AE%E7%9A%84-readme-%E6%96%87%E4%BB%B6)
    - [Parameters](#parameters-3)
    - [Response](#response-3)

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
| `name`  | `string` | **Required**. 项目名称   |

### Response

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

返回的数据

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

## 查看项目的目录结构

逐层获取项目的结构。如果当前层级是目录（或者叫）分组，则获取分组下的直属内容列表。

```text
GET /projects/{owner}/{projectName}/tree/{pathId}
```

### Parameters

| Name          | Type     | Description               |
| ------------- | -------- | ------------------------- |
| `owner`       | `string` | **Required**. 用户登录名  |
| `projectName` | `string` | **Required**. 项目名称    |
| `pathId`      | `string` | 当前目录的标识，默认为 -1 |

### Response

```text
Status: 200 OK
```

返回一个数组

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `key`              | `string`   | 资源标识       |
| `name`             | `string`   | 资源名称       |
| `description`      | `string`   | 资源描述       |
| `resource_type`    | `string`   | 资源类型       |
| `parent_id`        | `int`      | 父标识         |
| `seq`              | `int`      | 排序           |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |

如果没有找到此目录结构或者用户没有访问权限，则

```text
Status: 404 Not Found
```

## 查看项目中的一个对象

如果当前层级是程序模块（叶节点）等，则获取文件的内容。

```text
GET /projects/{owner}/{projectName}/blob/{path}
```

## 获取项目的 README 文件

默认为每个项目创建一个 `README.md` 文件。

```text
GET /projects/{owner}/{projectName}/readme
```

### Parameters

| Name          | Type     | Description               |
| ------------- | -------- | ------------------------- |
| `owner`       | `string` | **Required**. 用户登录名  |
| `projectName` | `string` | **Required**. 项目名称    |

### Response

```text
Status: 200 OK
```

返回的是一段文本，不是 json 对象。返回的是 README.md 文档的内容。
