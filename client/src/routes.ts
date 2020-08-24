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
		outlet: 'new-repo',
		id: 'new-repo',
		title: '创建一个仓库',
	},
	{
		path: '{owner}/{repo}/projects/new?{type}',
		outlet: 'new-project',
		id: 'new-project',
	},
	{
		path: '{owner}/{repo}',
		outlet: 'view-repo',
		id: 'view-repo',
	},
	{
		path: '{owner}/{repo}/readme',
		outlet: 'view-repo-readme',
		id: 'view-repo-readme',
	},
	{
		path: '{owner}/{repo}/{project}/dependence',
		outlet: 'view-project-dependence',
		id: 'view-project-dependence',
	},
	{
		path: '{owner}/{repo}/pages/{path}',
		outlet: 'view-repo-page',
		id: 'view-repo-page',
	},
	{
		path: '{owner}/{repo}/templets/{path}',
		outlet: 'view-repo-templet',
		id: 'view-repo-templet',
	},
	{
		path: '{owner}/{repo}/services/{path}',
		outlet: 'view-repo-service',
		id: 'view-repo-service',
	},
	{
		path: '{owner}/{repo}/groups/{parentPath}',
		outlet: 'view-repo-group',
		id: 'view-repo-group',
	},
	{
		path: '{owner}/{repo}/pages/new',
		outlet: 'new-page-root',
		id: 'new-page-root',
		title: '创建页面',
	},
	{
		path: '{owner}/{repo}/groups/new',
		outlet: 'new-group-root',
		id: 'new-group-root',
		title: '创建分组',
	},
	{
		path: '{owner}/{repo}/pages/new/{parentPath}',
		outlet: 'new-page',
		id: 'new-page',
		title: '创建页面',
	},
	{
		path: '{owner}/{repo}/groups/new/{parentPath}',
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
