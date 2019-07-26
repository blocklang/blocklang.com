import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectDependence, { ViewProjectDependenceProperties } from '../../pages/project/ViewProjectDependence';

function getProperties(store: Store<State>): ViewProjectDependenceProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewProjectDependence, 'state', {
	paths: [],
	getProperties
});
