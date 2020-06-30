export default [
	{
		path: '',
		outlet: 'home',
		defaultRoute: true,
		id: 'home',
		title: '软件拼装工厂 · BlockLang',
	},
	{
		path: 'about',
		outlet: 'about',
		id: 'about',
	},
	{
		path: 'user/completeUserInfo',
		outlet: 'complete-user-info',
		id: 'complete-user-info',
		title: '完善用户信息',
	},
	{
		path: '{user}',
		outlet: 'profile',
		id: 'profile',
	},
	{
		path: 'settings/profile',
		outlet: 'settings-profile',
		id: 'settings-profile',
		title: '个人资料',
	},
	{
		path: 'settings/marketplace/repo',
		outlet: 'settings-marketplace',
		id: 'settings-marketplace',
		title: '组件市场',
	},
	{
		path: 'new',
		outlet: 'new-repository',
		id: 'new-repository',
		title: '创建一个仓库',
	},
	{
		path: '{owner}/{repository}/projects/new?{type}',
		outlet: 'new-project',
		id: 'new-project',
	},
	{
		path: '{owner}/{project}',
		outlet: 'view-project',
		id: 'view-project',
	},
	{
		path: '{owner}/{project}/readme',
		outlet: 'view-project-readme',
		id: 'view-project-readme',
	},
	{
		path: '{owner}/{project}/dependence',
		outlet: 'view-project-dependence',
		id: 'view-project-dependence',
	},
	{
		path: '{owner}/{project}/pages/{path}',
		outlet: 'view-project-page',
		id: 'view-project-page',
	},
	{
		path: '{owner}/{project}/templets/{path}',
		outlet: 'view-project-templet',
		id: 'view-project-templet',
	},
	{
		path: '{owner}/{project}/services/{path}',
		outlet: 'view-project-service',
		id: 'view-project-service',
	},
	{
		path: '{owner}/{project}/groups/{parentPath}',
		outlet: 'view-project-group',
		id: 'view-project-group',
	},
	{
		path: '{owner}/{project}/pages/new',
		outlet: 'new-page-root',
		id: 'new-page-root',
		title: '创建页面',
	},
	{
		path: '{owner}/{project}/groups/new',
		outlet: 'new-group-root',
		id: 'new-group-root',
		title: '创建分组',
	},
	{
		path: '{owner}/{project}/pages/new/{parentPath}',
		outlet: 'new-page',
		id: 'new-page',
		title: '创建页面',
	},
	{
		path: '{owner}/{project}/groups/new/{parentPath}',
		outlet: 'new-group',
		id: 'new-group',
		title: '创建分组',
	},
	{
		path: '{owner}/{project}/releases',
		outlet: 'list-release',
		id: 'list-release',
	},
	{
		path: '{owner}/{project}/releases/new',
		outlet: 'new-release',
		id: 'new-release',
	},
	{
		path: '{owner}/{project}/releases/{version}',
		outlet: 'view-release',
		id: 'view-release',
	},
	{
		path: 'marketplace',
		outlet: 'list-component-repo',
		id: 'list-component-repo',
		title: '组件市场',
	},
	{
		path: 'settings/marketplace/publish/{taskId}',
		outlet: 'view-component-repo-publish-task',
		id: 'view-component-repo-publish-task',
	},
	{
		path: 'docs/{fileName}',
		outlet: 'docs',
		id: 'docs',
		title: 'BlockLang.com 帮助文档',
	},
];
