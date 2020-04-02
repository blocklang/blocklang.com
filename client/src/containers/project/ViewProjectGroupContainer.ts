import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectGroup, { ViewProjectGroupProperties } from '../../pages/project/ViewProjectGroup';
import { initForViewProjectGroupProcess } from '../../processes/projectProcesses';

function getProperties(store: Store<State>): ViewProjectGroupProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		project: get(path('project')),
		groupId: get(path('projectResource', 'id')),
		path: get(path('projectResource', 'fullPath')),
		groups: get(path('parentGroups')),
		childResources: get(path('childResources')),
		latestCommitInfo: get(path('latestCommitInfo')),
		onOpenGroup: initForViewProjectGroupProcess(store),
	};
}

export default StoreContainer(ViewProjectGroup, 'state', {
	paths: [['user'], ['project'], ['projectResource'], ['parentGroups'], ['childResources'], ['latestCommitInfo']],
	getProperties,
});
