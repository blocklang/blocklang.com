import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ListRelease, { ListReleaseProperties } from '../../pages/release/ListRelease';

function getProperties(store: Store<State>): ListReleaseProperties {
	const { get, path } = store;

	return {
		loggedUsername: get(path('user', 'loginName')),
		repository: get(path('repository')),
		releases: get(path('releases')),
	};
}

export default StoreContainer(ListRelease, 'state', {
	paths: [['user'], ['repository'], ['releases']],
	getProperties,
});
