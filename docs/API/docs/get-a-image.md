# 获取文档中的一个图片

```text
GET /raw/docs/{filePath}
```

## Parameters

| Name       | Type     | Description            |
| ---------- | -------- | ---------------------- |
| `filePath` | `string` | **Required**. 图片路径 |

## Response

如果图片存在，则返回

```text
Status: 200 OK
```

返回图片

如果文档不存在，则返回

```text
Status: 404 Not Found
```