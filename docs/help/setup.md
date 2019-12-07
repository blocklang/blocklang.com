# 安装 BlockLang

## Linux 上安装

### 安装 git

不需要安装 git，因为使用的是 jgit。

### 安装 nodejs

```sh
# 安装 nodejs
sudo apt-get install nodejs
# 检测 nodejs 版本
node --version
```

### 安装 npm

```sh
# 安装 npm
sudo apt install npm
# 检测 npm 版本
npm --version
```

切换 npm 镜像(可选)

```sh
# 查看当前镜像地址
npm get registry

# 切换为淘宝镜像
npm config set registry http://registry.npm.taobao.org/

# 切换为原本的 npm 镜像
npm config set registry https://registry.npmjs.org/

# 使用淘宝 puppeteer 镜像
npm config set puppeteer_download_host=https://npm.taobao.org/mirrors
```

### 安装 yarn

```sh
# 配置仓库
curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -
echo "deb https://dl.yarnpkg.com/debian/ stable main" | sudo tee /etc/apt/sources.list.d/yarn.list
# 安装 yarn
sudo apt-get update && sudo apt-get install yarn
# 检测 yarn 版本
yarn --version

# 查看当前源
yarn config get registry
https://registry.yarnpkg.com
# 切换为淘宝镜像
yarn config set registry https://registry.npm.taobao.org
yarn config set puppeteer_download_host https://npm.taobao.org/mirrors
```

### 安装 dojo cli

```sh
# 全局安装 dojo cli
npm i @dojo/cli -g
# 检测 dojo cli 版本
dojo --version
```

### 安装 codemods

```sh
# 全局安装
npm i codemods -g
# 检测 codemods 版本
codemods --version
```

### 安装 maven

不用手动安装 maven，因为使用了 spring boot 的 `mvnw` 脚本。

### 安装 Python

Dojo framework 的依赖项中包含 node-gyp，需要安装 Python 3。

有的 Linux 中默认安装了 Python。

### 配置

1. 构建 windows 版和 linux 版的 blocklang-installer
2. 下载 oracle jdk 11.0.2
3. 将 blocklang-installer 和 oracle jdk 上传到服务器指定的文件夹中

### 启动

```sh
# 获取占用 443 端口的进程
netstat -apn | grep 443

# 杀掉进程
kill -9 {pid}

# 后台启动
## 使用 setsid 命令
setsid java -jar blocklang-0.0.1-SNAPSHOT.jar >> blocklang.log
## 或者，使用 nohup 命令
nohup java -jar blocklang-0.0.1-SNAPSHOT.jar &
```

### 注册标准库

1. 在组件市场中注册标准库：
    1. `https://github.com/blocklang/std-ide-widget.git`
    1. `https://github.com/blocklang/std-widget-web.git`
1. 选择一个 github 帐号或者 qq 帐号 `blocklang.com`，则第一个登录用户会被看作管理员
1. 在系统参数中配置当前使用的标准库，包括：
   1. `std.widget.api.name` - 标准库 API 版的名称
   1. `std.widget.ide.name` - 标准库 IDE 版的名称
   1. `std.widget.build.dojo.name`- 标准库 Build 版的名称，基于 Dojo 实现
   1. `std.widget.register.userid` - 管理员用户标识
   1. `std.widget.root.name` - 标准库中的页面根部件

## Windows 上安装

### 安装 Python

Dojo framework 的依赖项中包含 node-gyp，需要安装 Python 3。

注意：在 Windows 上可以安装 3.8 版本，但安装目录中不要出现中文字符。