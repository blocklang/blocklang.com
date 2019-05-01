# 查看项目中的一个页面

如果当前层级是程序模块（叶节点）等，则获取文件的内容。

项目中支持为一个页面提供多种实现，如网页版、pc 版或者小程序版。为了便于管理，允许不同实现的 key 值相同。
因为名字都相同，就需要在 url 中能区分开是哪一版实现，这里通过在 key 后面添加后缀名区分。

如:

1. `{key}.web`：web 版
2. `{key}.android`: android 版
3. `{key}.ios`: ios 版
4. `{key}.wechat`: 微信小程序版

```text
GET /projects/{owner}/{projectName}/pages/{pathId}
```