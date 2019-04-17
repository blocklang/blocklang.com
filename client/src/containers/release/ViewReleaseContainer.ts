import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewRelease, { ViewReleaseProperties } from '../../pages/release/ViewRelease';

function getProperties(store: Store<State>): ViewReleaseProperties {
	const { get, path } = store;

	return {
		project: get(path('project')),
		projectRelease: get(path('projectRelease'))
	};
}

export default StoreContainer(ViewRelease, 'state', {
	paths: [['project'], ['projectRelease']],
	getProperties
});
