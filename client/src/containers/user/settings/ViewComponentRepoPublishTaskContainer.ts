import Store from '@dojo/framework/stores/Store';
import { State } from '../../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewComponentRepoPublishTask, {
	ViewComponentRepoPublishTaskProperties
} from '../../../pages/user/settings/ViewComponentRepoPublishTask';

function getProperties(store: Store<State>): ViewComponentRepoPublishTaskProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		publishTask: get(path('componentRepoPublishTask'))
	};
}

export default StoreContainer(ViewComponentRepoPublishTask, 'state', {
	paths: [['user'], ['componentRepoPublishTask']],
	getProperties
});
