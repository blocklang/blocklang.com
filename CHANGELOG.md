# Changelog

## 2019-01-04

1. 安装 PostgreSQL 数据库
2. 创建 blocklang 数据库
3. 配置 PostgreSQL 数据库连接
4. 配置 liquibase
5. 创建 Java 项目结构

## 2019-01-05

1. 编写需求文档，包括发布相关的术语和自动化发布流程

## 2019-01-10

1. 设计 `APP`、`APP_RELEASE`、`APP_RELEASE_RELATION`、`APP_RELEASE_FILE` 数据库表

## 2019-01-11

1. 设计 `WEB_SERVER`、`INSTALLER` 数据库表

## 2019-01-13

1. 集成日志系统
2. 集成 i18n 国际化功能
3. 支持 Bean Validation
4. 支持自动化测试 WebMVC

## 2019-01-14

1. 完成注册 installer 的 API 层 9 个测试用例和源代码的编写
2. 支持 travis-ci
3. 编写 service 层测试用例和实现类

## 2019-01-15

1. 实现 `AppReleaseService` 业务逻辑接口

## 2019-01-16

1. 实现 `AppReleaseRelationService` 业务逻辑接口
2. 实现 `AppReleaseFileService` 业务逻辑接口
3. 实现 `InstallerService` 业务逻辑接口

## 2019-01-17

1. 实现升级 APP 的 REST API
2. 支持在 travis-ci 上运行测试用例

## 2019-01-18

1. 实现注销 installer 的 REST API
2. 实现下载软件的 REST API

## 2019-01-22

1. 实现 MavenInstall 命令
2. 实现 NpmInstall 命令
3. 实现 DojoBuild 命令

## 2019-01-23

1. 实现 ClientDistCopy 命令

## 2019-01-24

1. 重构 build 相关的 Task 类
2. 将 build 过程中日志都写到一个文件中

## 2019-01-26

1. 调整项目结构，从发布中心提升为 block lang 平台

## 2019-01-27

1. 增加一个存储 build 信息的表，用来记录 build 是否成功
2. 设计 GIT_TAG 和 BUILD 表结构
3. 实现 GitTag 命令

## 2019-01-29

1. 重构 `AppBuildContext` 类
2. 创建 `PROJECT_RELEASE_TASK` 数据库表

## 2019-01-30

1. 完善 releases.md 文档
2. 完成 ReleaseController 的 newRelease 方法
3. 创建业务逻辑层的实现类，解决 travis-ci 找不到实现 bean 的问题
4. 实现 `ProjectTagService` 接口
5. 实现 `ProjectReleaseTaskService` 接口

## 2019-01-31

1. 完成 `BuildService#build` 的开发

## 2019-02-01

1. 新增 git clone 帮助方法
2. 支持从 github 或 gitee 下载项目模板
3. 支持将项目模板复制到指定的项目下
4. 拆分表结构文档，让一个文档存一张表，然后在 README 中分类维护表清单

## 2019-02-02

1. 将系统参数移到全局的 `Config` 文件中

## 2019-02-03

1. 设计 CM_PROPERTY 表结构
2. 将公共的代码移到 `core` 包中，而不是放在顶级 package 下
3. 将 git 和 util 包移到 `core` 包中（`core` 包中的内容意味着是公共类）
4. 实现 `propertyService#findStringValue`
5. 集成缓存功能，并应用到 `propertyService#findStringValue` 上
6. 将 `Config` 中的系统参数存到数据库表中，并删除 `Config` 类

## 2019-02-07

1. 集成 Github 登录功能
2. 支持从环境变量中读取敏感信息
3. 学习 dojo 的 realworld 项目，了解其中如何控制登录权限
4. 第一版使用 bootstrap 默认样式，以便快速构建出能用的模块，后续使用 google material

## 2019-02-08

1. 设计用户表
2. 改为使用 oauth2-client 集成 github 登录
3. 在页面中一律使用 `Block Lang`，不再使用 `块语言`

## 2019-02-09

1. 支持新增第三方用户数据

## 2019-02-10

1. 重构新增第三方用户数据的方法
2. 支持更新第三方用户数据
3. 确认为什么用户登录后，页面右上角没有显示登录用户的信息
4. 编写 `user` API 文档

## 2019-02-11

1. 编写 `user` API 测试用例

## 2019-02-12

1. client: 扩展 Home 部件，当用户未登录时显示公共首页，当用户已登录时显示个人首页
2. 完成注销用户功能
3. 调整项目结构，将需求文档放到 `spec` 文件夹中

## 2019-02-13

1. doc: 编写创建项目文档
2. client: 新增创建项目部件
3. client: 支持国际化

## 2019-02-15

1. 解决添加了 spring security 和 oauth2 后测试用例运行失败的问题，注意只有在运行测试的时候才会报错
2. 学习 Spring Boot 的异步方法，并应用到 build 服务中
3. 学习 Spring boot cache
4. 编写校验项目名称有效性的 controller 及测试用例

## 2019-02-16

1. 当使用 StateHistory 进入一个页面后，将加载的资源文件中多余的路径去掉
2. 当使用浏览器的 F5 刷新任一页面时，都跳转到首页

## 2019-02-17

1. 编写创建项目文档

## 2019-02-18

1. 实现创建项目 controller
2. 实现 `ProjectService` 接口

## 2019-02-19

1. 实现新增项目成功后，跳转到项目详情页面
2. 调整使用 `@WithMockUser` 的测试用例

## 2019-02-20

1. 设计 `PROJECT_RESOURCE` 数据库表
2. 设计 `PROJECT_FILE` 数据库表
3. 支持为项目添加 README.md 文件
4. 支持为项目添加 Main 入口模块

## 2019-02-21

1. 开发并测试获取项目结构的 REST API
2. 实现 `ProjectResourceService#findChildren` 方法
3. 测试用例中的 assertThat 完全迁移到 assertj

## 2019-02-22

1. 编写获取 README 内容的 REST API 文档
2. 实现获取 README 内容的 REST API

## 2019-02-23

1. 实现查询用户有权访问的项目 REST API

## 2019-02-24

1. Client: 创建个人首页中的“我能访问的项目”卡片

## 2019-02-26

1. 新增 MarkdownPreview 部件

## 2019-02-27

1. 支持获取仓库的最近提交信息

## 2019-02-28

1. 新增从仓库中逐层获取文件及提交信息的 API
2. Client: 创建查看项目页面
3. 为 ProjectResource 扩展 icon 和 resourceType 等字段

## 2019-03-04

1. 优化部署相关的数据库表结构，支持用户部署任何有权访问的项目

## 2019-03-05

1. 调整代码支持用户部署任何有权访问的项目

## 2019-03-08

1. 如果用户未登录，则让部署按钮失效
2. 登录用户点击部署按钮后，才延迟加载部署设置信息

## TODO

1. 项目的目录结构，为了便于快速定位文件或切换，可参考 windows 文件资源管理器的设计，左侧和顶部的导航设计
2. 也参考 eclipse 的文件和目录的关联，实现快速定位目录
3. github 的目录结构设计虽然简洁，但是浏览文件时，感觉还是颇多不方便
4. 查询系统支持的所有 JDK 版本
5. 日志详细记录到对应的日志文件中，然后分别开发历史日志读取服务和实时日志跟踪服务
6. 编写登记项目发布信息 API，采用异步方法
7. 编写获取项目发布日志 API
8. 编写实时读取发布日志 API，采用 websocket
9. 考虑为 BuildService#build 方法添加全面的自动化测试用例
10. 搭建测试环境
11. 决定开发单页面应用，要坚持积累组件
12. 使用 google material 重新布局界面
13. 将 github 等第三方网站的 client-id 和 client-security 存到数据库中
14. 使用 SPA 后，怎么控制客户端的权限问题，如怎么控制在用户未登录时，输入 `projects/new` 后不显示创建项目页面
15. client: 将 HashHistory 修改为 StateHistory，但要在服务器端重写获取 js 和 css 文件的方法
16. 使用 github 登录失败后，页面链接变为 `?error`，但仍停留在公共首页，没有提示
17. 编写快速入门文档，发布到百度经验、开源中国等网站 (*)
18. 录制快速入门视频 (*)
19. 第一次发布：完成项目的创建、发布和安装，并编写使用手册和快速入门文档
20. 支持多人协作维护同一个项目
21. 添加应用模板功能
22. 之前的设计中，pane 应该是 program 的子节点，但这样不利于共用，是否可考虑与 program 放在同一层级？或者将 pane 看作用户自定义部件？
23. 支持实时编辑
24. <https://blocklang.com/blocklang> 这个用户名要平台专用
25. 【bug】经过 route 跳转到一个页面后，需要点击三次才能回退到上一个页面
26. 支持在个人首页的项目 card 中添加过滤功能，即添加一个搜索框
27. 使用 dojo 路由后，可提供用户友好的 url，不再需要在服务器端的 filter 中做处理
28. 用不同的颜色标识出哪些是登录人的操作，哪些不是
29. 为 Moment 部件添加全局定时器
30. 扩展 Moment 部件，让时间间隔超过一定时间后，不再以多久之前的方式显示，而是直接显示时间
31. 添加 404 和 500 页面
32. 帮助文档和培训服务
33. 支持 docker 安装
34. 添加将文本复制到粘贴板的组件
35. 将用户登录信息存到 sessionStorage 中
