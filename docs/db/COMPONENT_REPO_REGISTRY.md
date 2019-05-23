# `COMPONENT_REPO_REGISTRY` - 登记组件仓库信息

统一登记市场中发布的组件库，存储 git 仓库的基本信息。

## 字段

| 字段名           | 注释               | 类型    | 长度 | 默认值 | 主键 | 可空 |
| ---------------- | ------------------ | ------- | ---- | ------ | ---- | ---- |
| dbid             | 主键               | int     |      |        | 是   | 否   |
| git_repo_url     | git 仓库地址       | varchar | 128  |        |      | 否   |
| git_repo_website | git 仓库网站       | varchar | 32   |        |      | 否   |
| git_repo_owner   | git 仓库拥有者     | varchar | 64   |        |      | 否   |
| git_repo_name    | git 仓库名称       | varchar | 64   |        |      | 否   |
| name             | 组件库的名称       | varchar | 64   |        |      | 否   |
| version          | 组件库的版本号     | varchar | 32   |        |      | 否   |
| label            | 组件库的显示名     | varchar | 64   |        |      | 是   |
| description      | 组件库的详细说明   | varchar | 512  |        |      | 是   |
| logo_path        | 项目 Logo 存储路径 | varchar | 64   |        |      | 是   |
| category         | 组件库分类         | char    | 2   |        |      | 否   |

## 约束

* 主键：`PK_COMPONENT_REPO_REGISTRY`
* 外键：无
* 索引：`UK_COMPONENT_REPO_REGISTRY_ON_GIT_REPO_URL`，对应字段 `git_repo_url`；`IDX_COMPONENT_REPO_REGISTRY_ON_NAME`(不是唯一索引)，对应字段 `name`

## 说明

1. `git_repo_url` 只支持 `https` 协议的 url
2. `git_repo_website` 的值从 `git_repo_url` 中截取，如 `github`、`gitlab`、`gitee` 等
3. `version` 指仓库的最新版本号
4. `name`、`version`、`label`、`description`、`logo_path` 和 `category` 的值是从项目根目录下的 `package.json` 文件里获取的
5. `category` 的值：`01` 表示 `Widget`，`02` 表示 `Client API`，`03` 表示 `Server API`
6. 注意：**一个组件仓库中只能存一类组件**
