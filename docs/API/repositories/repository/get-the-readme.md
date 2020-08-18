# 获取仓库的 README 文件

默认为每个仓库创建一个 `README.md` 文件。

```text
GET /repos/{owner}/{repoName}/readme
```

## Parameters

| Name       | Type     | Description              |
| ---------- | -------- | ------------------------ |
| `owner`    | `string` | **Required**. 用户登录名 |
| `repoName` | `string` | **Required**. 仓库名称   |

## Response

```text
Status: 200 OK
```

返回的是一段文本，不是 json 对象。返回的是 README.md 文档的内容。