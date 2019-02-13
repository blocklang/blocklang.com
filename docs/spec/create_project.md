# 创建项目

1. 项目基本信息有效性校验
   1. 项目名是否有效：只允许字母、数字、中划线(-)、下划线(_)、点(.)等字符
   2. 项目名是否被占用：一个用户下的项目不能重名
2. 在 `PROJECT` 数据库表中存储项目基本信息
3. 在 `APP` 数据库表中存储 APP 基本信息，其中包括 `Registration Token`
4. 在 `BlockLang/gitRepo/{owner}/{project_name}` 文件夹下初始化一个 git 仓库，其中默认生成一个 README.md 文件