# 获取部署配置信息

根据登录用户和项目信息生成部署配置信息，如 `registration_token`

```text
GET /projects/{owner}/{projectName}/deploy_setting
```

## Parameters

| Name          | Type     | Description                   |
| ------------- | -------- | ----------------------------- |
| `owner`       | `string` | **Required**. 用户登录名      |
| `projectName` | `string` | **Required**. 项目名称        |

## Response

如果用户登录，则返回

```text
Status: 200 OK
```

| 属性名                | 类型     | 描述                          |
| --------------------- | -------- | ----------------------------- |
| `id`                  | `int`    | 记录标识                      |
| `projectId`           | `int`    | 项目标识                      |
| `userId`              | `int`    | 部署用户标识                  |
| `registrationToken`   | `string` | 注册 Token                    |
| `url`                 | `string` | 注册 API 链接                 |
| `installerLinuxUrl`   | `string` | linux 版 installer 下载地址   |
| `installerWindowsUrl` | `string` | windows 版 installer 下载地址 |
| `deployState`         | `string` | 部署状态                      |

如果用户未登录，则返回

```text
Status: 403 Forbidden
```
