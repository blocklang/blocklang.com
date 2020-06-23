# 获取 IDE 项目资源

在页面设计器中设计页面时，需要通过 `<script />` 方式加载 IDE 版的资源，目前确定的资源包含：

1. `main.bundle.js` 和 `main.bundle.js.map`
2. `main.bundle.css` 和 `main.bundle.css.map`
3. `icons.svg`

本 API 就是用于加载这些文件。

```text
GET /designer/assets/{gitRepoWebsite}/{gitRepoOwner}/{gitRepoName}/{version}/{fileName}
```

## Parameters

| Name             | Type     | Description                       |
| ---------------- | -------- | --------------------------------- |
| `gitRepoWebsite` | `string` | git 源码托管网站，如 `github.com` |
| `gitRepoOwner`   | `string` | git 仓库拥有者                    |
| `gitRepoName`    | `string` | git 仓库名                        |
| `version`        | `string` | 项目版本号                        |
| `fileName`       | `string` | 资源的文件名，包含后缀名          |

## Response

如果资源不存在，则返回

```text
Status: 404 Not Found
```

如果资源存在，则返回

```text
Status: 200 OK
```

并返回该资源。
