import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectDependence, { ViewProjectDependenceProperties } from '../../pages/repository/ViewProjectDependence';
import { initForViewRepositoryGroupProcess } from '../../processes/repositoryProcesses';
import {
	queryComponentReposForProjectProcess,
	addDependenceProcess,
	deleteDependenceProcess,
	showDependenceVersionsProcess,
	updateDependenceVersionProcess,
} from '../../processes/projectDependenceProcesses';

function getProperties(store: Store<State>): ViewProjectDependenceProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		repository: get(path('repository')),
		sourceId: get(path('projectDependenceResource', 'resourceId')),
		pathes: get(path('projectDependenceResource', 'pathes')),
		dependences: get(path('projectDependenceResource', 'dependences')),
		latestCommitInfo: get(path('latestCommitInfo')),
		pagedComponentRepos: get(path('pagedComponentRepoInfos')),
		onQueryComponentRepos: queryComponentReposForProjectProcess(store),
		onOpenGroup: initForViewRepositoryGroupProcess(store),
		onAddDependence: addDependenceProcess(store),
		onDeleteDependence: deleteDependenceProcess(store),
		onShowDependenceVersions: showDependenceVersionsProcess(store),
		onUpdateDependenceVersion: updateDependenceVersionProcess(store),
	};
}

export default StoreContainer(ViewProjectDependence, 'state', {
	paths: [
		['user'],
		['repository'],
		['projectDependenceResource'],
		['selectedDependenceVersions'],
		['latestCommitInfo'],
		['pagedComponentRepoInfos'],
	],
	getProperties,
});
