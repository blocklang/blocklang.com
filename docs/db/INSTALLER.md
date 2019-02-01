# `INSTALLER` - APP 安装器信息

## 字段

| 字段名          | 注释           | 类型 | 长度 | 默认值 | 主键 | 可空 |
| --------------- | -------------- | ---- | ---- | ------ | ---- | ---- |
| dbid            | 主键           | int  |      |        | 是   | 否   |
| web_server_id   | 应用服务器标识 | int  |      |        |      | 否   |
| app_release_id  | app 发行版标识 | int  |      |        |      | 否   |
| app_run_port    | app 运行端口   | int  |      | 80     |      | 否   |
| installer_token | 安装器 token   | char | 22   |        |      | 否   |

## 约束

* 主键：`PK_INSTALLER`
* 外键：(*未设置*)`FK_WEB_SERVER`，`web_server_id` 对应 `WEB_SERVER` 的 `dbid`；`FK_APP_RELEASE` 对应 `APP_RELEASE` 的 `dbid`
* 索引：`UK_INSTALLER_TOKEN`，对应字段 `installer_token`

## 说明

1. `installer_token` 是22位的 UUID