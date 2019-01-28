# 数据库表结构

本文档存储 Block Lang 平台的数据库表结构清单。

## 开发

1. `PROJECT`

### `PROJECT` - 项目基本信息

| 字段名 | 注释 | 类型 | 长度 | 默认值 | 主键 | 可空 |
| ------ | ---- | ---- | ---- | ------ | ---- | ---- |
| dbid   | 主键 | int  |      |        | 是   | 否   |

约束

* 主键：`PK-PROJECT`
* 外键：`FK-`
* 索引：`UK-`

说明

## 构建

1. `PROJECT_TAG`
2. `PROJECT_BUILD`

### `PROJECT_TAG` - GIT 仓库附注标签信息

为项目创建 GIT 附注标签

| 字段名     | 注释         | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ---------- | ------------ | ------- | ---- | ------ | ---- | ---- |
| dbid       | 主键         | int     |      |        | 是   | 否   |
| project_id | 项目标识     | int     |      |        |      | 否   |
| version    | 版本号       | varchar | 32   | 0.1.0  |      | 否   |
| git_tag_id | git 标签标识 | varchar | 50   |        |      | 否   |

约束

* 主键：`PK_PROJECT_TAG`
* 外键：(*未设置*)`FK_TAG_PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_TAG_PROJECT_ID_VERSION`，对应字段 `project_id`、`version`

说明

1. `version` 的取值遵循语义化版本号规范
2. git 标签名称是在 `version` 的值前加上字母 `v`，如 `v0.1.0`
3. `git_tag_id` 存 git 附注标签的标识

### `PROJECT_BUILD` - 项目构建信息

项目基于 git tag 标签构建。

| 字段名         | 注释     | 类型     | 长度 | 默认值 | 主键 | 可空 |
| -------------- | -------- | -------- | ---- | ------ | ---- | ---- |
| dbid           | 主键     | int      |      |        | 是   | 否   |
| project_tag_id | 项目标识 | int      |      |        |      | 否   |
| start_time     | 开始时间 | datetime |      |        |      | 否   |
| end_time       | 结束时间 | datetime |      |        |      | 否   |
| build_result   | 构建结果 | char     |      | 01     |      | 否   |

约束

* 主键：`PK_PROJECT_BUILD`
* 外键：`FK-`
* 索引：`UK-`

说明

1. `build_result` 的值为：`01` 表示 `未构建`，`02` 表示 `正在构建`，`03` 表示 `构建失败`，`04` 表示 `构建成功`

## 发布

1. `APP`
2. `APP_RELEASE`
3. `APP_RELEASE_RELATION`
4. `APP_RELEASE_FILE`
5. `WEB_SERVER`
6. `INSTALLER`

### `APP` - 应用程序基本信息

这里的 APP 是指能部署和运行的文件。

| 字段名             | 注释       | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------------ | ---------- | ------- | ---- | ------ | ---- | ---- |
| dbid               | 主键       | int     |      |        | 是   | 否   |
| project_id         | 项目标识   | int     |      |        |      | 是   |
| app_name           | app 名称   | varchar | 32   |        |      | 否   |
| registration_token | 注册 token | char    | 22   |        |      | 是   |

约束

* 主键：`PK_APP`
* 外键：(*未设置*)`FK_PROJECT`，`PROJECT_ID` 对应 `PROJECT` 表的 `dbid`
* 索引：`UK_REG_TOKEN`，对应字段 `registration_token`；`UK_APP_NAME`，对应字段 `app_name`

说明

1. `registration_token` 是22位的 UUID
2. `project_id` 字段的值可空，因为有些 APP（如 JDK）是直接上传的，不是根据项目编译的
3. 当 `project_id` 字段的值为空时，`registration_token` 的值也为空

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

1. `version` 采用语义化版本，自动发布的 APP，必须严格按照时间增加，在保存记录时要添加版本号的校验逻辑，确保新的版本比上一个发布的版本号大
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

1. `target_os` 的值为：`01` 表示 `Unknown`，`02` 表示 `Android`，`03` 表示 `Bitrig`，`04` 表示 `CloudABI`，`05` 表示 `Dragonfly`，`06` 表示 `Emscripten`，`07` 表示 `FreeBSD`，`08` 表示 `Fuchsia`，`09` 表示 `Haiku`，`10` 表示 `iOS`，`11` 表示 `Linux`，`12` 表示 `MacOS`，`13` 表示 `NetBSD`，`14` 表示 `OpenBSD`，`15` 表示 `Redox`，`16` 表示 `Solaris`，`17` 表示 `Windows`，`99` 表示 `Any`
2. `arch` 的值为：`01` 表示 `Unknown`，`02` 表示 `X86_64`，`03` 表示 `X86`，`04` 表示 `WASM32`，`05` 表示 `SPARC64`，`06` 表示 `S390X`，`07` 表示 `RISCV`，`08` 表示 `POWERPC64`，`09` 表示 `POWERPC`，`10` 表示 `MSP430`，`11` 表示 `MIPS64`，`12` 表示 `MIPS`，`13` 表示 `ASMJS`，`14` 表示 `ARM`，`15` 表示 `AARCH64`，`99` 表示 `Any`
3. linux 系统的文件名最大长度是255，windows 系统的文件名最大长度是260，所以 `file_name` 的长度是255
4. 区别：`target_os` 的值为 `linux`，但 `os_type` 的值可具体到 `ubuntu`
5. 如果是跨平台，则 `target_os` 值为 `Any`

### `WEB_SERVER` - 应用服务器信息

| 字段名       | 注释           | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------ | -------------- | ------- | ---- | ------ | ---- | ---- |
| dbid         | 主键           | int     |      |        | 是   | 否   |
| server_token | 服务器 token   | varchar | 50   |        |      | 否   |
| ip           | ip 地址        | varchar | 50   |        |      | 否   |
| os_type      | 操作系统类型   | char    | 2    | 99     |      | 否   |
| os_version   | 操作系统版本号 | varchar | 32   |        |      | 否   |
| arch         | CPU 架构       | char    | 2    | 99     |      | 否   |

约束

* 主键：`PK_WEB_SERVER`
* 外键：无
* 索引：`UK_SERVER_TOKEN`，对应字段 `server_token`

说明

1. `os_type` 的值为：`01` 表示 `Unknown`，`02` 表示 `Android`，`03` 表示 `Emscripten`，`04` 表示 `Linux`，`05` 表示 `Redhat`，`06` 表示 `Ubuntu`，`07` 表示 `Debian`，`08` 表示 `Arch`，`09` 表示 `Centos`，`10` 表示 `Fedora`，`11` 表示 `Alpine`，`12` 表示 `Macos`，`13` 表示 `Redox`，`14` 表示 `Windows`，`99` 表示 `Any`
2. `arch` 是编码字段
3. 注意，比较 `os_type` 时要忽略大小写
4. `server_token` 的值取服务器的 MAC 地址

### `INSTALLER` - APP 安装器信息

| 字段名          | 注释           | 类型 | 长度 | 默认值 | 主键 | 可空 |
| --------------- | -------------- | ---- | ---- | ------ | ---- | ---- |
| dbid            | 主键           | int  |      |        | 是   | 否   |
| web_server_id   | 应用服务器标识 | int  |      |        |      | 否   |
| app_release_id  | app 发行版标识 | int  |      |        |      | 否   |
| app_run_port    | app 运行端口   | int  |      | 80     |      | 否   |
| installer_token | 安装器 token   | char | 22   |        |      | 否   |

约束

* 主键：`PK_INSTALLER`
* 外键：(*未设置*)`FK_WEB_SERVER`，`web_server_id` 对应 `WEB_SERVER` 的 `dbid`；`FK_APP_RELEASE` 对应 `APP_RELEASE` 的 `dbid`
* 索引：`UK_INSTALLER_TOKEN`，对应字段 `installer_token`

说明

1. `installer_token` 是22位的 UUID

## 草稿

部署软件下载记录
部署日志
项目在发布时要指定依赖的 JDK 版本
发布的软件可手工上传，也可以走 CI 流程。
