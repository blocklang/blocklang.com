# `COMPONENT_REPO` - 登记组件仓库信息

统一登记市场中发布的组件库，存储 git 仓库的基本信息。

如果组件库发布失败，则不允许登记。即此表只能登记发布成功的组件库。

## 字段

| 字段名            | 注释           | 类型     | 长度 | 默认值 | 主键 | 可空 |
| ----------------- | -------------- | -------- | ---- | ------ | ---- | ---- |
| dbid              | 主键           | int      |      |        | 是   | 否   |
| git_repo_url      | git 仓库地址   | varchar  | 128  |        |      | 否   |
| git_repo_website  | git 仓库网站   | varchar  | 32   |        |      | 否   |
| git_repo_owner    | git 仓库拥有者 | varchar  | 64   |        |      | 否   |
| git_repo_name     | git 仓库名称   | varchar  | 64   |        |      | 否   |
| category          | 组件库分类     | char     | 2    |        |      | 否   |
| repo_type         | 仓库类型       | char     | 2    |        |      | 否   |
| last_publish_time | 最近发布时间   | datetime |      |        |      | 否   |

## 约束

* 主键：`PK_COMPONENT_REPO`
* 外键：(*未设置*)
* 索引：`UK_COMP_REPO_ON_GIT_REPO_URL_USER_ID`，对应字段 `git_repo_url`、`create_user_id`

## 说明

1. `git_repo_url` 只支持 `https` 协议的 url
2. `git_repo_website` 的值从 `git_repo_url` 中截取，如 `github`、`gitlab`、`gitee` 等
3. `version` 指仓库的最新版本号
4. `name`、`version`、`label`、`description`、`logo_path` 和 `category` 的值是从项目根目录下的 `component.json` 文件里获取的
5. `category` 的值：`01` 表示 `Widget`，`02` 表示 `Service`，`03` 表示 `WebAPI`，`99` 表示 `Unknown`
6. `repo_type` 的值为: `02` 表示 `IDE`，`03` 表示 `PROD`(此表不存储 `01`，即 `API` 仓库信息)
7. 登记仓库时间对应的字段是 `create_time`，当有新版本出现时，可能会发布新版内容，`last_publish_time` 中存的就是最近发布的时间
8. 为了避免有人在市场中抢注仓库名，使用 `@{createUserId}/{git_repo_url}` 的形式唯一定位一个组件库
9.  一个组件仓库中只能存一类组件
10. `repo_type` 的值为 `IDE` 时，在可视化的设计器中，需要为 API 仓库编写 IDE 专用的组件，如一个 `TextInput` 部件，在 IDE 中就需要提供展示 `TextInput` 部件的属性和事件的面板，在 IDE 扩展库中就是存储这些组件

## changelog

1. 删除 `api_repo_id` 字段，只在 `component_repo_version` 表中存 `api_version_id`，这样就支持在 `blocklang.json` 文件中修改 `api.git`
2. 删除字段 `is_std` 是否 blocklang 标准库，类似于 rust 的 prelude，会默认为每个项目引入标准库，标准库需要在组件市场中注册，但不能向没有系统级权限的用户。此字段放在此处可能不合适，是不是标准库，不是由用户指定，而是由平台选中。
3. 删除字段 `is_ide_extension` 改为 `repo_type`
