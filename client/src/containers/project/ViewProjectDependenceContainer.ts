import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectDependence, { ViewProjectDependenceProperties } from '../../pages/project/ViewProjectDependence';
import { initForViewProjectGroupProcess } from '../../processes/projectProcesses';
import {
	queryComponentReposForProjectProcess,
	addDependenceProcess,
	deleteDependenceProcess
} from '../../processes/projectDependenceProcesses';

function getProperties(store: Store<State>): ViewProjectDependenceProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		sourceId: get(path('projectDependenceResource', 'resourceId')),
		pathes: get(path('projectDependenceResource', 'pathes')),
		dependences: get(path('projectDependenceResource', 'dependences')),
		latestCommitInfo: get(path('latestCommitInfo')),
		pagedComponentRepos: get(path('pagedComponentRepoInfos')),
		onQueryComponentRepos: queryComponentReposForProjectProcess(store),
		onOpenGroup: initForViewProjectGroupProcess(store),
		onAddDependence: addDependenceProcess(store),
		onDeleteDependence: deleteDependenceProcess(store)
	};
}

export default StoreContainer(ViewProjectDependence, 'state', {
	paths: [['user'], ['project'], ['projectDependenceResource'], ['latestCommitInfo'], ['pagedComponentRepoInfos']],
	getProperties
});
