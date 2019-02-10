import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import Menu, { MenuProperties } from '../widgets/Menu';
import { State } from '../interfaces';

function getProperties(store: Store<State>): MenuProperties {
	const { get, path } = store;

	return {
		isAuthenticated: !!get(path('user', 'userId')),
		loggedUsername: get(path('user', 'loginName'))
	};
}

export default StoreContainer(Menu, 'state', { paths: [['user']], getProperties });
