# 系统参数

注意事项：

1. 参数值类型：01 表示 `string`、02 表示 `number`
2. 系统参数中配置的标准库信息，是指系统正在应用的标准库（在用的标准库只应有一套）
3. 平台关键字的 `id` 占用 200-299 段
4. 系统预留用户名的 `id` 占用 300-399 段

| id  | parent id | 参数名                     | 参数描述                 | 参数值                                               | 参数值类型 | 是否生效 |
| --- | --------- | -------------------------- | ------------------------ | ---------------------------------------------------- | ---------- | -------- |
| 1   | -1        | blocklang.root.path        | BlockLang 站点资源根目录 | /home/blocklang/data                                 | `string`   | 是       |
| 2   | -1        | maven.root.path            | Maven 仓库根目录         | /root/.m2                                            | `string`   | 是       |
| 3   | -1        | template.project.git.url   | 模板项目的 git 仓库地址  | <https://gitee.com/blocklang/blocklang-template.git> | `string`   | 是       |
| 4   | -1        | install.api.root.url       | 安装 API 的网址          | <https://blocklang.com>                              | `string`   | 是       |
| 5   | -1        | std.widget.api.name        | 部件标准库的 api 项目名  | std-api-widget                                       | `string`   | 是       |
| 6   | -1        | std.widget.ide.name        | 部件标准库的 ide 项目名  | std-ide-widget                                       | `string`   | 是       |
| 7   | -1        | std.widget.register.userid | 注册标准库的用户标识     | 1                                                    | `number`   | 是       |
| 8   | -1        | std.widget.root.name       | 标准库中根部件的名称     | Page                                                 | `string`   | 是       |
| 200 | -1        | platform.keywords          | 平台关键字               | platform.keywords                                    | `string`   | 是       |
| 201 | 200       | 01                         | servlet:apps             | apps                                                 | `string`   | 是       |
| 202 | 200       | 02                         | servlet:installers       | installers                                           | `string`   | 是       |
| 203 | 200       | 03                         | servlet:user             | user                                                 | `string`   | 是       |
| 204 | 200       | 04                         | servlet:users            | users                                                | `string`   | 是       |
| 205 | 200       | 05                         | servlet:projects         | projects                                             | `string`   | 是       |
| 206 | 200       | 06                         | servlet:docs             | docs                                                 | `string`   | 是       |
| 207 | 200       | 07                         | servlet:settings         | settings                                             | `string`   | 是       |
| 208 | 200       | 08                         | servlet:errors           | errors                                               | `string`   | 是       |
| 209 | 200       | 09                         | servlet:logout           | logout                                               | `string`   | 是       |
| 300 | -1        | platform.reserved.username | 系统预留的用户名         | platform.reserved.username                           | `string`   | 是       |
| 301 | 300       | 01                         | user:blocklang           | blocklang                                            | `string`   | 是       |
| 302 | 300       | 02                         | user:block-lang          | block-lang                                           | `string`   | 是       |
| 303 | 300       | 03                         | user:admin               | admin                                                | `string`   | 是       |
| 304 | 300       | 04                         | user:administrator       | administrator                                        | `string`   | 是       |
| 305 | 300       | 05                         | user:main                | main                                                 | `string`   | 是       |
