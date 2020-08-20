import Store from '@dojo/framework/stores/Store';
import { State } from '../../interfaces';
import { StoreContainer } from '@dojo/framework/stores/StoreInjector';
import ViewRepositoryTemplet, { ViewRepositoryTempletProperties } from '../../pages/repository/ViewRepositoryTemplet';

function getProperties(store: Store<State>): ViewRepositoryTempletProperties {
	// const { get, path } = store;

	return {};
}

export default StoreContainer(ViewRepositoryTemplet, 'state', {
	paths: [],
	getProperties,
});
