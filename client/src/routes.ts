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
		path: '{user}',
		outlet: 'profile'
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
		path: '{owner}/{project}/releases',
		outlet: 'list-release'
	},
	{
		path: '{owner}/{project}/releases/new',
		outlet: 'new-release'
	},
	{
		path: 'docs/{fileName}',
		outlet: 'docs'
	}
];
