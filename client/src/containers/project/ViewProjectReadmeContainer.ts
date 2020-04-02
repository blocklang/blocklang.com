import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectReadme, { ViewProjectReadmeProperties } from '../../pages/project/ViewProjectReadme';

function getProperties(store: Store<State>): ViewProjectReadmeProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewProjectReadme, 'state', {
	paths: [],
	getProperties,
});
