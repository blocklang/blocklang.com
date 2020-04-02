import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectService, { ViewProjectServiceProperties } from '../../pages/project/ViewProjectService';

function getProperties(store: Store<State>): ViewProjectServiceProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewProjectService, 'state', {
	paths: [],
	getProperties,
});
