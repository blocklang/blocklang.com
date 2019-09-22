# Block Lang

[![Build Status](https://travis-ci.org/blocklang/blocklang.com.svg?branch=master)](https://travis-ci.org/blocklang/blocklang.com)

Block Lang 尚在热火朝天、天马行空的开发，不适合在生产环境使用。君欲一睹 Block Lang 芳容，请移步[演示站点 https://blocklang.com](https://blocklang.com)。

## 相信

1. **老少皆宜**：每个人都可按照自己的需求，拼装出称心的软件
2. **人软合一**：每个人都需自定制生活、学习和工作用的软件套件

## 理念

BlockLang 致力于打造一朵“百花齐放、百鸟争鸣”的软件云，实现软件定义软件。

![Block Lang Idea](docs/spec/images/blocklang-idea.png)

## 定位

Block Lang 是一个：

1. 可视化的软件拼装工厂，致力于提高软件研发、运维的效率和质量；
2. 组件商店，提供功能全面、质量上乘、持续优化的组件和软件。

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

## 项目

BlockLang 平台由以下项目组成：

* [blocklang/blocklang.com](https://github.com/blocklang/blocklang.com) - BlockLang 平台
* [blocklang/installer](https://github.com/blocklang/blocklang-installer) - 一款自动化部署工具，专用于部署 BlockLang 平台中发布的 Spring Boot 项目
* [blocklang/blocklang-template](https://github.com/blocklang/blocklang-template) - BlockLang 平台默认的项目模板
* [blocklang/page-designer](https://github.com/blocklang/page-designer) - 可视化的页面设计器
* [blocklang/designer-core](https://github.com/blocklang/designer-core) - 存放设计器的通用功能

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

诚邀志同道合的编程**手艺人**加入（QQ群 `619312757`）。
