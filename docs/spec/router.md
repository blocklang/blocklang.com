# 路由及跳转

## 路由

这里的路由专指 dojo 中的 router。记录网站中的路由，以及页面中的路由跳转。

| outlet               | path                             | 页面               |
| -------------------- | -------------------------------- | ------------------ |
| `home`               |                                  | 公共首页或个人首页 |
| `complete-user-info` | `user/completeUserInfo`          | 完善用户信息       |
| `profile`            | `{user}`                         | 用户档案           |
| `settings-profile`   | `settings/profile`               | 设置用户资料       |
| `new-project`        | `projects/new`                   | 新建项目           |
| `view-project`       | `{owner}/{project}`              | 查看项目           |
| `list-release`       | `{owner}/{project}/releases`     | 发布项目列表       |
| `new-release`        | `{owner}/{project}/releases/new` | 新建发布           |
| `docs`               | `docs/{fileName}`                | 查看帮助文档       |

## 跳转

页面中有两类路由跳转：

1. 基于页面中的 `Link` 部件，用户点击超链接后，可跳转到新的页面；
2. 基于事件或 Fetch 请求的 `router.link()` 和 `router.setPath()` 的编程方式的跳转。

因为第一种的跳转，在页面中清晰可见；而第二种逻辑散落在代码中，因此在此处集中整理出来。

| 类型  | 发起源                                      | 跳转条件                               | 跳转到               |
| ----- | ------------------------------------------- | -------------------------------------- | -------------------- |
| Fetch | GET `/user`                                 | 使用 Oauth2 登录后，用户信息未通过校验 | `complete-user-info` |
| Fetch | PUT `/user/complete-user-info`              | 用户信息修改完成后                     | `home`               |
| Fetch | POST `/projects`                            | 项目创建完成后                         | `view-project`       |
| Fetch | POST `/projects/{owner}/{project}/releases` | 发布创建完成后                         | `list-release`       |

## 如何区分普通的 HTTP 请求和 Fetch 请求

通过在 http 请求中设置 header 信息：`X-Requested-With` 的值为 `FetchApi`

## 权限

角色：

1. 匿名用户
2. 登录用户

| 页面         | 匿名用户 | 登录用户     |
| ------------ | -------- | ------------ |
| 公共首页     | √        |              |
| 个人首页     |          | √            |
| 完善用户信息 |          | √            |
| 修改个人资料 |          | √            |
| 浏览用户档案 | √        | √            |
| 创建项目     |          | √            |
| 浏览公开项目 | √        | √            |
| 浏览私有项目 |          | √ (授权用户) |
| 创建发布任务 |          | √            |
| 浏览发布日志 | √        | √            |
| 浏览发布历史 | √        | √            |
| 浏览教程     | √        | √            |
