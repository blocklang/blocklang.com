import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectDependency, { ViewProjectDependencyProperties } from '../../pages/repository/ViewProjectDependency';
import { initForViewRepositoryGroupProcess } from '../../processes/repositoryProcesses';
import {
	queryComponentReposForProjectProcess,
	addDependencyProcess,
	deleteDependencyProcess,
	showDependencyVersionsProcess,
	updateDependencyVersionProcess,
} from '../../processes/projectDependencyProcesses';

function getProperties(store: Store<State>): ViewProjectDependencyProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		repository: get(path('repository')),
		sourceId: get(path('projectDependencyResource', 'resourceId')),
		pathes: get(path('projectDependencyResource', 'pathes')),
		dependencies: get(path('projectDependencyResource', 'dependencies')),
		latestCommitInfo: get(path('latestCommitInfo')),
		pagedComponentRepos: get(path('pagedComponentRepoInfos')),
		onQueryComponentRepos: queryComponentReposForProjectProcess(store),
		onOpenGroup: initForViewRepositoryGroupProcess(store),
		onAddDependency: addDependencyProcess(store),
		onDeleteDependency: deleteDependencyProcess(store),
		onShowDependencyVersions: showDependencyVersionsProcess(store),
		onUpdateDependencyVersion: updateDependencyVersionProcess(store),
	};
}

export default StoreContainer(ViewProjectDependency, 'state', {
	paths: [
		['user'],
		['repository'],
		['projectDependencyResource'],
		['selectedDependencyVersions'],
		['latestCommitInfo'],
		['pagedComponentRepoInfos'],
	],
	getProperties,
});
