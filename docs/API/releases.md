# 软件发布服务

## 自动化构建并发布 APP

具有项目管理权限的用户才有权创建一个发行版。

APP 的构建是基于git tag 标识的版本信息，在发布过程中会为项目创建一个 tag。
当设置好版本号信息，发行版标题和发行版描述信息后，点击发布按钮，就会在数据库中保存版本信息，
然后在项目的 git 仓库上打标签，并基于此版本来配置项目，build 项目，最终 install 到 maven 仓库中。

TODO: 添加用户访问限制，如果用户请求过于频繁，则提示用户在多长时间之后再访问。

```text
POST /projects/{owner}/{projectName}/releases
```

### Parameters

| Name          | Type     | Description                         |
| ------------- | -------- | ----------------------------------- |
| `version`     | `string` | **Required**. 语义化版本，如 v0.1.0 |
| `name`        | `string` | **Required**. 发行版的名称          |
| `description` | `string` | 发行版的描述                        |

### Response

输入参数校验

```text
Status: 422 Unprocessable Entity
```

只有通过以下校验后，才能开始注册：

1. `version` 不能为空
2. `name` 不能为空
3. `version` 已被占用
4. `version` 的值要大于上一个版本

创建成功

```text
Status: 201 CREATED
```

| Name          | Type     | Description           |
| ------------- | -------- | --------------------- |
| `id`          | `int`    | 发行版标识            |
| `version`     | `string` | 语义化版本，如 v0.1.0 |
| `name`        | `string` | 发行版的名称          |
| `description` | `string` | 发行版的描述          |

## git 仓库

目录结构

```text
根目录
   |--- gitRepo
          |--- {owner}
                  |--- {project name}

```