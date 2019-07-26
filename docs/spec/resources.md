# 项目资源

项目资源分类和 URL

1. README 文件 - `{owner}/{project}/readme`
2. DEPENDENCE 文件 - `{owner}/{project}/dependence`
3. 页面 - `{owner}/{project}/pages/{resourceKey}`
4. 页面模板 - `{owner}/{project}/templets/{resourceKey}`
5. 服务 - `{owner}/{project}/services/{resourceKey}`
6. 分组 - `{owner}/{project}/groups/{resourceKey}`
7. 数据库表 - `{owner}/{project}/tables/{resourceKey}`

## 分组的状态

1. 如果文件夹未跟踪，且文件夹下没有文件（可以有未跟踪的文件夹），则不设置文件夹状态
1. 如果文件夹未跟踪，但文件夹下有未跟踪的文件，则文件夹状态为未跟踪
1. 如果文件夹已跟踪，且文件夹下的文件未更改，则不设置文件夹状态
1. 如果文件夹已跟踪，且文件夹下只有未跟踪的文件，则文件夹状态为未跟踪
1. 如果文件夹已跟踪，且文件夹下只有修改的文件，则文件夹状态为修改
1. 如果文件夹已跟踪，且文件夹下同时有未跟踪和修改的文件，则文件夹状态为修改
