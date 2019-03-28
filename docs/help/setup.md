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

### 安装 yarn

```sh
# 配置仓库
curl -sS https://dl.yarnpkg.com/debian/pubkey.gpg | sudo apt-key add -
echo "deb https://dl.yarnpkg.com/debian/ stable main" | sudo tee /etc/apt/sources.list.d/yarn.list
# 安装 yarn
sudo apt-get update && sudo apt-get install yarn
# 检测 yarn 版本
yarn --version
```

### 安装 dojo cli

```sh
# 全局安装 dojo cli
npm i @dojo/cli -g
# 检测 dojo cli 版本
dojo --version
```

### 安装 maven

不用手动安装 maven，因为使用了 spring boot 的 `mvnw` 脚本。

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
nohup java -jar blocklang-0.0.1-SNAPSHOT.jar &
```