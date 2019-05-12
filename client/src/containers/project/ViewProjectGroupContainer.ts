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
		parentId: get(path('parentResource', 'id')),
		parentPath: get(path('parentResource', 'path')),
		parentGroups: get(path('parentResource', 'parentGroups')),
		projectResources: get(path('projectResources')),
		latestCommitInfo: get(path('latestCommitInfo')),
		onOpenGroup: initForViewProjectGroupProcess(store)
	};
}

export default StoreContainer(ViewProjectGroup, 'state', {
	paths: [['user'], ['project'], ['parentResource'], ['projectResources'], ['latestCommitInfo']],
	getProperties
});
