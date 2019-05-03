export default [
	{
		path: '',
		outlet: 'home',
		defaultRoute: true
	},
	{
		path: 'about',
		outlet: 'about'
	},
	{
		path: 'user/completeUserInfo',
		outlet: 'complete-user-info'
	},
	{
		path: '{user}',
		outlet: 'profile'
	},
	{
		path: 'settings/profile',
		outlet: 'settings-profile'
	},
	{
		path: 'projects/new',
		outlet: 'new-project'
	},
	{
		path: '{owner}/{project}',
		outlet: 'view-project'
	},
	{
		path: '{owner}/{project}/groups/{parentPath}',
		outlet: 'view-project-group'
	},
	{
		path: '{owner}/{project}/pages/new',
		outlet: 'new-page'
	},
	{
		path: '{owner}/{project}/groups/new',
		outlet: 'new-group'
	},
	{
		path: '{owner}/{project}/releases',
		outlet: 'list-release'
	},
	{
		path: '{owner}/{project}/releases/new',
		outlet: 'new-release'
	},
	{
		path: '{owner}/{project}/releases/{version}',
		outlet: 'view-release'
	},
	{
		path: 'docs/{fileName}',
		outlet: 'docs'
	}
];
