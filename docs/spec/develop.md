# 项目研发

## 创建项目

1. 项目基本信息有效性校验
   1. 项目名是否有效：只允许字母、数字、中划线(-)、下划线(_)、点(.)等字符
   2. 项目名是否被占用：一个用户下的项目不能重名
2. 在 `PROJECT` 数据库表中存储项目基本信息
3. 在新建的项目下创建一个入口程序模块，名称为 `Main`
4. 在新建的项目下创建一个 `README.md` 文件
5. 在 `APP` 数据库表中存储 APP 基本信息，其中包括 `Registration Token`
6. 在 `BlockLang/gitRepo/{owner}/{project_name}` 文件夹下初始化一个 git 仓库
7. 在新建的 git 仓库下创建并提交 `Main.json` 文件
8. 在新建的 git 仓库下创建并提交 `README.md` 文件

## 浏览项目

项目状态分为：编辑 -> 提交

1. 在浏览模式下，可为文件名显示不同的颜色，了解模块的当前状态：新增，修改和已提交；
2. 在提交模式下，可查看所有修改的模块，并支持选定哪些模块提交；
3. 显示项目最近提交信息
4. 如果程序模块处于提交状态，则显示提交信息；如果模块已被修改（包含未提交的内容），也显示提交信息
5. 提供进入提交历史、发布版本和贡献者等页面的入口
6. 注意，要为三种状态选好不同的颜色

## 创建资源

git 仓库中的文件命名规范：

1. 程序模块：`{key}.page.json`
2. 程序模块模板：`{key}.page.tmpl.json`
3. 服务：`{key}.api.json`
4. 文件：`{name}`

## 项目部署

提示信息

```text
部署到您的主机(help icon) Linux/Windows

1. 下载并<安装> <blocklang-installer>
2. 执行 `./blocklang-installer register` 命令注册服务器
    1. 指定URL 为 `https://blocklang.com`
    2. 指定注册 token 为 `xxxxxdsx`
    3. 设置运行端口 <port>
3. 执行 `./blocklang-installer run --port <port>` 命令启动服务
4. 在浏览器中访问 http://<ip>:<port>
```

将发布的 Linux 和 Windows 安装器上传到 <https://gitee.com> 网站，支持从 <https://gitee.com> 下载安装器