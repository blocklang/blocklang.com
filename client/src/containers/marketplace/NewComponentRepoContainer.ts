import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewComponentRepo, { NewComponentRepoProperties } from '../../pages/marketplace/NewComponentRepo';
import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';

function getProperties(store: Store<State>): NewComponentRepoProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName'))
	};
}

export default StoreContainer(NewComponentRepo, 'state', {
	paths: [],
	getProperties
});
