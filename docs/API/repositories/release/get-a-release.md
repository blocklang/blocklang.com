# 获取软件的一个发行版信息

只获取发行版的任务信息。

注意：一个版本只对应一个发行版。不需要为一个版本构建多次，如果是构建过程出错，则允许用户重新构建。

```text
GET /projects/{owner}/{projectName}/releases/{version}
```

## Parameters

| Name          | Type     | Description              |
| ------------- | -------- | ------------------------ |
| `owner`       | `string` | **Required**. 用户登录名 |
| `projectName` | `string` | **Required**. 项目名称   |
| `version`     | `string` | 语义化版本，如 0.1.0     |

## Response

如果发布任务存在，则返回

```text
Status: 200 OK
```

| 属性名                | 类型       | 描述                  |
| --------------------- | ---------- | --------------------- |
| `id`                  | `int`      | 记录标识              |
| `projectId`           | `string`   | 项目标识              |
| `version`             | `string`   | 语义化版本            |
| `title`               | `string`   | 发行版标题            |
| `description`         | `string`   | 发行版说明            |
| `jdkReleaseId`        | `int`      | 依赖的 jdk 发行版标识 |
| `startTime`          | `datetime` | 发布开始时间          |
| `endTime`            | `int`      | 发布结束时间          |
| `releaseResult`       | `string`   | 发布结果              |
| `jdkName`             | `string`   | 依赖的 jdk 名称       |
| `jdkVersion`          | `string`   | 依赖的 jdk 版本号     |
| `createUserName`      | `string`   | 创建用户名            |
| `createUserAvatarUrl` | `string`   | 创建用户头像          |

如果发布任务不存在，则返回

```text
Status: 404 Not Found
```