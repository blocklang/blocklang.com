import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ListComponentRepo, { ListComponentRepoProperties } from '../../pages/marketplace/ListComponentRepo';
import { queryComponentReposProcess } from '../../processes/componentRepoProcess';

function getProperties(store: Store<State>): ListComponentRepoProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		pagedComponentRepos: get(path('pagedComponentRepoInfos')),
		marketplacePageStatusCode: get(path('marketplacePageStatusCode')),
		onQueryComponentRepos: queryComponentReposProcess(store),
	};
}

export default StoreContainer(ListComponentRepo, 'state', {
	paths: [['user'], ['pagedComponentRepoInfos'], ['marketplacePageStatusCode']],
	getProperties,
});
