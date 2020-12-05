# 查看仓库中的一个页面

如果当前层级是程序模块（叶节点）等，则获取页面的基本信息以及所有分组信息。

项目中支持为一个页面提供多种实现，如网页版、pc 版或者小程序版。为了便于管理，允许不同实现的 key 值相同。
因为名字都相同，就需要在 url 中能区分开是哪一版实现，这里通过在 key 后面添加后缀名区分。

如:

1. `{key}.web`：web 版
2. `{key}.android`: android 版
3. `{key}.ios`: ios 版
4. `{key}.wechat`: 微信小程序版

注意：**关于上述的实现，有一处疑问，是否可以在定义页面时不考虑具体实现，而是在发布阶段，自动适配不同的实现？**如果这样的话，则先提供不加后缀的链接，不加后缀意味着可以在发布阶段适配不同的实现。

```text
GET /repos/{owner}/{repoName}/pages/{pagePath}
```

## Parameters

| Name       | Type     | Description              |
| ---------- | -------- | ------------------------ |
| `owner`    | `string` | **Required**. 用户登录名 |
| `repoName` | `string` | **Required**. 仓库名称   |
| `pagePath` | `string` | 分组的路径               |

## Response

如果用户没有访问权限，则返回

```text
Status: 403 Forbidden
```

如果

1. 项目不存在
2. 没有找到此文件
3. 找到的文件不属于页面

则返回

```text
Status: 404 Not Found
```

如果找到了，则返回

```text
Status: 200 OK
```

返回一个 json 对象

```json
{
    "projectId": 1,
    "repositoryResource": {},
    "parentGroups": []
}
```

RepositoryResource 对象

| 属性名             | 类型       | 描述           |
| ------------------ | ---------- | -------------- |
| `id`               | `int`      | 记录标识       |
| `key`              | `string`   | 资源标识       |
| `name`             | `string`   | 资源名称       |
| `description`      | `string`   | 资源描述       |
| `resourceType`     | `string`   | 资源类型       |
| `parentId`         | `int`      | 父标识         |
| `seq`              | `int`      | 排序           |
| `createTime`       | `datetime` | 创建时间       |
| `createUserId`     | `int`      | 创建用户标识   |
| `lastUpdateTime`   | `datetime` | 最近修改时间   |
| `lastUpdateUserId` | `int`      | 最近修改人标识 |

parentGroups 对象（最后一个元素是当前页面）

| 属性名 | 类型     | 描述       |
| ------ | -------- | ---------- |
| `name` | `string` | 资源名     |
| `path` | `string` | 资源的路径 |
