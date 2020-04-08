# 更新一个页面的模型

如果页面模型不存在，则新建页面模型；如果已存在，则更新页面模型。

注意：

1. **返回的每个字段都必须严格把关，如果在页面中用不到则不能添加**；
2. 在保存时，`widgets` 要按顺序保存，seq 从 1 开始。

```text
PUT /designer/pages/{pageId}/model
```

## Parameters

输入参数与 [update-a-page-model](./update-a-page-model.md) 中的返回值类型要**完全**保持一致。

## Response

页面不存在，则返回

```text
Status: 404 Not Found
```

用户未登录或者对页面所属的项目没有写权限，则返回

```text
Status: 403 Forbidden
```

否则返回

```text
Status: 204 No Content
```

不返回任何数据。
