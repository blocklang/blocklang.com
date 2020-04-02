export default [
	{
		path: '',
		outlet: 'home',
		defaultRoute: true,
	},
	{
		path: 'about',
		outlet: 'about',
	},
	{
		path: 'user/completeUserInfo',
		outlet: 'complete-user-info',
	},
	{
		path: '{user}',
		outlet: 'profile',
	},
	{
		path: 'settings/profile',
		outlet: 'settings-profile',
	},
	{
		path: 'settings/marketplace/repo',
		outlet: 'settings-marketplace',
	},
	{
		path: 'projects/new',
		outlet: 'new-project',
	},
	{
		path: '{owner}/{project}',
		outlet: 'view-project',
	},
	{
		path: '{owner}/{project}/readme',
		outlet: 'view-project-readme',
	},
	{
		path: '{owner}/{project}/dependence',
		outlet: 'view-project-dependence',
	},
	{
		path: '{owner}/{project}/pages/{path}',
		outlet: 'view-project-page',
	},
	{
		path: '{owner}/{project}/templets/{path}',
		outlet: 'view-project-templet',
	},
	{
		path: '{owner}/{project}/services/{path}',
		outlet: 'view-project-service',
	},
	{
		path: '{owner}/{project}/groups/{parentPath}',
		outlet: 'view-project-group',
	},
	{
		path: '{owner}/{project}/pages/new',
		outlet: 'new-page-root',
	},
	{
		path: '{owner}/{project}/groups/new',
		outlet: 'new-group-root',
	},
	{
		path: '{owner}/{project}/pages/new/{parentPath}',
		outlet: 'new-page',
	},
	{
		path: '{owner}/{project}/groups/new/{parentPath}',
		outlet: 'new-group',
	},
	{
		path: '{owner}/{project}/releases',
		outlet: 'list-release',
	},
	{
		path: '{owner}/{project}/releases/new',
		outlet: 'new-release',
	},
	{
		path: '{owner}/{project}/releases/{version}',
		outlet: 'view-release',
	},
	{
		path: 'marketplace',
		outlet: 'list-component-repo',
	},
	{
		path: 'settings/marketplace/publish/{taskId}',
		outlet: 'view-component-repo-publish-task',
	},
	{
		path: 'docs/{fileName}',
		outlet: 'docs',
	},
];
