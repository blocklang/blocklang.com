# 组件

组件是可复用的、是对特定功能的封装。我们将 UI 组件称为小部件(Widget)。

在 Block Lang 中，软件是由组件拼装而成的，因此对组件有更严格的规范。

目标：做到在项目中可以无缝切换组件，项目直接依赖于 API，项目不能依赖于组件，而组件实现 API。

## 待解决的问题

1. 支持跨平台，配置一次后，生成的软件可运行在 web 浏览器上，也可运行在 PC 桌面应用中
   1. 只有一套规范
   2. 但有多套实现，如 Dojo、Vue、React 等实现，或 WPF、QT 等实现
2. UI 部件可支持响应式设计
   1. 如果部件不支持响应式设计，则能为不同分辨率配置不同的页面
   2. 如果部件支持响应式设计，则只需配置一套页面
3. 要能编译出多套软件
4. UI 部件和其他组件一样，都遵循相同的模式
5. 必须确保组件、组件的属性、组件的方法等接口的标识，当值确定后，就不能再修改

## 实现方式

![组件关系图](images/blocklang-component.png)

如上图所示：

1. 为每个组件定义一个 API 项目，专门描述组件接口；
2. 为每个 API 项目定义多套组件项目，可使用不同的编程语言或组件实现，但必须遵循 API 项目中的规范；
3. 项目依赖多版本组件，包含两类依赖：
   1. dev 依赖，专用于开发过程中；
   2. prod 依赖，专用于软件发布，可支持多套 profile，如可发布为 android 原生应用，也可发布为微信小程序。

项目关系说明

1. API 项目用于描述组件接口规范，在 README.md 文件中详细说明接口规范，在 changelog 文件夹下存储 json 格式的组件接口变更记录
2. 组件项目提供具体的实现，要指明对应的 API 项目，也要说明是否可用作 dev 依赖
3. 在项目的依赖配置中，可指定依赖的范围（scope），包括 dev 和 prod 两种，其中 prod 可指定多个 profile，但必须指定一个名为 `default` 的 profile
4. 在发布时，默认按照 `default` 依赖生成软件，用户也可以指定 profile

dev 模式的部件项目必须使用 dojo 开发，非 dev 模式的可使用任何语言或组件库开发。

## 项目结构

一个组件由两类项目定义：

1. API 项目：用于定义组件接口
2. 组件项目：用于实现组件接口

### API 项目

API 是一个不断借鉴和淘汰的演化过程。所以平台要能管理 API 的不同版本，同时平台提供开发的 API 管理能力，让人人都可以定义 API，然后在使用过程中优胜劣汰。

```text
root
  |--- api.json
  |--- README.md
  |--- components
          |--- {component}
                   |--- README.md
                   |--- changelog
                            |--- 0_1_0.json
                            |--- 0_2_0.json
```

目录结构介绍

1. `api.json` - 存储组件 API 基本信息
2. `README.md` - 组件库介绍
3. `components/` - 存所有组件的 API
   1. `{component}/` - 组件名称，采用小写字母，多个单词用'-'隔开
      1. `README.md` - 存组件 API 的详细定义，只存储最新的信息
      2. `changelog/` - 按版本，存储组件 API 变更记录
         1. `0_1_0.json` - 一个版本对应一个变更文件

约定

1. 一个 git tag 中只能集中维护一个 changelog 文件
2. changelog 文件的名称必须与对应的 git tag 的版本号一致(用`_`线替代`.`)

所以，一个 git 版本最多对应 0 或 1 个变更文件，如 v0.1.0 tag 下可能会有一个 0_1_0.json 文件，而 v0.1.1 tag 下可能没有添加变更文件，但在 v0.1.2 tag 下又添加了一个 0_1_2.json 文件。变更文件名使用 git tag 的版本号命名。

| git tag | version in api.json | changlog file |
| ------- | ------------------- | ------------- |
| v0.1.0  | 0.1.0               | 0_1_0.json    |
| v0.1.1  | 0.1.0               |               |
| v0.1.2  | 0.1.2               | 0_1_2.json    |
| v0.1.3  | 0.1.2               |               |

而 `dev` 或 `build` 版组件库引用的是 `api` 库中 `api.json` 文件中的 `version`（注意，不是 git tag 对应的版本号）值。

git tag 与 api.json 中的 `version` 各自的作用分别是：

1. 只有打了 git tag 标记后，才会认为已发布，`dev` 或 `build` 版组件库才能引用；
2. 只有当 changelog 有新增后，才升级 api.json 中的 `version`，并同步打一个 git tag；
3. 如果 changelog 没有新增（changelog 只能新增），但其他文件修改了，如 `api.json` 文件改动了，则不能升级 api.json 中的 `version`，但也可以打一个 git tag 来发布；
4. 所以 api.json 中的 `version` **必须**与最新的 changelog 文件的版本保持一致；
5. 在安装 `api` 版组件库时，如果 git tag 有升级，但是 changelog 文件没有新增，则只更新最新代码，但是不在 `API_REPO_VERSION` 中保存记录

模拟发布流程，验证是否会出现 `dev` 版与 `api` 版对应不上的问题：

1. 发布 `dev` 版的组件库
   1. 下载最新源码
   2. 找出最新的 git tag
   3. 找出最新 git tag 下的 `component.json` 文件，并找出其中的 `version` 属性
   4. 在 `component_repo_version` 表中存储 `git_tag_name` 和 `version`
2. 发布 `api` 版组件库（注意，`api` 组件库必须是增量更新的，所以不能只发当前版本，还需要发布当前版本之前的所有未安装版本）
   1. 根据 `component.json` 中的 `api` 属性，定位到对应的 `api` 版仓库
   2. 获取 `api` 版仓库下的最新的 git tag（注意，并不是截止到 `component.json` 中 `api.version` 指定的版本，而是所有已发布但未安装的版本）
   3. 从 `api_repo_version` 中查找出，当前已安装到哪一个版本（`version`）
   4. 从最新的 git tag 下找出所有的 changelog 文件，并对比出未安装的 changelog 文件
   5. 循环未安装的 changelog 文件，在上一版的基础上逐个安装 changelog
   6. ~~通过对比最新的 git tag 和 当前安装的 git tag，来计算出所有未安装的 git tag（因为有的 git tag 下没有 changelog 文件，所以可能会出现重复尝试安装没有 changelog 文件的 git tag）~~

根据上述用例，`api_repo_version` 表中数据的存储情况为：

以下是每个版本都安装的情况

| use case                | dbid | api_repo_id | version | git_tag_name |
| ----------------------- | ---- | ----------- | ------- | ------------ |
| (1) git tag 为 0.1.0 时安装 | 1    | 1           | 0.1.0   | v0.1.0       |
| (2) git tag 为 0.1.1 时安装 | 1    | 1           | 0.1.0   | v0.1.0       |
| (3) git tag 为 0.1.2 时安装 | 2    | 1           | 0.1.2   | v0.1.2       |
| (4) git tag 为 0.1.3 时安装 | 2    | 1           | 0.1.2   | v0.1.2       |

因为 `api_repo_id` 与 `version` 是联合唯一，所以如果以上用例同时存在，则要忽略安装。比如，如果执行了用例 (3)，则执行用例 (4) 时就会发现 0.1.2 版本已安装，所以无需安装。所以用例 (1) 和用例 (2) 是同一条记录，用例 (3) 和用例 (4) 是同一条记录

以下跳跃式安装的情况

分别是在 0.1.0 和 0.1.2 时安装的

| use case                | dbid | api_repo_id | version | git_tag_name |
| ----------------------- | ---- | ----------- | ------- | ------------ |
| (1) git tag 为 0.1.0 时安装 | 1    | 1           | 0.1.0   | v0.1.0       |
| (2) git tag 为 0.1.2 时安装 | 2    | 1           | 0.1.2   | v0.1.2       |

如果随后在 git tag 为 0.1.3 时安装，则除了更新 git 仓库代码外，不会更新 `api_repo_version` 表中任何信息。

分别是在 0.1.1 和 0.1.3 时安装的

| use case                | dbid | api_repo_id | version | git_tag_name |
| ----------------------- | ---- | ----------- | ------- | ------------ |
| (1) git tag 为 0.1.1 时安装 | 1    | 1           | 0.1.0   | v0.1.1       |
| (2) git tag 为 0.1.3 时安装 | 2    | 1           | 0.1.2   | v0.1.3       |

> 只从最新的 git tag 中查找 changelog 文件，还是逐个 git tag 查找 changelog 文件？

因为 changelog 是增量维护的，当前的实现是从最新的 git tag 中查找所有 changelog 文件。

> “发布”与“安装”的区别：

1. 发布是指通过 git tag 来发布 git 仓库源码
2. 安装是指在 blocklang 相关表中登记已发布的组件库信息

#### `api.json`

```json
{
    "name": "",
    "version": "",
    "displayName": "",
    "description": "",
    "category": "Widget",
    "components": [
        "components/button"
    ]
}
```

数据项介绍

1. `name` - 组件库的名称(必填)
2. `version` - 组件库的版本(必填)
3. `displayName` - 组件库的显示名(可选)
4. `description` - 组件库详细介绍(可选)
5. `category` - 组件库种类，当前支持 `Widget` 和 `ClientAPI`(必填)
6. `components` - 数组，存储组件的相对路径
   1. 当 category 为 `Widget` 时，如 Button 部件的 API 存在 `components/button` 文件夹中

注意，不需要在 API 仓库上增加 `std` 属性，因为 API 库只是一个接口描述，而标准库是相对于实现来讲的。

#### changelog

一个发行版，记录一次 api 变更。

在变更文件中存储变更操作和变更的接口信息，如部件名称、属性和事件等信息。

UI 部件支持以下命令：

1. `newWidget` - 新增部件
2. `alterWidget` - 修改部件基本信息
3. `addProperty` - 在部件中新增属性
4. `removeProperty` - 移除部件中的属性
5. `alterProperty` - 修改部件中的属性
6. `addEvent`, - 在部件中新增事件
7. `removeEvent` - 移除部件中的事件
8. `alterEvent` - 修改部件中的事件

功能组件支持以下命令：

1. `newComponent` - 新增组件
2. `alterComponent` - 修改组件基本信息

不提供删除命令，提供过期命令？

注意：

1. 修改已存在的属性和事件时，要使用 alter 命令，而不是组合使用 remove 和 add 命令
2. 如果组件已经登记，则不允许删除组件（因为可能已被引用），所以增加时要慎重

changelog 的 json 格式

```json
{
    "id": "20190101_button",
    "author": "",
    "changes": [
        {
            "newWidget": {
                "name": "部件名称，一个项目中要唯一",
                "label": "部件显示名",
                "description": "部件详细说明",
                "iconClass": "部件图标",
                "properties": [{
                    "name": "属性名",
                    "label": "属性显示名",
                    "defaultValue": "属性默认值",
                    "valueType": "string | number | boolean",
                    "description": "属性详细说明",
                    "required": false,
                    "options": [{
                        "value": "选项值",
                        "label": "显示名",
                        "description": "选项的详细说明",
                        "valueDescription": "选项值的详细说明",
                        "iconClass": ""
                    }]
                }],
                "events": [{
                    "name": "onClick",
                    "label": "单击事件",
                    "valueType": "function",
                    "description": "事件详细说明",
                    "requird": false,
                    "arguments": [{
                        "name": "参数名",
                        "label": "显示名",
                        "defaultValue": "默认值",
                        "valueType": "string | number | boolean",
                        "description": "事件输入参数的详细说明"
                    }]
                }]
            }
        }
    ]
}
```

注意：

1. **要能跟踪部件名、属性名、事件名等，当给定唯一标识后，不能再变更此标识**
2. 部件的图标在 API 库中管理
3. `events` 下的 `valueType` (与 `name` 属性并列)的值只能是 `function`，所以不需要用户设置；如果设置为 `function` 也不会报错
4. changelog 文件的命名约定是，名称使用版本号，但要将版本中的点替换为下划线，如 `0_1_0.json`
5. `properties` 和 `events` 中增加 `required` 属性，如果部件的对应属性是可选的，则 `required` 的值为 `false`，否则为 `true`；如果 `required` 为 `true` 则要设置好默认值

##### Value Or Default Value

部件的属性值只使用 `value`，还是组合使用 `value` 和 `defaultValue`?

刚开始时属性值为 `value`，但是在设计 api 时，发现取名为 `defaultValue` 更贴切，因为在 `api.json` 中设置的值就是默认值，但是在设计部件时却需要使用 `value`，一个部件的值是会变化的，`defaultValue` 只是部件的初始值，是一个特殊阶段的值。

在 API 中的 `defaultValue` 指的是各属性的默认值；在部件中的 `value` 指的是 `value` 属性。这两者不是一回事，概念不能混淆。

### 组件项目

#### UI 部件

设计目标：能集成开源社区的设计语言，如 ant.design，google material 等。

为了实现层面的统一，需要使用 dojo 实现 ant.design 等，但要充分复用样式等。

存放 UI 部件的目录结构为（以 Button 部件为例）：

UI 部件又包含两种：

1. 一种是在开发模式下，设计器中使用的部件
2. 一种是在生产模式下，渲染界面时使用的部件

这两种项目的结构类似，都为

```text
root
  |--- src
        |--- button
                |--- index.tsx
                |--- propertiesLayout.ts
                |--- demo.ts
                |--- tests

        |--- themes
                |--- bootstrap
                       |--- button.m.css
                       |--- button.m.css.d.ts
  |--- tests
        |--- functional
                |--- Button.ts
        |--- unit
                |--- Button.ts
  |--- component.json
  |--- icons.svg
```

目录结构介绍

1. `component.json` - 存储组件基本信息
2. `icons.svg` - 部件在设计器中显示的图标，统一存在一个文件中，**专用于设计器版组件**
3. `src/{button}/` - 存 ui 部件，一个部件对应一个文件夹，文件夹名小写
   1. `index.tsx` - 部件类
   2. `propertiesLayout.ts` - 部件属性面板的布局，**专用于设计器版组件**
   3. `demo.ts` - 部件使用效果示例，集成到设计器中
4. `src/themes/` - 存放样式主题，一个文件夹对应一个主题
5. `test/` - 存放单元测试和功能测试

注意：

1. **部件名和部件的属性名必须使用在 API 库中定义的名称**，这也是“组件库实现 API 库”这句话在实现层面的体现；
1. 必须按照上述结构存放部件，这是必须遵循的约定，如将部件存到 `src/widgets/{button}` 目录下，则发布到生产后的页面将找不到该部件；
1. 约定部件名遵循驼峰命名规范，而部件存储路径使用中划线分割的小写字母，如部件 `TextInput`，而部件的存储路径为 `src/text-input/index.ts`。

`component.json`

```json
{
    "name": "",
    "version": "",
    "displayName": "",
    "description": "",
    "category": "Widget",
    "language": "Typescript",
    "std": false,
    "baseOn": "dojo",
    "components": [
        "src/button"
    ],
    "api": {
        "git": "",
        "version": ""
    },
    "dev": true,
    "appType": "web"
}
```

数据项介绍

1. `name` - 组件库的名称(必填)
2. `version` - 组件库的版本(必填)
3. `displayName` - 组件库的显示名(可选)
4. `description` - 组件库详细介绍(可选)
5. `icon` - 组件库 logo 的存放路径(可选)
6. `category` - 组件库种类，当前仅支持 `Widget`(必填)
7. `language` - 开发语言，当前支持 `Typescript`(必填)
8. `std` - 是否为标准库，默认为 `false`，标准库是 blocklang 管理员注册到组件市场中的，但不会在组件市场中显示
9. `baseOn` - 这个参数主要用于 Widget，常用的值有 `dojo`、`react`、`vue`、`angular` 等
10. `components` - 数组，存储 widget 的相对路径，如果 `Button` 部件类在 `src/widgets/button/index.ts` 文件中，则相对路径为 `src/widgets/button`
11. `api` - 表示此仓库实现的是哪个 api 项目
    1. `git` - git 仓库地址
    2. `version` - 版本号
12. `dev` 是否能用户开发模式，如果项目仅用于开发模式下，则将 `dev` 的值设置为 `true`，默认为 `false`
13. `appType` 与页面的 `appType` 的值相同，**一个组件库只能存放一种 AppType 的组件**

注意：

1. 部件的存储路径是有约定的，本可以根据约定自动查找，但这里增加 `components` 参数，让组件库开发人员显式指定。这样就增加一个人为干预的手段。
2. 当 UI 部件的 `dev` 为 `true` 时，表示存储的是设计器版部件类，在对应的部件上增加设计器特性。
3. API 仓库的目标是支持通用的设计，因此 API 仓库不能有 `appType` 属性，只有组件仓库才需要 `appType` 属性。
4. 只需要在组件库中增加 `std` 属性，不用在 API 库中增加，因为 API 库只是一个接口描述，而标准库是相对于实现来讲的。

数据项校验规则

1. name
   1. 不能为空
   2. 长度不能超过64个字节
   3. 只能包含字母、数字、中划线和下划线
   4. 同一个发布者没有发不过此名称的组件库
2. version
   1. 不能为空
   2. 必须是有效的语义化版本
   3. 必须大于最新的版本号
3. category
   1. 不能为空
   2. 只能是 “Widget”（不区分大小写）
4. language
   1. 不能为空
   2. 只能是“Typescript”或“Java”（不区分大小写）
5. description
   1. 长度不能超过512个字节
6. icon
   1. 长度不能超过64个字节
7. api.git
   1. 不能为空
   2. 有效的 https 协议的 git 远程仓库地址
   3. 根据此地址能找到远程仓库
8. api.version
   1. 不能为空
   2. 有效的语义化版本号

UI 部件项目命名

拟支持的 UI 库，不限于此。

1. widgets-bootstrap - 基于 bootstrap
2. widgets-antd - 基于阿里巴巴蚂蚁金服的 ant design
3. widgets-fusion - 基于阿里巴巴的 fusion design
4. widgets-google-material - 基于 google material (dojo 官方提供)
5. widgets-wechat - 微信小程序版

`propertiesLayout.ts`

在设计器中，模板文件描述部件的属性面板布局结构。统一约定将模板文件命名为 `propertiesLayout.ts`。所以一个设计器版 Widget 的目录中必须包含 `index.ts` 和 `propertiesLayout.ts` 两个文件。`propertiesLayout.ts` 的结构详见 [模板文件介绍](./properties-layout.md)。

`icons.svg`

设计器版的部件库中，在一个 svg 文件中统一存储所有部件的图标。

1. 文件名必须是 `icons.svg`
2. 该文件要放在 `src` 文件夹下，此处需要加一个复制文件操作，以便将此文件复制到 `output/dist/` 文件夹中
3. 图标库使用的是 svg sprites，因此必须包括上 `symbol` 节点；
4. 一个部件对应一个 `symbol` 节点，且 `symbol` 节点的 `id` 必须与部件名保持一致，如 `TextInput`；
5. 所有的 `symbol` 节点必须放在 `svg` 节点中，并且添加 `display:none` 样式。

示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" style="display: none;">
    <symbol id="TextInput" viewBox="0 0 1024 1024">
        <title>TextInput</title>
        <g style="pointer-events:all">
            <g stroke-opacity="1">
                <rect stroke="#808080" height="468.80658" width="1156.54317" y="358.18931" x="-69.53086707336425" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" stroke-width="32" fill="#ffffff" fill-opacity="1" stroke-opacity="1"></rect>
                <text stroke="#808080" transform="matrix(18.141660690307617,0,0,5.216737270355225,-2768.37820148468,-2311.7603219524026) " xml:space="preserve" text-anchor="middle" font-family="serif" font-size="24" y="568.88889" x="167.50617" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" fill="#000000"></text>
                <line y2="723.9999977666023" x2="57.925922926635735" y1="453.1851758326515" x1="59.70370292663574" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" stroke-width="32" fill="none" fill-opacity="1" stroke-opacity="1" stroke="#808080"></line>
                <line y2="464.44444650772095" x2="138.51851331466673" y1="462.66666650772095" x1="-12.592596685333248" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" stroke-width="32" stroke="#808080" fill="none" fill-opacity="1" stroke-opacity="1"></line>
                <line y2="732.1481403744507" x2="139.55556165451048" y1="730.3703603744507" x1="-11.555548345489456" stroke-linecap="null" stroke-linejoin="null" stroke-dasharray="null" stroke-width="32" stroke="#808080" fill="none" fill-opacity="1" stroke-opacity="1"></line>
            </g>
        </g>
    </symbol>
</svg>
```

上例中的 `symbol` 中包含 `g`、`line` 等各种元素；而Bootstrap 的 svg 文件中都是用 `path` 绘制的，较简洁，如何做到的呢？

按上述约定，保存好 `icons.svg` 文件后，在发布时，会将此文件复制到 `output/dist` 文件夹下，然后 page designer 直接根据文件夹获取此文件。

注意，为了避免在页面中同时加载多个 `icons.svg` 时 `symbol` 的 `id` 冲突时，在页面上加载完 `icons.svg` 文件后会在 `id` 上加上简短的前缀，以确保 `symbol` 的 `id` 在 html 页面中唯一。调整后的 `id` 如 `A-TextInput`。

### 功能组件

## 编译组件库

组件库中存的是源代码，在往组件市场发布组件时，要对组件库进行编译。

并且不同的项目会依赖组件库的不同版本，所以要按版本编译。

### JavaScript 组件库

JavaScript 组件库的扩展机制，是将组件库编译为 dojo app，然后通过 `script.js` 将组件库延迟注入为 html 页面的 `script` 节点。

在每个组件库（源码）的入口文件 `main.ts` 中以 `{"widgetName1": "widgetObject1", "widgetNameX": "widgetObjectX"}` 的格式存储部件。

因为 `main.ts` 文件的唯一功能就是向 `global` 中缓存部件，所以可以根据 `component.json` 中的配置自动生成代码。

`global._block_lang_widgets` 的数据格式：

```json
{
    "github.com/owner/repo": {
        "widgetName1": "WidgetObject1"
    }
}
```

编译后的文件是按版本存在 `package` 文件夹中的。主文件名为 `main_{version}.js` 和 `main_{version}.css`。通过约定，不用在数据库中存储入口文件的名称。但是发布后的文件名是 `main_xxxxx_bundle.js` 这种格式，因此需要将两者关联起来，有两种处理方式：

1. 在 build dojo app 时，规范文件名，但涉及到 `dojo build` 的内部实现，所以暂不考虑；
2. 在 spring mvc 的拦截器中转换文件名，直接找以 main 开头的 js 的 css 文件。

按照约定大于配置的原则，约定 url 中的入口文件名是 `main`，而编译后的入口文件名为 `bootstrap`。

资源请求的路径为：

1. js 文件为 `/packages/{website}/{owner}/{projectName}/{version}/main.js`；
2. css 文件为 `/packages/{website}/{owner}/{projectName}/{version}/main.css`；

Js module 在浏览器中的存储格式

Widget 组件存在 `global._block_lang_widgets_` 对象中，**注意只存当前项目的依赖的 widget**。如果存储用户访问过的项目依赖的 widgets，则就需要考虑如何清除，复杂度会增加。

```ts
{
    "widget_key": {
        widget: Widget_Class,
        propertiesLayout: Layout_module
    }
}
```

其中：

* `widget_key` - `{widget_name}`
* `widget` - 是一个基于类的 Widget
* `propertiesLayout` - 是部件的属性面板结构

其他 JavaScript 组件存在 `global._block_lang_widgets_` 对象中。

在 designer-core 项目中定义一个函数来封装实现细节。

### Java 组件库

## 管理方式

API 和组件源码可托管在基于 git 的源代码托管网站，如 github 或码云等。但在 BlockLang 平台中使用组件时，必须先将这些组件发布到 BlockLang 的组件市场，然后项目直接引用组件市场中的组件。关系如下图：

![组件关系图2](images/blocklang-component2.png)

后续会支持注册发布在 maven 中央仓库中的 jar，发布在 npmjs 中的 js，以及本地的项目等。

### 人工审核

为了充分发挥出 API 项目将业务项目与实现隔离的优势，以便在 API 稳定的前提下能无缝切换实现，就必须确保 API 项目具备普适的特性。要重点避免出现为一套实现定制一套 API 的情况，这样 API 项目就没有存在的价值了。

因此当往组件市场中发布时，要加入人工审核环节，以控制 API 项目的数量，如果发现已存在类似的 API，则建议作者为已存在的 API 项目做贡献，除非他提供的 API 更加丰富。

如何处理：

1. 为了不给大家的积极性设置任何约束，不强行限制大家发布 API 库，但是用户会对 API 库进行点评
2. 审核人员的审核依据：解决同一类业务、被标注为优质的 API 库，不能超过 10 个
3. 一个 API 项目的实现库越多，这套 API 项目就越优质
