# REST API

## 约定

1. 用 servlet 名称作为文件夹名称，将 servlet 名相同的 API 放在同一个 API 下
2. 在 servlet 文件夹下可创建分组，对应不同层级的服务，但尽量将目录控制在两级
3. 文件名和 url 名称一律小写，如果出现多个单词，则用中划线隔开，如 `check-name`

## 项目

1. [校验项目名称](./projects/project/check-name.md)
2. [创建项目](./projects/project/create-a-project.md)
3. [查看项目的目录结构](./projects/project/get-a-tree.md)
4. [查看项目中的一个对象](./projects/project/get-a-blob.md)
5. [获取项目的 README 文件](./projects/project/get-the-readme.md)
6. [查看一个项目](./projects/project/get-a-project.md)
7. [获取最近提交信息](./projects/project/get-latest-commit.md)
8. [获取部署配置信息](./projects/deploy/get-deploy-setting.md)

## APP

1. [获取 JDK 列表](./apps/list-jdks.md)

## 软件发布服务

1. [校验发布版本号](./projects/release/check-version.md)
2. [发布软件](./projects/release/create-a-release.md)
3. [获取项目的发布列表](./projects/release/list-releases-for-a-project.md)
4. [获取项目的发布数](./projects/release/get-release-count-for-a-project.md)

## 软件安装服务

1. [注册和更新项目信息](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/01_installers.md)
2. [下载软件](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/02_apps.md)

## 用户服务

1. [获取登录用户简要信息](./user/get-the-authenticated-user.md)
2. [获取登录用户详细信息](./user/get-user-profile.md)
3. [查看我的项目](./user/list-my-projects.md)
4. [修改用户信息](./user/update-user-profile.md)

## 教程

1. [查看一个文档](./docs/get-a-document.md)
2. [获取文档中的一个图片](./docs/get-a-image.md)
