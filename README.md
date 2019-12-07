# Block Lang

[![Build Status](https://travis-ci.org/blocklang/blocklang.com.svg?branch=master)](https://travis-ci.org/blocklang/blocklang.com)

> 信息化工作者对美好生活的向往，在工作中收获成就感和幸福感，就是 Block Lang 的奋斗目标。

Block Lang 是一个：

1. 业务人员友好的软件拼装工厂；
2. 开发人员友好的组件、API 和软件等共建、共享社区。

Block Lang 专注于业务层面，仅在现有软件开发模式之上增加一个可视化的拼装层。Block Lang 将开启**可持续发展**的软件开发新模式，实现“技术可积累，经验可沉淀，平台能力递增，研发成本递减”。

Block Lang 尚在热火朝天、天马行空的开发，不适合在生产环境使用。君欲一睹 Block Lang 芳容，请移步[演示站点 https://blocklang.com](https://blocklang.com)。

## 相信

1. **老少皆宜**：每个人都可按照自己的需求，拼装出称心的软件
2. **软件赋能**：每个人在生活、学习和工作中都需自定制的软件套件

## 理念

BlockLang 致力于打造一朵“百花齐放、百鸟争鸣”的软件云，实现软件定义软件。

![Block Lang Idea](docs/spec/images/blocklang-idea.png)

## 原理

BlockLang 认为：

1. 一切通用功能都可封装成组件；
2. 一切业务逻辑都可用组件拼装；
3. 软件生产过程应该所见即所得。

BlockLang 将软件开发拆分为两部分：

1. 一是通用组件的研发；
2. 二是基于通用组件拼装出满足业务的软件。

最终实现硬件、软件和生产过程一站式、全面云化。

![Block Lang Architecture](docs/spec/images/blocklang-architecture.png)

## 用户心声

> 这个平台会不会革程序员的命，引发程序员的失业潮？

多虑了，您品，您细品：

* 不但不是要革程序员的命，恰恰相反，是帮助程序员从重复、繁重且技术含量低的工作中解放出来，有精力、有时间专注于业务自身或专注于创新；
* 的确会代替程序员做一部分工作，但生产力提高了，并不就意味着失业，恰恰相反，正是成本的降低、质量的提升，会促进更多传统行业、更多徘徊的领域拥抱信息化，信息化需求会出现指数级增长，市场会需要更多专业程度更多的程序员。

> 一个项目中，能不能在使用 Block Lang 的拼装方式同时，也能使用传统的编程方式开发？

不建议，但可以做到。我们以一个完整的业务功能为单位：

* 如果一个完整的业务功能都用编程方式开发，则将代码放在模板项目中即可；
* 如果用编程方式开发部分业务组件或 API，则可以注册到组件市场中，供拼装时使用。

## 项目

BlockLang 平台由以下项目组成：

* [blocklang/blocklang.com](https://github.com/blocklang/blocklang.com) - BlockLang 平台
* [blocklang/installer](https://github.com/blocklang/blocklang-installer) - 一款自动化部署工具，专用于部署 BlockLang 平台中发布的 Spring Boot 项目
* [blocklang/blocklang-template](https://github.com/blocklang/blocklang-template) - BlockLang 平台默认的项目模板
* [blocklang/page-designer](https://github.com/blocklang/page-designer) - 可视化的页面设计器
* [blocklang/designer-core](https://github.com/blocklang/designer-core) - 存放设计器的通用功能
* [blocklang/codemods](https://github.com/blocklang/codemods) - 生成客户端代码工具

需要往 BlockLang 组件市场注册的项目：

* [blocklang/std-api-widget](https://github.com/blocklang/std-api-widget) - 标准库，存放 UI 组件的 API
* [blocklang/std-widget-web](https://github.com/blocklang/std-widget-web) - 标准库，存放 UI 组件
* [blocklang/std-ide-widget](https://github.com/blocklang/std-ide-widget) - 标准库，存放 UI 组件的 IDE 版
* [blocklang/api-widgets-bootstrap](https://github.com/blocklang/api-widgets-bootstrap) - 扩展库，存放 UI 组件的 API
* [blocklang/widgets-bootstrap](https://github.com/blocklang/widgets-bootstrap) - 扩展库，存放 UI 组件
* [blocklang/ide-widgets-bootstrap](https://github.com/blocklang/ide-widgets-bootstrap) - 扩展库，存放 UI 组件的 IDE 版

依赖的通用组件：

* [blocklang/dojo-fontawesome](https://github.com/blocklang/dojo-fontawesome) - 在 dojo 中使用 [fontawesome 5](https://fontawesome.com/) 字体图标
* [blocklang/bootstrap-classes](https://github.com/blocklang/bootstrap-classes) - 存放 [bootstrap 4](https://getbootstrap.com/) 的 css class 常量

## 贡献

在提交代码前，请先了解[为什么要开发 Block Lang](./docs/help/why.md) 以及[设计细节](./docs/spec/program.md)。

Block Lang 完全开源，诚邀志同道合的编程**手艺人**共筑社区（QQ群 `619312757`）。
