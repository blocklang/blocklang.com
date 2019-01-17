# BlockLang Release

[![Build Status](https://travis-ci.org/blocklang/blocklang-release.svg?branch=master)](https://travis-ci.org/blocklang/blocklang-release)
[![codecov](https://codecov.io/gh/blocklang/blocklang-release/branch/master/graph/badge.svg)](https://codecov.io/gh/blocklang/blocklang-release)

Block Lang 软件发布中心。功能包括 dojo 项目和 Spring Boot 项目的配置、编译和构建，以及提供 JDK 和 Spring Boot Jar 的下载服务。

## REST API

1. [注册和更新项目信息](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/01_installers.md)
2. [下载软件](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/02_apps.md)

## 项目结构

### Server

服务器端基于 Spring Boot 开发，使用以下组件：

1. Spring MVC - 开发 web 端
2. Thymeleaf - HTML 模板引擎
3. Hibernate JPA - 数据库交互组件
4. Liquibase - 管理和重构数据库表结构，以及管理系统初始数据
5. PostgreSQL - 数据库

#### `src/main/java` 目录

1. `controller` - 存放本项目专用的 HTTP 服务
2. `api` - 存放共享的 HTTP 服务
3. `service` - 存放业务逻辑接口
4. `service.impl` - 存放业务逻辑实现类
5. `dao` - 存放数据访问接口
6. `dao.impl` - 存放数据访问实现类
7. `model` - 存放实体对象
8. `data` - 存放 POJO 对象，是对 model 实体对象的组合

#### `src/main/resources` 目录

1. `db/changelog/data` - 存放初始化数据
2. `db/changelog/table` - 存放变更的表脚本
3. `db/db.sql` - 存放建库脚本

### Client

客户端基于最新版 [Dojo](https://dojo.io/) 开发。

## 需求描述

详见[需求](docs/README.md)。

## 数据库表结构

1. 设计数据库表时常用的[文档模板](docs/db/TEMPLATE.md)；
2. [数据库表结构](docs/db/README.md)。
