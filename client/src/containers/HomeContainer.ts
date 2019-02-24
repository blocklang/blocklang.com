import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import Home, { HomeProperties } from '../pages/Home';
import { State } from '../interfaces';

function getProperties(store: Store<State>): HomeProperties {
	const { get, path } = store;

	return {
		isAuthenticated: !!get(path('user', 'loginName')),
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl'))
	};
}

export default StoreContainer(Home, 'state', { paths: [['user']], getProperties });
