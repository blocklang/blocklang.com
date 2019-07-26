import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewProjectTemplet, { ViewProjectTempletProperties } from '../../pages/project/ViewProjectTemplet';

function getProperties(store: Store<State>): ViewProjectTempletProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewProjectTemplet, 'state', {
	paths: [],
	getProperties
});
