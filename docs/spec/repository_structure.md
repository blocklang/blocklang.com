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

要包含 appType 信息

```json
{

}
```

#### `{page_key}.json`

```json
{
    "pageInfo": {},
    
}
```


### `sources`


## `marketplace` 目录

```text

```

## `apps` 目录