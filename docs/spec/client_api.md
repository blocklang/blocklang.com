# 客户端 API

客户端 API 也称为客户端功能组件。

## 设计思路

一个客户端 API 就是一个 JS 函数，输入参数可看作一个属性；如果输入参数是函数，则看作此组件的子部件，要支持多个函数。

如 `setData(key: string, value: string, callback: ()=> {})`，则此客户端 API 的名称为 `setData`，其中拥有三个属性：

1. `key` - 一个普通属性
2. `value` - 一个普通属性
3. `callback` - 看作 `setData` 的子组件，`canHasChildren` 的值要设置为 `true`

这样客户端 API 的结构和 Widget 的结构就能保持一致了。

## Web 版

创建以下项目存放 web 版的客户端 API：

1. api-web-api - web 功能组件的 api 描述仓库
2. ide-web-api - ide 版的 web 功能组件仓库
3. web-api - 在运行环境下使用的 web 功能组件

注意命名上的区别，UI 组件使用 `widget`，而客户端 API 使用 `api`。

此项目存放的 API 如下：

1. `setData` - 在浏览器端往 dojo store 中写入数据
2. `getData` - 在浏览器中从 dojo store 中获取数据（也可通过表达式直接绑定 page data 中的变量）
3. `request` - 发起网络请求，包括对远程数据进行增删改查
4. ...

`request` 的使用注意事项

1. 如果 `request` 从服务器端请求数据，则数据成功返回后，要存在 dojo store 中；
2. 在设计器中不建议直接使用 `request` 组件，而是使用远程的 service 组件，service 组件调用 `request` 组件。
