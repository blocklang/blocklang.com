import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import Header, { HeaderProperties } from '../pages/Header';
import { State } from '../interfaces';

function getProperties(store: Store<State>): HeaderProperties {
	const { get, path } = store;

	return {
		routing: get(path('routing', 'outlet')),
		isAuthenticated: !!get(path('user', 'loginName')),
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl'))
	};
}

export default StoreContainer(Header, 'state', { paths: [['user'], ['routing']], getProperties });
