# `APP_RELEASE_FILE` - 应用程序发行版文件信息

一个发行版为了适配不同的系统和架构，会有多个文件。

## 字段

| 字段名         | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| -------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid           | 主键               | int     |      |        | 是   | 否   |
| app_release_id | 应用程序发行版标识 | int     |      |        |      | 否   |
| target_os      | 操作系统类型       | char    | 2    | 99     |      | 否   |
| arch           | CPU 架构           | char    | 2    | 99     |      | 否   |
| file_name      | 文件名             | varchar | 255  |        |      | 否   |
| file_path      | 文件的存储路径     | varchar | 255  |        |      | 否   |

## 约束

* 主键：`PK_APP_RELEASE_FILE`
* 外键：(*未设置*)`FK_APP_RELEASE`，`app_release_id` 对应 `APP_RELEASE` 表的 `dbid`
* 索引：`UK_RELEASE_OS_ARCH`，对应字段 `app_release_id`、`target_os`、`arch`

## 说明

1. `target_os` 的值为：`01` 表示 `Unknown`，`02` 表示 `Android`，`03` 表示 `Bitrig`，`04` 表示 `CloudABI`，`05` 表示 `Dragonfly`，`06` 表示 `Emscripten`，`07` 表示 `FreeBSD`，`08` 表示 `Fuchsia`，`09` 表示 `Haiku`，`10` 表示 `iOS`，`11` 表示 `Linux`，`12` 表示 `MacOS`，`13` 表示 `NetBSD`，`14` 表示 `OpenBSD`，`15` 表示 `Redox`，`16` 表示 `Solaris`，`17` 表示 `Windows`，`99` 表示 `Any`
2. `arch` 的值为：`01` 表示 `Unknown`，`02` 表示 `X86_64`，`03` 表示 `X86`，`04` 表示 `WASM32`，`05` 表示 `SPARC64`，`06` 表示 `S390X`，`07` 表示 `RISCV`，`08` 表示 `POWERPC64`，`09` 表示 `POWERPC`，`10` 表示 `MSP430`，`11` 表示 `MIPS64`，`12` 表示 `MIPS`，`13` 表示 `ASMJS`，`14` 表示 `ARM`，`15` 表示 `AARCH64`，`99` 表示 `Any`
3. linux 系统的文件名最大长度是255，windows 系统的文件名最大长度是260，所以 `file_name` 的长度是255
4. 区别：`target_os` 的值为 `linux`，但 `os_type` 的值可具体到 `ubuntu`
5. 如果是跨平台，则 `target_os` 值为 `Any`
6. `file_path` 中存的是相对路径，如 maven 生成的文件路径中不包含 maven 仓库的根路径，自动上传的文件路径中不包含存平台数据的根路径以及 apps 文件夹
7. 如果是使用 maven 自动构建的文件，则使用 maven 自动生成的文件名
