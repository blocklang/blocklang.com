# `WEB_SERVER` - 应用服务器信息

## 字段

| 字段名       | 注释           | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ------------ | -------------- | ------- | ---- | ------ | ---- | ---- |
| dbid         | 主键           | int     |      |        | 是   | 否   |
| server_token | 服务器 token   | varchar | 50   |        |      | 否   |
| ip           | ip 地址        | varchar | 50   |        |      | 否   |
| os_type      | 操作系统类型   | char    | 2    | 99     |      | 否   |
| os_version   | 操作系统版本号 | varchar | 32   |        |      | 否   |
| arch         | CPU 架构       | char    | 2    | 99     |      | 否   |
| user_id      | 所属用户标识   | int     |      |        |      | 否   |

## 约束

* 主键：`PK_WEB_SERVER`
* 外键：无
* 索引：`UK_SERVER_TOKEN`，对应字段 `server_token`

## 说明

1. `os_type` 的值为：`01` 表示 `Unknown`，`02` 表示 `Android`，`03` 表示 `Emscripten`，`04` 表示 `Linux`，`05` 表示 `Redhat`，`06` 表示 `Ubuntu`，`07` 表示 `Debian`，`08` 表示 `Arch`，`09` 表示 `Centos`，`10` 表示 `Fedora`，`11` 表示 `Alpine`，`12` 表示 `Macos`，`13` 表示 `Redox`，`14` 表示 `Windows`，`99` 表示 `Any`
2. `arch` 是编码字段
3. 注意，比较 `os_type` 时要忽略大小写
4. `server_token` 的值取服务器的 MAC 地址
