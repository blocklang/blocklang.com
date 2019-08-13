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

## 项目

1. [校验项目名称](./projects/project/check-name.md)
2. [创建项目](./projects/project/create-a-project.md)
3. [获取资源的父分组列表](./projects/project/list-parent-group.md)
4. [查看项目的目录结构](./projects/project/get-a-group.md)
5. [查看项目中的一个页面](./projects/project/get-a-page.md)
6. [查看项目依赖](./projects/project/list-dependences.md)
7. [获取项目的 README 文件](./projects/project/get-the-readme.md)
8. [查看一个项目](./projects/project/get-a-project.md)
9. [获取最近提交信息](./projects/project/get-latest-commit.md)
10. [为项目添加一个依赖](./projects/project/add-a-dependence.md)
11. [删除项目的一个依赖](./projects/project/delete-a-dependence.md)
12. [获取项目的依赖列表](./projects/project/list-project-dependences.md)
13. [更新项目依赖的版本信息](./projects/project/update-dependence-version.md)
14. [获取部署配置信息](./projects/deploy/get-deploy-setting.md)

## 资源

1. [校验页面的 key 值](./projects/resource/page/check-key.md)
2. [校验页面的显示名](./projects/resource/page/check-name.md)
3. [创建页面](./projects/resource/page/create-a-page.md)
4. [校验分组的 key 值](./projects/resource/group/check-key.md)
5. [校验分组的显示名](./projects/resource/group/check-name.md)
6. [创建分组](./projects/resource/group/create-a-group.md)

## 提交

1. [获取有变动的资源](./projects/commit/list-changes.md)
2. [暂存更改](./projects/commit/stage-changes.md)
3. [撤销暂存的更改](./projects/commit/unstage-changes.md)
4. [提交更改](./projects/commit/commit-changes.md)

## APP

1. [获取 JDK 列表](./apps/list-jdks.md)

## 软件发布服务

1. [校验发布版本号](./projects/release/check-version.md)
2. [发布软件](./projects/release/create-a-release.md)
3. [获取软件的一个发行版信息](./projects/release/get-a-release.md)
4. [获取软件的一个发行版的发布日志](./projects/release/get-a-release-log.md)
5. [获取项目的发布列表](./projects/release/list-releases-for-a-project.md)
6. [获取项目的发布数](./projects/release/get-release-count-for-a-project.md)

## 软件安装服务

1. [注册和更新项目信息](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/01_installers.md)
2. [下载软件](https://github.com/blocklang/blocklang-installer/blob/master/docs/API/02_apps.md)

## 用户服务

1. [校验用户登录名](./user/check-login-name.md)
2. [完善用户信息](./user/complete-user-info.md)
3. [获取登录用户简要信息](./user/get-the-authenticated-user.md)
4. [获取登录用户详细信息](./user/get-user-profile.md)
5. [查看我的项目](./user/list-my-projects.md)
6. [获取登录用户发布的组件库](./user/list-my-component-repos.md)
7. [获取登录用户正在运行的组件库发布任务](./user/list-my-component-repo-publishing-tasks.md)
8. [修改用户信息](./user/update-user-profile.md)

## 教程

1. [查看一个文档](./docs/get-a-document.md)
2. [获取文档中的一个图片](./docs/get-a-image.md)

## 信息分类编码

1. [获取软件类型](./properties/app-type.md)
