# 属性面板

属性面板的结构是在模板文件中定义的，其中定义了属性面板的排列方式。模板文件是一个导出的 JSON 对象。

模板文件的结构如下：

> widget/propertiesLayout.ts

```ts
export default {
    "propertyName": "Name1"
    ...
};
```

## 字段说明

1. `propertyName` - 属性名称
2. `propertyWidget` - 属性部件，与属性部件关联
3. `propertyLabel` - 属性标签，即显示名
4. `propertyGroup` - 属性组
5. `target` - 目标或引用属性
6. `indent` - 空格，将一个属性组内部的各个属性间隔开
7. `newline` - 换行，当一个属性组内的属性较多，无法在一行内显示时，进行换行显示
8. `divider` - 分割线，用于在属性组内分隔原生属性与复合属性，以及为区间部件添加连接符等，支持三个属性值
   1. `vertical` - 垂直分割线
   2. `horizontal` - 水平分割线
   3. `segement` - 中划线
9. `if` - 按条件匹配，如果条件表达式解析为真，则显示该属性，否则不显示
10. `widget` - 目标部件表达式
11. `propertyValue` - 属性值数组

**属性部件**用于组织一个或多个属性，属性虽然众多，但是属性的排列方式可归为常用的几大类，这些都定义在 `designer-core` 中，可直接引用。

## 用法

1. 常规属性

   ```ts
   {
       "propertyName": "maxWidth",
       "propertyLabel": "最大宽度",
       "propertyWidget": PropertyWidget1
   }
   ```

2. 多个属性组合成一个属性组的时候需要将组合的属性放在 `propertyGroup` 中

   ```ts
   {
       "propertyLabel": "边框",
       "propertyGroup": [
           {
               "propertyName": "borderLeft"
           },
           {
               "propertyName": "borderTop"
           },
           {
               "propertyName": "borderRight"
           }
        ]
   }
   ```

3. 在属性组内组合使用 `indent`，`newline` 和 `divider` 调整布局

   ```ts
   {
       "propertyLabel": "字体",
       "propertyGroup": [
           {
               "propertyName": "fontWeight"
           },
           {
               "indent": true
           },
           {
               "propertyName": "fontItalic"
           },
           {
               "newline": true
           },
           {
               "propertyName": "textColor"
           }
       ]
   }
   ```

   ```ts
   {
       "propertyLabel": "外边距",
       "propertyGroup": [
           {
               "propertyName": "marginLeft"
           },
           {
               "divider": "horizontal"
           },
           {
               "propertyName": "margin",
               "target": [
                   "marginLeft",
                   "marginTop",
                   "marginRight",
                   "marginBottom"
               ]
           }
       ]
   }
   ```

4. 当多个属性集中在一个属性部件中展示的时候，需要使用 `target` 标识出对应的属性。`target` 值可以是字符串或者 json 对象，字符串直接写属性名，解析时取其对应的属性部件；使用 json 对象时，`widget` 表示目标部件，当前仅支持 `{self}` 和 `{parent}`，`propertyName` 表示属性名称

   ```ts
   {
       "propertyLabel": "弹性容器子部件",
       "propertyName": "flexItem",
       "target": [
           {"widget": "{parent}","propertyName": "flexDirection"},
           "alignSelf"
       ]
   }
   ```

5. 当满足某个前置条件才显示属性部件时，需要使用 `if` 做条件判断，`widget` 可以用来表示判断的目标（同上），`propertyName`表示目标的属性名称，`propertyValue` 用于表示符合条件的属性值数组；

   ```ts
   {
       "if": {"widget": "{self}", "propertyName": "display", "propertyValue": ["flex", "inlineFlex"]},
       "propertyLabel": "弹性容器",
       "propertyName": "flexContainer"
   }
   ```
