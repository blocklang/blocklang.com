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
	}
];
