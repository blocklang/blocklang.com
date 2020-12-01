# 数据服务

对应 RESTful API。

支持三种 RESTful API 集成方式：

1. 与已部署好的 API 集成；
2. 与包含 API 的 jar 包集成，jar 包中包含 controller 类（归到如何集成 jar 包？）；
3. 与在 BlockLang 平台上定义的 API 集成
   1. 基于已定义的 API 进行二次封装
   2. 或者从头开始定义的 API

第一和第二种是通过 git 仓库实现的，第三种是直接在 BlockLang 平台定义的。

Service 仓库只提供一个 API 仓库，不需要提供 IDE 版和 PROD 版。但需要在部署时，为 Service 仓库指定一个 `host` 和 `basePath` 属性。这样就可以与实现关联起来。而在开发阶段，通常不能直接连到生产环境，我们可以准备一个开发环境或者提供一版 mock 实现。

如果在 IDE 版仓库中提供 mock 实现，感觉为使用者带来了更多的工作、限制了实现的灵活度，也与目前常用的工作方式不一致，因为完全可以通过部署一套开发环境来解决此问题；即使在设计阶段需要提供 mock 实现，也可以将 mock 实现直接发布为一个服务。所以：

1. Service 仓库只有一版，其中存放 OpenApi，命名规范为 `service-{name}`
2. 在开发阶段，如果没有提供 mock 实现，则统一使用空值
3. 在开发阶段，如果提供了发布好的 mock 实现或开发环境，则能通过配置 `host` 和 `basePath` 连接上
4. 在部署阶段，能通过配置 `host` 和 `basePath` 连接上生产环境

有关 `host` 和 `basePath` 的配置

1. 在开发阶段，在 `DEPENDENCY.json` 文件中配置（不要放在 git 仓库中配置，因为 git 仓库中存的是规范，而这两个参数应归到实例范围）
2. 在部署阶段，在部署面板中配置
3. `basePath` 的默认值为 `/`

因此在设计如何存储项目的配置信息时，既要考虑部署阶段的配置，也要考虑开发阶段的配置。

要在 `blocklang` 下创建一个 `service-demo` 仓库，用作示例仓库。

有关 RESTful API 的定义，最好要兼容 [OpenAPI 3](http://spec.openapis.org/oas/v3.0.3) 规范。

既要支持在一个版本中调整 API，即依赖不稳定版本；也支持升级版本。是否需要追踪每个字段的变更历史？
