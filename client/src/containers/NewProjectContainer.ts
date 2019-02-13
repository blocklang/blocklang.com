import { Store } from '@dojo/framework/stores/Store';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import NewProject, { NewProjectProperties } from '../widgets/NewProject';
import { State } from '../interfaces';

function getProperties(store: Store<State>): NewProjectProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		loggedAvatarUrl: get(path('user', 'avatarUrl'))
	};
}

export default StoreContainer(NewProject, 'state', { paths: [['user']], getProperties });
