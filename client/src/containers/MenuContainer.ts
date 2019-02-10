import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import Menu, { MenuProperties } from '../widgets/Menu';
import { State } from '../interfaces';

function getProperties(store: Store<State>): MenuProperties {
	const { get, path } = store;

	return {
		isAuthenticated: !!get(path('user', 'loginName')),
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl'))
	};
}

export default StoreContainer(Menu, 'state', { paths: [['user']], getProperties });
