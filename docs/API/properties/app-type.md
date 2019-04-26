# 获取软件类型

为了让项目结构足够灵活，允许在一个项目中存储不同软件类型的实现，如 web 版和 mobile 版，此时就需要 app-type 对页面加以区分。

```text
GET /properties/app-type
```

## Parameters

无

## Response

获取成功

```text
Status: 200 OK
```

返回一个 JSON 数组，其中的 JSON 对象字段为：

| Name    | Type     | Description |
| ------- | -------- | ----------- |
| `key`   | `string` | 属性名      |
| `value` | `string` | 属性值      |
| `icon`  | `string` | 图标样式类  |
