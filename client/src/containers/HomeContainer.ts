import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import Home, { HomeProperties } from '../pages/Home';
import { State } from '../interfaces';
import { LoginStatus } from '../constant';

function getProperties(store: Store<State>): HomeProperties {
	const { get, path } = store;

	return {
		isAuthenticated: !!get(path('user', 'loginName')),
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl')),
		canAccessProjects: get(path('canAccessProjects')),
		loginFailure: get(path('user', 'status')) === LoginStatus.FAILED,
		loginFailureMessage: get(path('user', 'loginFailureMessage')),
	};
}

export default StoreContainer(Home, 'state', { paths: [['user'], ['canAccessProjects']], getProperties });
