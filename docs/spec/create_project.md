# 创建项目

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