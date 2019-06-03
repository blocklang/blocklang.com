# UI 部件

设计目标：能集成开源社区的设计语言，如 ant.design，google material 等。

为了实现层面的统一，需要使用 dojo 实现 ant.design 等，但要充分复用样式等。

## 目录结构

存放 UI 部件的目录结构为（以 Button 部件为例）：

```text
root
  |--- src
        |--- widgets
                |--- button
                       |--- index.ts
                       |--- designer.ts
                       |--- demo.ts
                       |--- README.md
                       |--- tests
                              |--- functional
                                      |--- Button.ts
                              |--- unit
                                      |--- Button.ts
                       |--- changelog
                              |--- 1_0_0.json
                              |--- 1_1_0.json
        |--- themes
                |--- bootstrap
                       |--- button.m.css
                       |--- button.m.css.d.ts
  |--- blocklang.json
```

介绍

1. `blocklang.json` - 组件库基本信息
2. `src/widgets` - 存 ui 部件，一个部件对应一个文件夹，文件夹名小写
3. `src/widgets/{button}`
   1. `index.ts` - 部件类
   2. `designer.ts` - 设计器版部件类，在 `index.ts` 基础上增加设计器特性
   3. `demo.ts` - 部件使用效果示例，集成到设计器中
   4. `README.md` - 部件帮助文档，其中包括最新的 API 说明
   5. `test` - 存放单元测试和功能测试
   6. `changelog` - 部件 API 随版本的变更记录
      1. `1_0_0.json` - 一个版本对应一个文件
4. `src/themes` - 存放样式主题，一个文件夹对应一个主题

## `blocklang.json`

```json
{
    "name": "",
    "displayName": "",
    "version": "",
    "description": "",
    "category": "Widget",
    "language": "Typescript"
}
```

1. `name` - 组件库的名称(必填)
2. `version` - 组件库的版本(必填)
3. `displayName` - 组件库的显示名(可选)
4. `description` - 组件库详细介绍(可选)
5. `icon` - 组件库 logo 的存放路径(可选)
6. `category` - 组件库种类，当前仅支持 `Widget`(必填)
7. `language` - 开发语言，当前支持 `Typescript`(必填)

## Widget

其中 `src/button` 文件夹中存储 `Button` 部件的所有文件。

```json
{
    "name": "部件名称，一个项目中要唯一",
    "label": "部件显示名",
    "iconClass": "部件图标",
    "appType": ["web"],
    "properties": [{
        "name": "属性名",
        "label": "属性显示名",
        "value": "属性默认值",
        "valueType": "string | number | boolean | date",
        "options": [{
            "value": "选项值",
            "label": "显示名",
            "title": "",
            "iconClass": ""
        }]
    }],
    "events": [{
        "name": "onClick",
        "label": "单击事件",
        "valueType": "function",
        "arguments": [{
            "name": "参数名",
            "label": "显示名",
            "value": "默认值",
            "valueType": "string | number | boolean | date"
        }]
    }]
}
```

注意：**要能跟踪部件名、属性名、事件名等**

## `changelog`

一个发行版，记录一次 api 变更。

在变更文件中存储变更操作和部件的接口信息，如部件名称、属性和事件等信息。

支持以下命令：

1. `newWidget` - 新增部件
2. `alterWidget` - 修改部件基本信息
3. `addProperty` - 在部件中新增属性
4. `removeProperty` - 移除部件中的属性
5. `alterProperty` - 修改部件中的属性
6. `addEvent`, - 在部件中新增事件
7. `removeEvent` - 移除部件中的事件
8. `alterEvent` - 修改部件中的事件

注意：**修改已存在的属性和事件时，要使用 alter 命令，而不是组合使用 remove 和 add 命令**

## UI 部件项目命名

拟支持的 UI 库，不限于此。

1. widgets-bootstrap - 基于 bootstrap
2. widgets-antd - 基于阿里巴巴蚂蚁金服的 ant design
3. widgets-fusion - 基于阿里巴巴的 fusion design
4. widgets-google-material - 基于 google material (dojo 官方提供)
5. widgets-wechat - 微信小程序版
