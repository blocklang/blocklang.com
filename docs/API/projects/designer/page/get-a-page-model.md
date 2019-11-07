# 获取一个页面的模型

注意：**返回的每个字段都必须严格把关，如果在页面中用不到则不能添加**。

```text
GET /designer/pages/{pageId}/model
```

## Parameters

| Name     | Type     | Description |
| -------- | -------- | ----------- |
| `pageId` | `number` | 页面标识    |

## Response

页面不存在

```text
Status: 404 Not Found
```

对页面所属的项目没有读权限

```text
Status: 403 Forbidden
```

否则返回

```text
Status: 200 OK
```

返回的数据是一个 json 对象

```json
{
    "pageId": 1,
    "widgets": [],
    "data": [],
    "functions": [],
    "services": []
}
```

`widgets` 是页面中包含的部件列表，是一个 Json Array，其中的字段为

| 属性名       | 类型     | 描述                         |
| ------------ | -------- | ---------------------------- |
| `id`         | `string` | 添加到页面后生成的部件标识   |
| `parentId`   | `string` | 添加到页面后生成的父部件标识 |
| `widgetId`   | `int`    | 部件标识                     |
| `widgetCode` | `string` | 部件编码                     |
| `widgetName` | `string` | 部件名称                     |
| `canHasChildren` | `boolean` | 是否可以包含子部件                     |
| `apiRepoId`      | `int`    | API 仓库标识       |
| **properties**   |          |                    |
| `id`             | `int`    | 属性标识           |
| `name`           | `string` | 属性名             |
| `value`          | `string` | 属性值             |
| `valueType`      | `string` | 属性值数据类型     |

注意:

1. `widgetName` 来自 `API_COMPONENT` 表中的 `name`;
2. `properties` 是一个数组，其中即包含普通属性，也包含事件（valueType 的值为 function 就表示事件）
3. `name` 如果属性的 `label` 有值则取 `label` 值，否则取 `name` 值
