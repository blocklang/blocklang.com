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

| 属性名           | 类型      | 描述                         |
| ---------------- | --------- | ---------------------------- |
| `id`             | `string`  | 添加到页面后生成的部件标识   |
| `parentId`       | `string`  | 添加到页面后生成的父部件标识 |
| `widgetId`       | `int`     | 部件标识                     |
| `widgetCode`     | `string`  | 部件编码                     |
| `widgetName`     | `string`  | 部件名称                     |
| `canHasChildren` | `boolean` | 是否可以包含子部件           |
| `apiRepoId`      | `int`     | API 仓库标识                 |
| `properties`     | `Array`   | 部件的属性列表               |

`properties` 是部件的属性列表，是一个 Json Array，其中的字段为

| 属性名      | 类型      | 描述                           |
| ----------- | --------- | ------------------------------ |
| `code`      | `int`     | 属性编码                       |
| `name`      | `string`  | 属性名                         |
| `valueType` | `string`  | 属性值数据类型                 |
| `eventArgs` | `Array`   | 事件的参数列表                 |
| `id`        | `int`     | 属性添加到页面后，新生成的标识 |
| `value`     | `string`  | 属性值                         |
| `expr`      | `boolean` | 是否包含表达式                 |

`eventArgs` 是事件定义中的参数列表，是一个 Json Array，其中的字段为

| 属性名         | 类型     | 描述                   |
| -------------- | -------- | ---------------------- |
| `code`         | `int`    | 参数编码               |
| `name`         | `string` | 参数名                 |
| `label`        | `string` | 参数的显示名（移除？） |
| `valueType`    | `string` | 参数的数据类型         |
| `defaultValue` | `string` | 参数的默认值           |
| `description`  | `string` | 参数描述               |

`data` 是页面中的数据列表，是一个 Json Array，其中的字段为

| 属性名     | 类型     | 描述                 |
| ---------- | -------- | -------------------- |
| `id`       | `string` | 页面数据项标识       |
| `parentId` | `string` | 页面数据项父标识     |
| `name`     | `string` | 页面数据项的变量名   |
| `type`     | `string` | 页面数据项的数据类型 |
| `value`    | `string` | 页面数据项的默认值   |


`functions` 是页面中的函数列表，是一个 Json Array，其中的字段为

| 属性名                | 类型     | 描述             |
| --------------------- | -------- | ---------------- |
| `id`                  | `string` | 事件处理函数标识 |
| `nodes`               | `Array`  | 节点列表         |
| `sequenceConnections` | `Array`  | 序列连接线列表   |
| `dataConnections`     | `Array`  | 数据连接线列表   |

`nodes` 是节点列表，是一个 Json Array，其中的字段为

| 属性名                | 类型     | 描述                 |
| --------------------- | -------- | -------------------- |
| `id`                  | `string` | 节点标识             |
| `left`                | `int`    | 距左上角的 x 值      |
| `top`                 | `int`    | 距左上角的 y 值      |
| `caption`             | `string` | 标题                 |
| `text`                | `string` | 简述                 |
| `layout`              | `string` | 节点布局             |
| `category`            | `string` | 函数定义或调用类型   |
| `dataItemId`          | `string` | 引用的数据项标识     |
| `inputSequencePort`   | `Object` | 输入型的序列端口     |
| `outputSequencePorts` | `Array`  | 输出型的序列端口列表 |
| `inputDataPorts`      | `Array`  | 输入型的数据端口列表 |
| `outputDataPorts`     | `Array`  | 输出型的数据端口列表 |

`inputSequencePort` 是一个 Json Object，其中的字段为

| 属性名 | 类型     | 描述     |
| ------ | -------- | -------- |
| `id`   | `string` | 端口标识 |

`outputSequencePorts` 是一个 Json Array，其中的字段为

| 属性名 | 类型     | 描述             |
| ------ | -------- | ---------------- |
| `id`   | `string` | 端口标识         |
| `text` | `string` | 端口上显示的文本 |

`inputDataPorts` 是一个 Json Array，其中的字段为

| 属性名  | 类型     | 描述                   |
| ------- | -------- | ---------------------- |
| `id`    | `string` | 端口标识               |
| `name`  | `string` | 端口名称               |
| `type`  | `string` | 端口上存储值的数据类型 |
| `value` | `string` | 端口上存储的值         |


`outputDataPorts` 是一个 Json Array，其中的字段为

| 属性名 | 类型     | 描述                   |
| ------ | -------- | ---------------------- |
| `id`   | `string` | 端口标识               |
| `name` | `string` | 端口名称               |
| `type` | `string` | 端口上存储值的数据类型 |

`sequenceConnections` 和 `dataConnections` 分别是一个 Json Array，其中的字段为

| 属性名       | 类型     | 描述                       |
| ------------ | -------- | -------------------------- |
| `id`         | `string` | 连接标识                   |
| `fromNode`   | `string` | 起始节点标识               |
| `fromOutput` | `string` | 起始节点中的输出型端口标识 |
| `toNode`     | `string` | 终止节点标识               |
| `toInput`    | `string` | 终止节点中的输入型端口标识 |

注意:

1. `widgetName` 来自 `API_COMPONENT` 表中的 `name`;
2. `properties` 是一个数组，其中即包含普通属性，也包含事件（valueType 的值为 function 就表示事件）
3. `name` 如果属性的 `label` 有值则取 `label` 值，否则取 `name` 值
4. 注意返回的 widgets **必须按照 seq 排序**，即返回结果的顺序，要与保存时保持一致
