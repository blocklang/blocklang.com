import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import { ListComponentRepoProperties } from '../../pages/marketplace/ListComponentRepo';
import ListMyComponentRepo from '../../pages/marketplace/ListMyComponentRepo';

function getProperties(store: Store<State>): ListComponentRepoProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		pagedComponentRepos: get(path('pagedComponentRepos')),
		marketplacePageStatusCode: get(path('marketplacePageStatusCode'))
	};
}

export default StoreContainer(ListMyComponentRepo, 'state', {
	paths: [['user'], ['pagedComponentRepos'], ['marketplacePageStatusCode']],
	getProperties
});
