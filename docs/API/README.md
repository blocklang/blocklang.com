# REST API

## 约定

1. 用 servlet 名称作为文件夹名称，将 servlet 名相同的 API 放在同一个 API 下
2. 在 servlet 文件夹下可创建分组，对应不同层级的服务，但尽量将目录控制在两级
3. 文件名和 url 名称一律小写，如果出现多个单词，则用中划线隔开，如 `check-name`

## 市场

1. [获取组件仓库列表](./marketplace/list-component-repos.md)
2. [发布一个组件仓库](./marketplace/create-a-component-repo.md)
3. [获取一个组件库发布任务](./marketplace/get-a-publish-task.md)
4. [获取一个组件库发布任务的日志](./marketplace/get-a-publish-task-log.md)
5. [获取一个组件仓库的版本列表](./marketplace/list-component-repo-versions.md)

## 仓库

一个仓库中可存放多个软件项目

1. [校验仓库名称](./repositories/repository/check-name.md)
2. [创建仓库](./repositories/repository/create-a-repository.md)

## 软件项目

1. [获取资源的父分组列表](./repositories/repository/list-parent-group.md)
2. [查看仓库的目录结构](./repositories/repository/get-a-group.md)
3. [查看仓库中的一个页面](./repositories/repository/get-a-page.md)
4. [查看项目依赖资源的基本信息](./repositories/repository/project/get-a-project-dependency.md)
5. [获取仓库的 README 文件](./repositories/repository/get-the-readme.md)
6. [查看一个仓库信息](./repositories/repository/get-a-repository.md)
7. [获取最近提交信息](./repositories/repository/get-latest-commit.md)
8.  [为项目添加一个依赖](./repositories/repository/project/add-a-dependency.md)
9.  [删除项目的一个依赖](./repositories/repository/project/delete-a-dependency.md)
10. [获取项目的依赖列表](./repositories/repository/project/list-project-dependences.md)
11. [更新项目依赖的版本信息](./repositories/repository/project/update-dependence-version.md)
12. [获取部署配置信息](./repositories/deploy/get-deploy-setting.md)

## 资源

1. [校验页面的 key 值](./repositories/resource/page/check-key.md)
2. [校验页面的显示名](./repositories/resource/page/check-name.md)
3. [创建页面](./repositories/resource/page/create-a-page.md)
4. [校验分组的 key 值](./repositories/resource/group/check-key.md)
5. [校验分组的显示名](./repositories/resource/group/check-name.md)
6. [创建分组](./repositories/resource/group/create-a-group.md)

### 页面设计器

1. [获取页面模型](./repositories/designer/page/get-a-page-model.md)
2. [保存页面模型](./repositories/designer/page/update-a-page-model.md)
3. [获取项目可用的 Widget 列表](./repositories/designer/page/list-project-widgets.md)
4. [获取项目可用的 Service 列表](./repositories/designer/page/list-project-services.md)
5. [获取项目的 IDE 依赖列表](./repositories/designer/page/list-project-ide-dependences.md)
6. [获取 IDE 项目资源](./repositories/designer/page/get-a-ide-asset.md)

### 数据服务设计器

## 提交

1. [获取有变动的资源](./repositories/commit/list-changes.md)
2. [暂存更改](./repositories/commit/stage-changes.md)
3. [撤销暂存的更改](./repositories/commit/unstage-changes.md)
4. [提交更改](./repositories/commit/commit-changes.md)

## APP

1. [获取 JDK 列表](./apps/list-jdks.md)

## 软件发布服务

1. [校验发布版本号](./repositories/release/check-version.md)
2. [发布软件](./repositories/release/create-a-release.md)
3. [获取软件的一个发行版信息](./repositories/release/get-a-release.md)
4. [获取软件的一个发行版的发布日志](./repositories/release/get-a-release-log.md)
5. [获取项目的发布列表](./repositories/release/list-releases-for-a-project.md)
6. [获取项目的发布数](./repositories/release/get-release-count-for-a-project.md)

## 软件安装服务

1. [注册和更新项目信息](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/01_installers.md)
2. [下载软件](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/02_apps.md)

## 用户服务

1. [校验用户登录名](./user/check-login-name.md)
2. [完善用户信息](./user/complete-user-info.md)
3. [获取登录用户简要信息](./user/get-the-authenticated-user.md)
4. [获取登录用户详细信息](./user/get-user-profile.md)
5. [查看我的仓库](./user/list-my-repositories.md)
6. [获取登录用户发布的组件库](./user/list-my-component-repos.md)
7. [获取登录用户正在运行的组件库发布任务](./user/list-my-component-repo-publishing-tasks.md)
8. [修改用户信息](./user/update-user-profile.md)

## 教程

1. [查看一个文档](./docs/get-a-document.md)
2. [获取文档中的一个图片](./docs/get-a-image.md)

## 信息分类编码

1. [获取软件类型](./properties/app-type.md)
