import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectDependence, { ViewProjectDependenceProperties } from '../../pages/project/ViewProjectDependence';
import { initForViewProjectGroupProcess } from '../../processes/projectProcesses';

function getProperties(store: Store<State>): ViewProjectDependenceProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		parentId: get(path('projectResource', 'id')),
		parentPath: get(path('projectResource', 'path')),
		parentGroups: get(path('projectResource', 'parentGroups')),
		latestCommitInfo: get(path('latestCommitInfo')),
		onOpenGroup: initForViewProjectGroupProcess(store)
	};
}

export default StoreContainer(ViewProjectDependence, 'state', {
	paths: [],
	getProperties
});
