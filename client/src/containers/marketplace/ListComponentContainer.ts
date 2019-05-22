import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ListComponent, { ListComponentProperties } from '../../pages/marketplace/ListComponent';

function getProperties(store: Store<State>): ListComponentProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		components: get(path('components'))
	};
}

export default StoreContainer(ListComponent, 'state', {
	paths: [['user'], ['components']],
	getProperties
});
