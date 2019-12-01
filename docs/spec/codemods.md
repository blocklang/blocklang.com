# 代码生成

将模型转换为软件有两种方式：

1. 代码生成 - 根据模型生成代码，然后编译为软件；
2. 运行时解析模型 - 编写一个运行引擎，实时解析模型并转换为软件。

当前依然是编程式盛行，代码生成方式可以生成规范的、准确的代码，便于开发人员介入，也可以直接向客户交付代码，所以 blocklang 选用代码生成方案。但是当时机成熟之后，会考虑运行时解析模型的方案。

约定生成的代码：

1. 必须严格遵守代码规范，如《阿里巴巴 Java 开发手册》等；
2. 必须要与标准的手工开发时使用的目录结构一致；
3. 支持生成测试用例等。

注意：**所有的设计，必须朝着能最大化利用现有生态，最大化利用现有技术和组件的方向努力**。而不是跃迁式的自建一套框架，此类框架的代码即使完全开源，也理当被看作是封闭的，是开发者意识的自我封闭。

## 生成 TypeScript 代码

我们使用自研的 CLI 来生成 TypeScript 代码。CLI 项目的命名约定为 `codemods_{library}`，如 codemods_dojo 用于生成 dojo app 代码。

在模板项目的根目录下运行以下命令即可生成 dojo app 代码：

```sh
codemods --library dojo
```

其中 `library` 的值可以是 `dojo`、`react` 和 `vue` 等。

### 项目模型

约定项目模型的存储结构为：

```text
root/
 |--- .blocklang_models/
          |--- project.json
          |--- dependences.json
          |--- widgets.json
          |--- pages/
                |--- {pageKey}/
                        |--- page.json
```

其中：

1. `.blocklang-models/` - 下存储项目的模型信息，每次编译前都要清空并重新生成此文件夹内的所有内容；
2. `project.json` - 项目基本信息
3. `dependences.json` - 项目级信息，只存储 build 版本的依赖信息；
4. `widgets.json` - 项目级信息，存储项目使用的 UI 部件信息（API 版）；
5. `pages/` - 存储页面的模型数据
6. `{pageKey}/` - 每个页面的 key 值
7. `page.json` - 页面级信息，页面模型，其中包括页面基本信息、页面 ui 结构、页面数据结构和页面行为等
8. ……

### 生成 Dojo APP

生成一个 Dojo App 时，需处理以下文件：

1. `package.json` - 修改 `name`、`version` 和 `dependences` 属性；
2. `index.html` - 修改 `title` 属性；
3. `src/interfaces.d.ts` - 增加 type 和 interface；
4. `src/App.ts` - 添加页面 `Outlet`；
5. `src/routes.ts` - 为页面配置路由
6. `src/pages/{pageKey}.tsx` - 页面;
7. `src/processes/{pageKey}Processes.ts` - 页面的数据操作

注意：一个 page 对应零到一个 process；
