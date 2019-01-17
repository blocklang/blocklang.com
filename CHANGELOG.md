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

## TODO

1. 学习 Spring boot cache
2. 实现注销 installer 的 REST API
3. 实现下载软件的 REST API