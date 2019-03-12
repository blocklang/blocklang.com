# 获取项目的 README 文件

默认为每个项目创建一个 `README.md` 文件。

```text
GET /projects/{owner}/{projectName}/readme
```

## Parameters

| Name          | Type     | Description               |
| ------------- | -------- | ------------------------- |
| `owner`       | `string` | **Required**. 用户登录名  |
| `projectName` | `string` | **Required**. 项目名称    |

## Response

```text
Status: 200 OK
```

返回的是一段文本，不是 json 对象。返回的是 README.md 文档的内容。