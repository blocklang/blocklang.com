# `COMPONENT_REPO` - 登记组件仓库信息

统一登记市场中发布的组件库，存储 git 仓库的基本信息。

## 字段

| 字段名            | 注释               | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ----------------- | ------------------ | -------- | ---- | ------ | ---- | ---- |
| dbid              | 主键               | int      |      |        | 是   | 否   |
| api_repo_id       | api 仓库标识       | int      |      |        |      | 否   |
| git_repo_url      | git 仓库地址       | varchar  | 128  |        |      | 否   |
| git_repo_website  | git 仓库网站       | varchar  | 32   |        |      | 否   |
| git_repo_owner    | git 仓库拥有者     | varchar  | 64   |        |      | 否   |
| git_repo_name     | git 仓库名称       | varchar  | 64   |        |      | 否   |
| name              | 组件库的名称       | varchar  | 64   |        |      | 否   |
| version           | 组件库的最新版本号 | varchar  | 32   |        |      | 否   |
| label             | 组件库的显示名     | varchar  | 64   |        |      | 是   |
| description       | 组件库的详细说明   | varchar  | 512  |        |      | 是   |
| logo_path         | 项目 Logo 存储路径 | varchar  | 64   |        |      | 是   |
| category          | 组件库分类         | char     | 2    |        |      | 否   |
| language          | 编程语言           | varchar  | 32   |        |      | 否   |
| last_publish_time | 最近发布时间       | datetime |      |        |      | 是   |

## 约束

* 主键：`PK_COMPONENT_REPO`
* 外键：无
* 索引：`UK_COMP_REPO_ON_GIT_REPO_URL_USER_ID`，对应字段 `git_repo_url`、`create_user_id`；`UK_COMP_REPO_ON_NAME_USER_ID`，对应字段 `name`、`create_user_id`

## 说明

1. `api_repo_id` 指组件库实现的 API 库的标识
2. `git_repo_url` 只支持 `https` 协议的 url
3. `git_repo_website` 的值从 `git_repo_url` 中截取，如 `github`、`gitlab`、`gitee` 等
4. `version` 指仓库的最新版本号
5. `name`、`version`、`label`、`description`、`logo_path` 和 `category` 的值是从项目根目录下的 `component.json` 文件里获取的
6. `category` 的值：`01` 表示 `Widget`，`02` 表示 `Client API`，`03` 表示 `Server API`，`99` 表示 `Unknown`
7. 登记仓库时间对应的字段是 `create_time`，当有新版本出现时，可能会发布新版内容，`last_publish_time` 中存的就是最近发布的时间
8. 为了避免有人在市场中抢注仓库名，使用 `@{publisher}/{repo}` 的形式唯一定位一个组件库
9. 一个组件仓库中只能存一类组件