# 仓库结构

一个仓库中包含多个项目。每一种项目的存储结构各不相同。

`BlockLang` 是根目录，下有三个目录，存放三类资源：

1. `repositories` - 用户创建的仓库
2. `marketplace` - 组件市场
3. `apps` - 平台使用到的软件

## `repositories` 目录

`repositories` 目录下包含三个目录：

1. `templates` - 模板项目代码，可在用户项目中复用
2. `models` - 存储仓库中所有项目的模型信息
3. `sources` - 根据 `models` 中的模型信息生成源代码

当是 spring boot 项目时，会将构建的 jar 文件发布到本地的 maven 仓库中。

```text
templates/                                                                                 - 存放模板源码项目
    TODO: 支持多个模板项目
models/                                                                                    - 存放项目模型数据
    {owner}/                                                                               - 仓库拥有者的登录名
        {repository_name}/                                                                 - 仓库名
            .git/                                                                          - git 托管
            README.md                                                                      - 仓库介绍文档
            BUILD.json                                                                     - 仓库构建配置信息
            {project_name}/                                                                - 项目名
                PROJECT.json                                                               - 项目基本信息
                {group_key}/                                                               - 分组名
                    {page_key}.json                                                        - 页面模型信息，包含页面基本信息
                {page_key}.json                                                            - 页面模型数据
sources/                                                                                   - 存放项目源码（根据模型生成源码）
    {owner}/                                                                               - 仓库拥有者的登录名
        {repository_name}/                                                                 - 仓库名
            {project_name}/                                                                - 项目名（以下是**小程序**目录结构）
                source/                                                                    - 源码
                    {buildTarget}/                                                         - 要构建的项目类型
                        {profile}/                                                         - 配置依赖和发布目标
                            .git/                                                          - git 仓库
                            RELEASE.json                                                   - 存储当前源码的构建位置，是项目目录的 commit id
                            {app.js}                                                       - 代表项目源码文件
                buildLogs/                                                                 - 构建日志
                    {buildTarget}/                                                         - 要构建的项目类型
                        {profile}/                                                         - 配置依赖和发布目标
                            {version}-{yyyy_MM_dd_HH_mm_ss}-{git short commit id}.log      - 日志文件
```

### `models`

#### `BUILD.json`

```json
{

}
```

#### `PROJECT.json`

本文件用于在 git 仓库中存储项目基本信息，对应在仓库资源表中存储的项目基本信息。所以不需要再在项目资源表中存储 `PROJECT.json` 文件。

结构说明

1. id `number` - 项目标识
2. key `string` - 项目的 key 值
3. label `string` - 项目的显示名
4. appType `string` - 项目对应的 app 类型，如小程序等
5. version `string` - 模型的版本号，默认为 "master"

示例

```json
{
  "id": 1,
  "key": "",
  "label": "",
  "appType": "",
  "version": ""
}
```

#### `{page_key}.json`

```json
{
    "pageInfo": {
        "id": 1,
        "key": "",
        "label": "",
        "resourceType": "03"
    },
    "widgets": [],
    "data": [],
    "functions": []
}
```

当 `resourceType` 的值为 `03` 时，表示页面。

### `sources`


## `marketplace` 目录

```text

```

## `apps` 目录
