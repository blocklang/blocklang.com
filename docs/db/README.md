# 数据库表结构

本文档存储 Block Lang 平台的数据库表结构清单。

## 发布

1. PROJECT
2. WEB_SERVER
3. INSTALLER
4. APP
5. APP_RELEASE
6. APP_RELEASE_FILE
7. APP_DEPEND

### `PROJECT` - 项目基本信息

| 字段名             | 注释       | 类型   | 长度 | 默认值 | 主键 | 可空 |
| ------------------ | ---------- | ------ | ---- | ------ | ---- | ---- |
| id                 | 主键       | int    |      |        | 是   | 否   |
| registration_token | 注册 token | string |      |        |      | 是   |

* 主键：`PK-PROJECT`
* 外键：`FK-`
* 索引：`UK-`

### `WEB_SERVER` - 应用服务器

| 字段名       | 注释           | 类型   | 长度 | 默认值 | 主键 | 可空 |
| ------------ | -------------- | ------ | ---- | ------ | ---- | ---- |
| id           | 主键           | int    |      |        | 是   | 否   |
| server_token | 服务器 token   | string |      |        |      | 否   |
| ip           | ip 地址        | string |      |        |      | 否   |
| os_type      | 操作系统类型   | string |      |        |      |      |
| os_version   | 操作系统版本号 | string |      |        |      |      |
| arch         | CPU 架构       | string |      |        |      |      |

* 主键：`PK-`
* 外键：`FK-`
* 索引：`UK-`

### `INSTALLER` - 安装器信息

| 字段名          | 注释           | 类型   | 长度 | 默认值 | 主键 | 可空 |
| --------------- | -------------- | ------ | ---- | ------ | ---- | ---- |
| id              | 主键           | int    |      |        | 是   | 否   |
| web_server_id   | 应用服务器标识 | string |      |        |      | 否   |
| app_release_id  | app 发布标识   | int    |      |        |      | 否   |
| app_run_port    | app 运行端口   | int    |      |        |      | 否   |
| installer_token | 安装器 token   | string |      |        |      | 否   |

* 主键：`PK-`
* 外键：`FK-`
* 索引：`UK-`

### `APP` - 应用程序基本信息

这里的 APP 是指能部署和运行的文件。

| 字段名     | 注释     | 类型   | 长度 | 默认值 | 主键 | 可空 |
| ---------- | -------- | ------ | ---- | ------ | ---- | ---- |
| id         | 主键     | int    |      |        | 是   | 否   |
| project_id | 项目标识 | int    |      |        |      | 否   |
| app_name   | app 名称 | string |      |        |      | 否   |

* 主键：`PK-`
* 外键：`FK-PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `ID`
* 索引：`UK-`

### `APP_RELEASE` - 应用程序发布信息

| 字段名         | 注释         | 类型      | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------ | --------- | ---- | ------ | ---- | ---- |
| id             | 主键         | int       |      |        | 是   | 否   |
| app_id         | 应用程序标识 | int       |      |        |      | 否   |
| version        | 版本         | string    |      |        |      | 否   |
| title          | 发布标题     | string    |      |        |      | 否   |
| description    | 发布说明     | string    |      |        |      | 是   |
| release_time   | 发布时间     | date_time |      |        |      | 否   |
| release_method | 发布方法     | string    |      |        |      | 否   |

* 主键：`PK-`
* 外键：`FK-PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `ID`
* 索引：`UK-`

1. `release_method` 的值为 `ci`、`manual`

### `APP_RELEASE_FILE` - 应用程序发布文件信息

一个发行版为了适配不同的系统和架构，会有多个发布文件。

| 字段名         | 注释                          | 类型   | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ----------------------------- | ------ | ---- | ------ | ---- | ---- |
| id             | 主键                          | int    |      |        | 是   | 否   |
| app_release_id | 应用程序发布标识              | int    |      |        |      | 否   |
| file_name      | 原文件名                      | string |      |        |      | 否   |
| target_os      | 操作系统，如 windows 或 linux | string |      |        |      | 是   |
| arch           | CPU 架构                      | string |      |        |      | 是   |
| file_path      | 文件的存储路径                | string |      |        |      | 否   |

* 主键：`PK-`
* 外键：`FK-PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `ID`
* 索引：`UK-`

1. `target_os` 的值为：`windows`、`linux` 和 `any`
2. `arch` 的值为：`x86`、`x86_64` 和 `any` 等

### `APP_DEPEND` - 应用程序依赖关系

依赖关系是指 `APP_RELEASE` 表中记录间的依赖关系。

| 字段名                | 注释                   | 类型 | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ---------------------- | ---- | ---- | ------ | ---- | ---- |
| id                    | 主键                   | int  |      |        | 是   | 否   |
| app_release_id        | 应用程序发布标识       | int  |      |        |      | 否   |
| depend_app_release_id | 依赖的应用程序发布标识 | int  |      |        |      | 否   |

* 主键：`PK-`
* 外键：`FK-`
* 索引：`UK-`







为发布平台编码，如果是跨平台，则值为 any

1. 项目 token 信息
2. 部署项目的服务器信息
3. 部署项目的运行信息（为后续的一个服务器上部署多个软件做准备）
4. 部署软件信息
5. 部署日志

部署主机
部署 runner 实例
部署软件
ruuner 实例项目关系
部署软件下载记录
部署日志

部署主机
软件 app
软件实例 app-instance
app-release 记录所有发布版本
app-dependence  JDK

所有软件文件统一管理，release 版本信息等单独管理？

一个项目支持生成多个 token，这样可支持集群部署等，即一个app实例对应一个 token。

项目在发布时要指定依赖的 JDK 版本

发布的软件可手工上传，也可以走 CI 流程。
