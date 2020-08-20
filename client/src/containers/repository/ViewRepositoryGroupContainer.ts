import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewRepositoryGroup, { ViewRepositoryGroupProperties } from '../../pages/repository/ViewRepositoryGroup';
import { initForViewRepositoryGroupProcess } from '../../processes/repositoryProcesses';

function getProperties(store: Store<State>): ViewRepositoryGroupProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		repository: get(path('repository')),
		groupId: get(path('repositoryResource', 'id')),
		path: get(path('repositoryResource', 'fullPath')),
		groups: get(path('parentGroups')),
		childResources: get(path('childResources')),
		latestCommitInfo: get(path('latestCommitInfo')),
		onOpenGroup: initForViewRepositoryGroupProcess(store),
	};
}

export default StoreContainer(ViewRepositoryGroup, 'state', {
	paths: [
		['user'],
		['repository'],
		['repositoryResource'],
		['parentGroups'],
		['childResources'],
		['latestCommitInfo'],
	],
	getProperties,
});
