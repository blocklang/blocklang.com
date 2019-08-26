# 获取一个页面的模型

在创建页面之前，要对页面的 key 和 name 做校验。

只有 key 和 name 都校验之后才返回错误信息，而不是发现错误就返回。

注意：**返回的每个字段都必须严格把关，如果在页面中用不到则不能添加**。

```text
GET /pages/{pageId}/model
```

## Parameters

| Name     | Type     | Description |
| -------- | -------- | ----------- |
| `pageId` | `number` | 页面标识    |

## Response

对页面所属的项目没有读权限

```text
Status: 403 Forbidden
```

页面不存在

```text
Status: 404 Not Found
```

否则返回

```text
Status: 200 OK
```

返回的数据是一个 json 对象

```json
{
    "pageInfo": {},
    "data": [],
    "widgets": [],
    "functions": [],
    "services": []
}
```

`pageInfo` 是页面基本信息，是一个 Json Object，字段为

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

`widgets` 是页面中包含的部件列表，是一个 Json Array，其中的字段为

| 属性名         | 类型     | 描述           |
| -------------- | -------- | -------------- |
| `id`           | `int`    | 页面部件标识   |
| `parentId`     | `int`    | 父记录标识     |
| `apiRepoId`    | `int`    | API 仓库标识   |
| `widgetCode`   | `string` | 部件编码       |
| `widgetName`   | `string` | 部件名称       |
| **properties** |          |                |
| `id`           | `int`    | 属性标识       |
| `name`         | `string` | 属性名         |
| `label`        | `string` | 属性显示名     |
| `value`        | `string` | 属性值         |
| `valueType`    | `string` | 属性值数据类型 |
| **events**     |          |                |
| `id`           | `int`    | 事件标识       |
| `name`         | `string` | 事件名         |
| `label`        | `string` | 事件显示名     |
