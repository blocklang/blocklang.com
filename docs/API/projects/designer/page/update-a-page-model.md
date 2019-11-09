# 更新一个页面的模型

如果页面模型不存在，则新建页面模型；如果已存在，则更新页面模型。

注意：

1. **返回的每个字段都必须严格把关，如果在页面中用不到则不能添加**；
2. 在保存时，`widgets` 要按顺序保存，seq 从 1 开始。

```text
PUT /designer/pages/{pageId}/model
```

## Parameters

| Name        | Type         | Description    |
| ----------- | ------------ | -------------- |
| `pageId`    | `int`        | 页面基本信息   |
| `widgets`   | `Json Array` | 页面部件       |
| `functions` | `Json Array` | 页面行为       |
| `data`      | `Json Array` | 页面数据       |
| `services`  | `Json Array` | 页面引用的服务 |

`widgets` 是页面中包含的部件列表，是一个 Json Array，其中的字段为

| 属性名           | 类型      | 描述                         |
| ---------------- | --------- | ---------------------------- |
| `id`             | `string`  | 添加到页面后生成的部件标识   |
| `parentId`       | `string`  | 添加到页面后生成的父部件标识 |
| `widgetId`       | `int`     | 部件标识                     |
| `widgetCode`     | `string`  | 部件编码                     |
| `widgetName`     | `string`  | 部件名称                     |
| `canHasChildren` | `boolean` | 是否可以包含子部件           |
| `apiRepoId`      | `int`     | API 仓库标识                 |
| **properties**   |           |                              |
| `id`             | `int`     | 属性标识                     |
| `name`           | `string`  | 属性名                       |
| `value`          | `string`  | 属性值                       |
| `valueType`      | `string`  | 属性值数据类型               |

## Response

页面不存在，则返回

```text
Status: 404 Not Found
```

用户未登录或者对页面所属的项目没有写权限，则返回

```text
Status: 403 Forbidden
```

否则返回

```text
Status: 201 CREATED
```

不返回任何数据。
