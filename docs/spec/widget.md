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
                       |--- api.json
                       |--- README.md
                       |--- tests
                              |--- functional
                                      |--- Button.ts
                              |--- unit
                                      |--- Button.ts
        |--- themes
                |--- bootstrap
                       |--- button.m.css
                       |--- button.m.css.d.ts
  |--- package.json
```

介绍

1. `package.json` - 项目介绍
2. `src/widgets` - 存 ui 部件，一个部件对应一个文件夹，文件夹名小写
3. `src/widgets/{button}`
   1. `index.ts` - 部件类
   2. `designer.ts` - 设计器版部件类，在 `index.ts` 基础上增加设计器特性
   3. `api.json` - 描述部件的接口
   4. `README.md` - 部件帮助文档
   5. `test` - 存放单元测试和功能测试
4. `src/themes` - 存放样式主题，一个文件夹对应一个主题

## `package.json`

```json
{
    "name": "",
    "version": ""
}
```

其中 `src/button` 文件夹中存储 `Button` 部件的所有文件。

## `api.json`

约定，在每个部件的文件夹中定义一个 `api.json` 文件，在此文件中存储部件的接口信息，如部件名称、属性和事件等信息。

```json
{
    "name": "部件名称，一个项目中要唯一",
    "label": "部件显示名",
    "appType": ["web"],
    "properties": [{
        "name": "属性名",
        "label": "属性显示名",
        "value": "属性值",
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