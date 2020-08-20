import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewRepositoryPage, { ViewRepositoryPageProperties } from '../../pages/repository/ViewRepositoryPage';
import { initForViewRepositoryGroupProcess } from '../../processes/repositoryProcesses';

function getProperties(store: Store<State>): ViewRepositoryPageProperties {
	const { get, path } = store;

	return {
		loginUser: get(path('user')),
		repository: get(path('repository')),
		resource: get(path('repositoryResource')),
		groups: get(path('parentGroups')),
		onGotoGroup: initForViewRepositoryGroupProcess(store),
	};
}

export default StoreContainer(ViewRepositoryPage, 'state', {
	paths: [['repository'], ['repositoryResource'], ['parentGroups'], ['user']],
	getProperties,
});
