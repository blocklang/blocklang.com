# 发布一个组件仓库

往 Block Lang 组件仓库中发布一个组件库。组件库可以是托管在任一基于 git 的源代码托管网站上的一个仓库。

如果是一个 web 版 UI 部件，则在仓库的根目录要包含 `package.json` 文件，其中要包含以下信息

1. `name` - 组件库的名称(必填)
2. `version` - 组件库的版本(必填)
3. `displayName` - 组件库的显示名(可选)
4. `description` - 组件库详细介绍(可选)
5. `icon` - 组件库 logo 的存放路径(可选)
6. `category` - 组件库种类，当前仅支持 `Widget`(必填)

在 Block Lang 组件市场发布的内容，是基于 git 仓库的 tag 的。在发布时，查找到最新标注的 tab 发布。如果没有为仓库打标签，则提示用户先打标签。

注意：此操作在服务器端是异步操作。

```text
POST /component-repos
```

注意：**支持实时输出校验结果**。

## Parameters

| Name     | Type     | Description                |
| -------- | -------- | -------------------------- |
| `gitUrl` | `string` | **Required**. git 仓库地址 |

## Response

只有登录用户才有权发布组件，如果用户未登录，则返回

```text
Status: 403 Forbidden
```

输入参数校验

```text
Status: 422 Unprocessable Entity
```

只有通过以下校验后，才能开始发布：

1. `gitUrl` 不能为空字符串
2. `gitUrl` 是一个有效的基于 https 协议的 git 仓库地址
3. `gitUrl` 指定的是一个有效的 git 远程仓库
4. 判断在登录用户是否发布过 `gitUrl`
5. git 仓库的根目录下包含一个 `package.json` 文件
6. `package.json` 中包含 `name`、`version`、和 `category` 信息
7. 如果是 `Widget` 仓库，则按照约定的目录结构，找到部件的 `changelog` 信息，校验格式是否有效
8. 校验项目是否能编译通过

创建成功

```text
Status: 201 CREATED
```

| Name            | Type     | Description  |
| --------------- | -------- | ------------ |
| `id`            | `int`    | 发行版标识   |
| `gitUrl`        | `string` | git 仓库地址 |
| `startTime`     | `string` | 发布开始时间 |
| `endTime`       | `string` | 发布开始时间 |
| `releaseResult` | `string` | 发布结果     |
