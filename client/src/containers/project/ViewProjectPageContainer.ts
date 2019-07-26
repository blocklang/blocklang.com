import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectPage, { ViewProjectPageProperties } from '../../pages/project/ViewProjectPage';

function getProperties(store: Store<State>): ViewProjectPageProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewProjectPage, 'state', {
	paths: [],
	getProperties
});
