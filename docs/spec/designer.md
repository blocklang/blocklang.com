# 设计器

## Widget

设计器处于编辑模式时，用户需要与设计器中的 Widget(部件) 交互，因此需要在常规的 Widget 上扩展出设计器特有的交互功能。

即在常规的 Widget 上增加以下属性或事件：

1. `widget`: 一个 json 对象，存储部件信息
   1. `id`: 部件添加到页面后生成的标识
   2. `canHasChildren`: 是否可以包含子部件
   3. `properties`: 部件的常规属性值，是一个 json 对象，至少要包含 `id` 和 `value` 属性
2. `focus`: 当前部件是否获取焦点
3. `onFocus`: 部件在获取焦点时触发的事件
4. `onMouseUp`: 鼠标点击部件并松开后触发的事件

`activeWidgetId` 应该是全局唯一存储，犹如 `document.activeElement` 对象，而在每个部件中则使用 `focus` 属性。即 `focus` 的值是通过对比 `activeWidgetId` 与当前部件中的 `widget.id` 而做出的判断。

如果常规的 Widget 上已具有这些属性或事件，则设计器版实现覆盖常规实现。

而部件的常规属性和事件怎么处理呢？

1. 事件一概不处理
2. 属性全都传入
3. 属性是通过 `widget.properties` 传入的

`widget` 中包含四类信息：

1. 部件基本信息
2. 部件添加到页面后新增的信息
3. 部件的属性信息
4. 部件属性信息对应的属性值

## 根据页面模型渲染

页面模型是一组 API 组件库的 Widget 分层组成的。

因为 API 版 Widget，只包含 Widget 的定义信息，没有指定实现的 Widget，所以在渲染时，需要根据 API 版的 Widget 描述找到对应的设计器版的 Widget。寻找路径如下：

1. 页面模型的 Widget 中包含 `api_repo_id`
2. 项目依赖的 dev 依赖中也包含 `api_repo_id`，这样就能找到对应的 dev 版依赖
3. 然后在 dev 版依赖中根据 Api 版 Widget 中的 widgetName 找到对应部件的实例

注意：

1. 在一个项目中，只能为一个 API 版的 Widget 配置**一**套 Dev 版的部件；
2. dev 仓库中的部件名必须与 Api 仓库中的部件名保持一致；
3. 设计器中预览用的部件必须在 Dev 版中导出，而不是从项目的依赖项中选择一个 Build 版部件。
