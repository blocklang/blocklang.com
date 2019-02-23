import Store from '@dojo/framework/stores/Store';
import ViewProject, { ViewProjectProperties } from '../../widgets/project/ViewProject';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';

function getProperties(store: Store<State>): ViewProjectProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		name: get(path('project', 'name')),
		isPublic: get(path('project', 'isPublic'))
	};
}

export default StoreContainer(ViewProject, 'state', {
	paths: [['user'], ['project']],
	getProperties
});
