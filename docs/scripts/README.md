# 脚本

## 往 gitee 备份 blocklang 代码

1. `pull_from_github.bat` - 从 github 下载 blocklang 所有仓库的代码；
1. `repos.txt` - blocklang 项目列表，`pull_from_github.bat` 使用；
1. `add_remote_gitee.bat` - 为 blocklang 所有的 git 仓库添加 gitee 远程地址，只需执行一次；
1. `push_to_gitee.bat` - 将 blocklang 所有代码提交到 gitee。

以上文件，要放在存放 blocklang 项目的根目录下，如：

```text
blocklang/
    |--- repo1/
    |--- repo2/
    |--- ...
    |--- pull_from_github.bat
    |--- repos.txt
    |--- add_remote_gitee.bat
    |--- push_to_gitee.bat
```
