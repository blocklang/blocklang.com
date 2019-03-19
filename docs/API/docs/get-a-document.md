# 查看一个文档

查看指定的文档

```text
GET /docs/{fileName}
```

## Parameters

| Name       | Type     | Description                          |
| ---------- | -------- | ------------------------------------ |
| `fileName` | `string` | **Required**. 文件名，不带文件扩展名 |

## Response

如果文档存在，则返回

```text
Status: 200 OK
```

返回 Markdown 格式的文档内容。

如果文档不存在，则返回

```text
Status: 404 Not Found
```