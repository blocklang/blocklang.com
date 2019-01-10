# 数据库表结构

本文档存储 Block Lang 平台的数据库表结构清单。

## 发布

1. `PROJECT`
2. `APP`
3. `APP_RELEASE`
4. `APP_RELEASE_RELATION`
5. `APP_RELEASE_FILE`
6. `WEB_SERVER`
7. `INSTALLER`

### `PROJECT` - 项目基本信息

| 字段名 | 注释 | 类型 | 长度 | 默认值 | 主键 | 可空 |
| ------ | ---- | ---- | ---- | ------ | ---- | ---- |
| dbid   | 主键 | int  |      |        | 是   | 否   |

* 主键：`PK-PROJECT`
* 外键：`FK-`
* 索引：`UK-`

### `APP` - 应用程序基本信息

这里的 APP 是指能部署和运行的文件。

| 字段名             | 注释       | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------ | ---------- | ------- | ---- | ------ | ---- | ---- |
| dbid               | 主键       | int     |      |        | 是   | 否   |
| project_id         | 项目标识   | int     |      |        |      | 是   |
| app_name           | app 名称   | varchar | 32   |        |      | 否   |
| registration_token | 注册 token | char    | 22   |        |      | 否   |

约束

* 主键：`PK_APP`
* 外键：(*未设置*)`FK_PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_REG_TOKEN`，对应字段 `registration_token`

说明

1. `registration_token` 是22位的 UUID
2. `project_id` 字段的值可空，因为有些 APP（如 JDK）是直接上传的，不是根据项目编译的

### `APP_RELEASE` - 应用程序发行版基本信息

| 字段名         | 注释         | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------ | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键         | int      |      |        | 是   | 否   |
| app_id         | 应用程序标识 | int      |      |        |      | 否   |
| version        | 版本         | varchar  | 32   | 0.1.0  |      | 否   |
| title          | 发行版标题   | varchar  | 64   |        |      | 否   |
| description    | 发行版说明   | clob     |      |        |      | 是   |
| release_time   | 发布时间     | datetime |      |        |      | 否   |
| release_method | 发布方法     | char     | 2    |        |      | 否   |

约束

* 主键：`PK_APP_RELEASE`
* 外键：(*未设置*)`FK_APP`，`app_id` 对应 `APP` 表的 `dbid`
* 索引：`UK_APP_ID_VERSION`，对应字段 `app_id`、`version`

说明

1. `version` 采用语义化版本
2. `release_method` 的值为：`01` 表示自动发布，`02` 表示人工上传
3. 一个 APP 必须至少发布一个版本后，才能注册 installer

### `APP_RELEASE_RELATION` - 应用程序发行版依赖关系

依赖关系是指 `APP_RELEASE` 表中记录间的依赖关系，如 Spring Boot Jar 要运行在 JDK 上，则称 Spring Boot Jar 依赖。

| 字段名                | 注释                     | 类型 | 长度 | 默认值 | 主键 | 可空 |
| --------------------- | ------------------------ | ---- | ---- | ------ | ---- | ---- |
| dbid                  | 主键                     | int  |      |        | 是   | 否   |
| app_release_id        | 应用程序发行版标识       | int  |      |        |      | 否   |
| depend_app_release_id | 依赖的应用程序发行版标识 | int  |      |        |      | 否   |

约束

* 主键：`PK_APP_RELEASE_RELATION`
* 外键：无
* 索引：`UK_APP_RELEASE_DEPEND`，对应字段 `app_release_id`、`depend_app_release_id`

说明

1. 此表不需要加4个辅助字段，因为这些值与 `APP_RELEASE` 表中的值相同

### `APP_RELEASE_FILE` - 应用程序发行版文件信息

一个发行版为了适配不同的系统和架构，会有多个文件。

| 字段名         | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid           | 主键               | int     |      |        | 是   | 否   |
| app_release_id | 应用程序发行版标识 | int     |      |        |      | 否   |
| target_os      | 操作系统类型       | char    | 2    | 99     |      | 否   |
| arch           | CPU 架构           | char    | 2    | 99     |      | 否   |
| file_name      | 文件名             | varchar | 255  |        |      | 否   |
| file_path      | 文件的存储路径     | varchar | 255  |        |      | 否   |

约束

* 主键：`PK_APP_RELEASE_FILE`
* 外键：(*未设置*)`FK_APP_RELEASE`，`app_release_id` 对应 `APP_RELEASE` 表的 `dbid`
* 索引：`UK_RELEASE_OS_ARCH`，对应字段 `app_release_id`、`target_os`、`arch`

说明

1. `target_os` 的值为：`01` 表示 `linux`，`02` 表示 `windows`，`99` 表示 `any`
2. `arch` 的值为：`01` 表示 `x86`，`02` 表示 `x86_64`，`99` 表示 `any`
3. linux 系统的文件名最大长度是255，windows 系统的文件名最大长度是260，所以 `file_name` 的长度是255

### `WEB_SERVER` - 应用服务器

| 字段名       | 注释           | 类型   | 长度 | 默认值 | 主键 | 可空 |
| ------------ | -------------- | ------ | ---- | ------ | ---- | ---- |
| dbid         | 主键           | int    |      |        | 是   | 否   |
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
| dbid            | 主键           | int    |      |        | 是   | 否   |
| web_server_id   | 应用服务器标识 | string |      |        |      | 否   |
| app_release_id  | app 发布标识   | int    |      |        |      | 否   |
| app_run_port    | app 运行端口   | int    |      |        |      | 否   |
| installer_token | 安装器 token   | string |      |        |      | 否   |

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
